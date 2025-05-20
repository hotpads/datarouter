/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.trace.filter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.session.CurrentSessionInfo;
import io.datarouter.auth.session.Session;
import io.datarouter.bytes.KvString;
import io.datarouter.gson.DatarouterGsons;
import io.datarouter.httpclient.client.DatarouterHttpCallTool;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.instrumentation.trace.TraceCategory;
import io.datarouter.instrumentation.trace.TraceDto;
import io.datarouter.instrumentation.trace.TraceSaveReasonType;
import io.datarouter.instrumentation.trace.TraceSpanDto;
import io.datarouter.instrumentation.trace.TraceThreadDto;
import io.datarouter.instrumentation.trace.TraceTimeTool;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationConstants.ExceptionInstrumentationConstants;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.trace.conveyor.DatarouterDebuggingBuffers;
import io.datarouter.trace.service.TraceUrlBuilder;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.types.Ulid;
import io.datarouter.util.PlatformMxBeans;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tracer.DatarouterTracer;
import io.datarouter.web.dispatcher.Dispatcher;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.HandlerMetrics;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.IpAddressService;
import io.datarouter.web.util.http.RecordedHttpHeaders;
import io.datarouter.web.util.http.RequestTool;

public abstract class TraceFilter implements Filter, InjectorRetriever{
	private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

	// Trace backend storage is often time-sorted.
	// TraceIds with abnormal future or past timestamps can cause problems with the storage.
	// We therefore discard traces that are further into the future or past than this.
	private static final Duration FUTURE_TIMESTAMP_TOLERANCE = Duration.ofSeconds(5);
	private static final Duration PAST_TIMESTAMP_TOLERANCE = Duration.ofDays(1);
	private static final int PAYLOAD_SIZE_METRIC_THRESHOLD_BYTES = 5242880; // 5 MB

	private ServerName serverName;
	private DatarouterTraceFilterSettingRoot traceSettings;
	private DatarouterDebuggingBuffers debuggingBuffers;
	private TraceUrlBuilder urlBuilder;
	private CurrentSessionInfo currentSessionInfo;
	private ServiceName serviceName;
	private EnvironmentName environmentName;
	private IpAddressService ipAddressService;

	@Override
	public void init(FilterConfig filterConfig){
		DatarouterInjector injector = getInjector(filterConfig.getServletContext());
		serverName = injector.getInstance(ServerName.class);
		debuggingBuffers = injector.getInstance(DatarouterDebuggingBuffers.class);
		traceSettings = injector.getInstance(DatarouterTraceFilterSettingRoot.class);
		urlBuilder = injector.getInstance(TraceUrlBuilder.class);
		currentSessionInfo = injector.getInstance(CurrentSessionInfo.class);
		serviceName = injector.getInstance(ServiceName.class);
		environmentName = injector.getInstance(EnvironmentName.class);
		ipAddressService = injector.getInstance(IpAddressService.class);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException{
		try{
			HttpServletRequest request = (HttpServletRequest)req;
			HttpServletResponse response = (HttpServletResponse)res;

			// get or create TraceContext
			boolean shouldBeRandomlySampled = false;
			String traceparentStr = request.getHeader(DatarouterHttpCallTool.TRACEPARENT);
			String tracestateStr = request.getHeader(DatarouterHttpCallTool.TRACESTATE);
			long traceCreatedEpochNanos = TraceTimeTool.epochNano();
			var traceContext = new W3TraceContext(traceparentStr, tracestateStr, traceCreatedEpochNanos);
			if(isTraceIdTimestampOutsideCutoffTimes(traceContext.getTraceparent())){
				traceContext = new W3TraceContext(traceCreatedEpochNanos);
				logger.info("Replacing Datarouter standard non-conforming {} with {}",
						new KvString().add("traceparent", traceparentStr),
						new KvString().add("traceparent", traceContext.getTraceparent().toString()));
				countDiscarding("original traceContext");
			}
			String initialParentId = traceContext.getTraceparent().parentId;
			traceContext.updateParentIdAndAddTracestateMember();
			RequestAttributeTool.set(
					request,
					BaseHandler.TRACE_URL_REQUEST_ATTRIBUTE,
					urlBuilder.buildTraceForCurrentServer(traceContext.getTraceparent().toString()));
			RequestAttributeTool.set(request, BaseHandler.TRACE_CONTEXT, traceContext.copy());
			if(RandomTool.getRandomIntBetweenTwoNumbers(1, traceSettings.randomSamplingMax.get())
					<= traceSettings.randomSamplingThreshold.get()){
				shouldBeRandomlySampled = true;
				traceContext.getTraceparent().enableSample();
			}

			// need to set the header before doFilter
			// traceflag might be incorrect because it might have been modified during request processing
			if(traceSettings.addTraceparentHeader.get()){
				response.setHeader(
						DatarouterHttpCallTool.TRACEPARENT,
						traceContext.getTraceparent().toString());
			}

			// bind these to all threads, even if tracing is disabled
			Tracer tracer = new DatarouterTracer(
					serverName.get(),
					null,
					traceContext,
					traceSettings.maxSpansPerTrace.get());
			tracer.setSaveThreadCpuTime(traceSettings.saveThreadCpuTime.get());
			tracer.setSaveThreadMemoryAllocated(traceSettings.saveThreadMemoryAllocated.get());
			tracer.setSaveSpanCpuTime(traceSettings.saveSpanCpuTime.get());
			tracer.setSaveSpanMemoryAllocated(traceSettings.saveSpanMemoryAllocated.get());
			TracerThreadLocal.bindToThread(tracer);
			String requestThreadName = (request.getContextPath() + " request").trim();
			tracer.createAndStartThread(requestThreadName, TraceTimeTool.epochNano());

			// This is read from the ThreadContext by the log4j2 pattern in DatarouterTraceIdLog4j2Configuration
			// https://logging.apache.org/log4j/2.x/manual/layouts.html#pattern-layout.
			ThreadContext.put("traceId", traceContext.getTraceparent().traceId);

			long threadId = Thread.currentThread().threadId();
			boolean saveCpuTime = traceSettings.saveTraceCpuTime.get();
			Long cpuTimeBegin = saveCpuTime ? PlatformMxBeans.THREAD.getCurrentThreadCpuTime() : null;
			boolean saveAllocatedBytes = traceSettings.saveTraceAllocatedBytes.get();
			Long threadAllocatedBytesBegin = saveAllocatedBytes
					? PlatformMxBeans.THREAD.getThreadAllocatedBytes(threadId)
					: null;

			boolean errored = false;
			try{
				fc.doFilter(req, res);
			}catch(Exception e){
				errored = true;
				throw e;
			}finally{
				long ended = TraceTimeTool.epochNano();
				Long cpuTimeEnded = saveCpuTime ? PlatformMxBeans.THREAD.getCurrentThreadCpuTime() : null;
				Long threadAllocatedBytesEnded = saveAllocatedBytes
						? PlatformMxBeans.THREAD.getThreadAllocatedBytes(threadId)
						: null;
				Traceparent traceparent = tracer.getTraceContext().get().getTraceparent();
				TraceThreadDto rootThread = null;
				if(tracer.getCurrentThreadId() != null){
					rootThread = ((DatarouterTracer)tracer).getCurrentThread();
					rootThread.setCpuTimeEndedNs(cpuTimeEnded);
					rootThread.setMemoryAllocatedBytesEnded(threadAllocatedBytesEnded);
					rootThread.setEnded(ended);
					((DatarouterTracer)tracer).setCurrentThread(null);
				}
				List<TraceSaveReasonType> saveReasons = new ArrayList<>();
				var trace = new TraceDto(
						traceparent,
						initialParentId,
						request.getContextPath(),
						request.getRequestURI().toString(),
						request.getQueryString(),
						traceCreatedEpochNanos,
						ended,
						serviceName.get(),
						tracer.getDiscardedThreadCount(),
						tracer.getThreadQueue().size(),
						cpuTimeBegin,
						cpuTimeEnded,
						threadAllocatedBytesBegin,
						threadAllocatedBytesEnded,
						saveReasons,
						TraceCategory.HTTP_REQUEST,
						environmentName.get());

				Long traceDurationMs = trace.getDurationInMs();
				long mainThreadCpuTimeNs = saveCpuTime ? cpuTimeEnded - cpuTimeBegin : -1;
				long totalCpuTimeNs = -1;
				if(saveCpuTime && traceSettings.saveThreadCpuTime.get()){
					totalCpuTimeNs = mainThreadCpuTimeNs;
					for(TraceThreadDto thread : tracer.getThreadQueue()){
						totalCpuTimeNs += thread.getCpuTimeEndedNs() - thread.getCpuTimeCreatedNs();
					}
				}
				int childThreadCount = tracer.getThreadQueue().size() + tracer.getDiscardedThreadCount();
				long totalCpuTimeMs = TimeUnit.NANOSECONDS.toMillis(totalCpuTimeNs);
				Long threadAllocatedKB = saveAllocatedBytes
						? (threadAllocatedBytesEnded - threadAllocatedBytesBegin) / 1024
						: null;
				String traceCounterPrefix = "traceSaved ";
				if(traceSettings.saveTraces.get()){
					if(traceDurationMs > traceSettings.saveTracesOverMs.get()){
						saveReasons.add(TraceSaveReasonType.DURATION);
					}
					if(RequestTool.getBoolean(request, "trace", false)){
						saveReasons.add(TraceSaveReasonType.QUERY_PARAM);
					}
					if(!shouldBeRandomlySampled && tracer.shouldSample()){
						saveReasons.add(TraceSaveReasonType.TRACE_CONTEXT);
					}
					if(errored){
						saveReasons.add(TraceSaveReasonType.ERROR);
					}
					if(totalCpuTimeMs > traceSettings.saveTracesCpuOverMs.get()){
						saveReasons.add(TraceSaveReasonType.CPU);
					}
					if(shouldBeRandomlySampled){
						saveReasons.add(TraceSaveReasonType.RANDOM_SAMPLING);
					}
					saveReasons.forEach(reason -> Metrics.count(traceCounterPrefix + reason.type));
				}
				if(!saveReasons.isEmpty()){
					List<TraceThreadDto> threads = new ArrayList<>(tracer.getThreadQueue());
					List<TraceSpanDto> spans = new ArrayList<>(tracer.getSpanQueue());
					if(rootThread != null){
						rootThread.setTotalSpanCount(spans.size());
						threads.add(rootThread); // force to save the rootThread even though the queue could be full
					}
					String userAgent = RequestTool.getUserAgent(request);
					String userToken = currentSessionInfo.getSession(request)
							.map(Session::getUserToken)
							.orElse("unknown");
					HttpRequestRecordDto httpRequestRecord = buildHttpRequestRecord(
							errored,
							request,
							traceCreatedEpochNanos,
							userToken,
							traceparent);
					Optional<String> destination = offerTrace(
							new TraceBundleDto(trace, threads, spans),
							Optional.ofNullable(httpRequestRecord));
					if(destination.isEmpty()){
						Metrics.count("traceSavedNotAllowed");
						Metrics.count("traceSavedNotAllowed " + trace.type);
					}
					String logAction = destination.isPresent() ? "saved" : "not allowed to save";
					var logAttributes = new KvString()
							.add("to", destination.orElse(null))
							.add("traceparent", traceparent, Traceparent::toString)
							.add("initialParentId", initialParentId)
							.add("durationMs", traceDurationMs, Number::toString)
							.add("mainThreadCpuTimeMs",
									TimeUnit.NANOSECONDS.toMillis(mainThreadCpuTimeNs),
									Number::toString)
							.add("totalCpuTimeMs", totalCpuTimeMs, Number::toString)
							.add("spansRecorded", spans.size(), Number::toString)
							.add("spansDiscarded", tracer.getDiscardedSpanCount(), Number::toString)
							.add("childThreadCount", childThreadCount, Number::toString)
							.add("threadAllocatedKB", threadAllocatedKB, Number::toString)
							.add("path", trace.type)
							.add("query", trace.params)
							.add("userAgent", "\"" + userAgent + "\"")
							.add("userToken", userToken)
							.add("saveReasons", saveReasons, List::toString);
					logger.warn("Trace {} {}", logAction, logAttributes);
				}else if(traceDurationMs > traceSettings.logTracesOverMs.get()
						|| TracerTool.shouldLog()){
					// only log once
					var logAttributes = new KvString()
							.add("traceparent", traceparent, Traceparent::toString)
							.add("durationMs", traceDurationMs, Number::toString)
							.add("mainThreadCpuTimeMs",
									TimeUnit.NANOSECONDS.toMillis(mainThreadCpuTimeNs),
									Number::toString)
							.add("totalCpuTimeMs", totalCpuTimeMs, Number::toString)
							.add("spansRecorded", tracer.getSpanQueue().size(), Number::toString)
							.add("spansDiscarded", tracer.getDiscardedSpanCount(), Number::toString)
							.add("childThreadCount", childThreadCount, Number::toString)
							.add("threadAllocatedKB", threadAllocatedKB, Number::toString)
							.add("path", trace.type)
							.add("query", trace.params)
							.add("saveReasons", saveReasons, List::toString);
					logger.warn("Trace logged {}", logAttributes);
				}
				Optional<Class<? extends BaseHandler>> handlerClassOpt = RequestAttributeTool.get(
						request,
						BaseHandler.HANDLER_CLASS);
				Optional<Method> handlerMethodOpt = RequestAttributeTool.get(request, BaseHandler.HANDLER_METHOD);
				if(handlerClassOpt.isPresent() && handlerMethodOpt.isPresent()){
					Class<? extends BaseHandler> handlerClass = handlerClassOpt.get();
					if(traceSettings.recordAllLatency.get()){
						HandlerMetrics.saveMethodLatency(handlerClass, handlerMethodOpt.get(), traceDurationMs);
					}
					HandlerMetrics.incDuration(handlerClass, handlerMethodOpt.get(), traceDurationMs);
					if(totalCpuTimeNs != -1){
						HandlerMetrics.incTotalCpuTime(handlerClass, handlerMethodOpt.get(), totalCpuTimeMs);
					}
					if(traceSettings.savePayloadSizeBytes.get()
							&& request.getContentLengthLong() > PAYLOAD_SIZE_METRIC_THRESHOLD_BYTES){
						HandlerMetrics.savePayloadSize(handlerClassOpt.get(), handlerMethodOpt.get(),
								request.getContentLengthLong());
					}
				}
			}
		}finally{
			TracerThreadLocal.clearFromThread();
		}
	}

	private HttpRequestRecordDto buildHttpRequestRecord(
			boolean errored,
			HttpServletRequest request,
			Long receivedAt,
			String userToken,
			Traceparent traceparent){
		if(errored){
			// an exception in a request is recorded in the ExceptionRecorder already.
			return null;
		}
		receivedAt = TimeUnit.NANOSECONDS.toMillis(receivedAt);
		long created = TimeUnit.NANOSECONDS.toMillis(TraceTimeTool.epochNano());
		RecordedHttpHeaders headersWrapper = new RecordedHttpHeaders(request);
		var untrimmedRecord = new HttpRequestRecordDto(
				new Ulid().value(),
				new Date(created),
				new Date(receivedAt),
				created - receivedAt,
				null, // no exceptionRecordId
				traceparent.traceId,
				traceparent.parentId,
				request.getMethod(),
				Optional.ofNullable(request.getParameterMap())//TODO is it nullable?
						.map(TraceFilter::formatParamMap)
						.orElse(null),
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				getRequestPath(request),
				request.getQueryString(),
				getBinaryBody(request),
				ipAddressService.getIpAddress(request),
				currentSessionInfo.getRoles(request).toString(),
				userToken,
				headersWrapper.getAcceptCharset(),
				headersWrapper.getAcceptEncoding(),
				headersWrapper.getAcceptLanguage(),
				headersWrapper.getAccept(),
				headersWrapper.getCacheControl(),
				headersWrapper.getConnection(),
				headersWrapper.getContentEncoding(),
				headersWrapper.getContentLanguage(),
				headersWrapper.getContentLength(),
				headersWrapper.getContentType(),
				headersWrapper.getCookie(),
				headersWrapper.getDnt(),
				headersWrapper.getHost(),
				headersWrapper.getIfModifiedSince(),
				headersWrapper.getOrigin(),
				headersWrapper.getPragma(),
				headersWrapper.getReferer(),
				headersWrapper.getUserAgent(),
				headersWrapper.getXForwardedFor(),
				headersWrapper.getXRequestedWith(),
				headersWrapper.getOthers(),
				environmentName.get());
		return untrimmedRecord.trimmed();
	}

	private static String getRequestPath(HttpServletRequest request){
		String requestUri = request.getRequestURI();
		return requestUri == null ? "" : requestUri.substring(StringTool.nullSafe(request.getContextPath()).length());
	}

	private static byte[] getBinaryBody(HttpServletRequest request){
		if(RequestAttributeTool.get(request, Dispatcher.TRANSMITS_PII).orElse(false)){
			return HttpRequestRecordDto.CONFIDENTIALITY_MSG_BYTES;
		}
		byte[] binaryBody = RequestTool.tryGetBodyAsByteArray(request);
		int originalLength = binaryBody.length;
		return originalLength > ExceptionInstrumentationConstants.MAX_SIZE_BINARY_BODY
				? ArrayTool.trimToSize(binaryBody, ExceptionInstrumentationConstants.MAX_SIZE_BINARY_BODY)
				: binaryBody;
	}

	@SuppressWarnings("deprecation")
	private static String formatParamMap(Map<String,String[]> paramMap){
		Map<String,List<String>> trimmed = Scanner.of(paramMap.entrySet())
				.toMap(Entry::getKey,
						entry -> Scanner.of(entry.getValue())
								.limit(10)
								.map(str -> StringTool.trimToSize(str, 100))
								.list());
		return DatarouterGsons.withUnregisteredEnums().toJson(trimmed);
	}

	private Optional<String> offerTrace(TraceBundleDto traceBundle, Optional<HttpRequestRecordDto> httpRequestRecord){
		if(httpRequestRecord.isPresent()){
			debuggingBuffers.httpRequests.offer(httpRequestRecord.get());
		}
		debuggingBuffers.traces.offer(traceBundle);
		return Optional.of(debuggingBuffers.traces.getName());
	}

	public static boolean isTraceIdTimestampOutsideCutoffTimes(Traceparent traceparent){
		Instant timestamp;
		try{
			timestamp = traceparent.getInstantTruncatedToMillis();
		}catch(NumberFormatException e){
			logger.warn("traceid not prefixed with 16 digit hex-nano timestamp {}", new KvString()
					.add("traceparent", traceparent.toString()),
					e);
			countDiscarding("invalid traceid");
			return true;
		}
		Instant now = Instant.now();
		Instant cutoffFuture = now.plus(FUTURE_TIMESTAMP_TOLERANCE);
		if(timestamp.isAfter(cutoffFuture)){
			logger.info("discarding future timestamp {}", new KvString()
					.add("cutoff", cutoffFuture.toString())
					.add("traceparent", traceparent.toString())
					.add("timestamp", timestamp.toString()));
			countDiscarding("future timestamp");
			return true;
		}
		Instant cutoffPast = now.minus(PAST_TIMESTAMP_TOLERANCE);
		if(timestamp.isBefore(cutoffPast)){
			logger.info("discarding past timestamp {}", new KvString()
					.add("cutoff", cutoffPast.toString())
					.add("traceparent", traceparent.toString())
					.add("timestamp", timestamp.toString()));
			countDiscarding("past timestamp");
			return true;
		}
		return false;
	}

	private static void countDiscarding(String string){
		Metrics.count("trace discarding all");
		Metrics.count("trace discarding " + string);
	}

}
