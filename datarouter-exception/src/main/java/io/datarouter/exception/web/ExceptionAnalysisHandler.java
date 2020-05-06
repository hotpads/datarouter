/**
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
package io.datarouter.exception.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.gson.reflect.TypeToken;

import io.datarouter.exception.config.DatarouterExceptionFiles;
import io.datarouter.exception.config.DatarouterExceptionPaths;
import io.datarouter.exception.service.ExceptionGraphLink;
import io.datarouter.exception.service.ExceptionIssueLinkPrefixSupplier;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord.HttpRequestRecordByExceptionRecord;
import io.datarouter.exception.storage.metadata.DatarouterExceptionRecordSummaryMetadataDao;
import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadata;
import io.datarouter.exception.storage.metadata.ExceptionRecordSummaryMetadataKey;
import io.datarouter.exception.storage.summary.DatarouterExceptionRecordSummaryDao;
import io.datarouter.exception.storage.summary.ExceptionRecordSummary;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.model.databean.Databean;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.exception.ExceptionCounters;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.util.ExceptionService;
import io.datarouter.web.util.http.CookieTool;

public class ExceptionAnalysisHandler extends BaseHandler{

	public static final String P_exceptionRecord = "exceptionRecord";

	@Inject
	private ExceptionService exceptionService;
	@Inject
	private DatarouterExceptionFiles files;
	@Inject
	private DatarouterExceptionPaths paths;
	@Inject
	private DatarouterExceptionRecordDao exceptionRecordDao;
	@Inject
	private DatarouterHttpRequestRecordDao httpRequestRecordDao;
	@Inject
	private DatarouterExceptionRecordSummaryDao exceptionRecordSummaryDao;
	@Inject
	private DatarouterExceptionRecordSummaryMetadataDao exceptionSummaryMetadataDao;
	@Inject
	private ExceptionGraphLink exceptionGraphLink;
	@Inject
	private ExceptionIssueLinkPrefixSupplier issueLinkPrefixSupplier;

	@Handler
	public Mav browse(){
		Mav mav = new Mav(files.jsp.datarouter.exception.browseExceptionsJsp);
		Long lastPeriodStart = null;
		SortedSet<ExceptionRecordSummary> summaries = new TreeSet<>(Comparator.comparing(
				ExceptionRecordSummary::getNumExceptions,
				Comparator.reverseOrder()));
		Set<ExceptionRecordSummaryMetadataKey> metadataKeys = new HashSet<>();
		for(var summary : exceptionRecordSummaryDao.scan().iterable()){
			if(lastPeriodStart == null){
				lastPeriodStart = summary.getKey().getPeriodStart();
			}else if(lastPeriodStart != summary.getKey().getPeriodStart()){
				break;
			}
			summaries.add(summary);
			metadataKeys.add(summary.getKey().getExceptionRecordSummaryMetadataKey());
		}
		mav.put("exceptionRecordSummaries", summaries);
		Map<ExceptionRecordSummaryMetadataKey,ExceptionRecordSummaryMetadata> summaryMetadatas =
				exceptionSummaryMetadataDao.getMulti(metadataKeys).stream()
				.collect(Collectors.toMap(Databean::getKey, Function.identity()));

		mav.put("summaryMetadatas", summaryMetadatas);
		if(lastPeriodStart != null){
			mav.put("lastPeriodStart", ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastPeriodStart),
					ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME));
		}
		mav.put("detailsPath", paths.datarouter.exception.details.toSlashedString());
		mav.put("issueLinkPrefix", issueLinkPrefixSupplier.get());
		return mav;
	}

	protected Mav initDetailsMav(){
		return new Mav(files.jsp.datarouter.exception.exceptionDetailsJsp);
	}

	@Handler
	public Mav details(@Param(P_exceptionRecord) OptionalString exceptionRecord){
		Mav mav = initDetailsMav();
		mav.put("detailsPath", paths.datarouter.exception.details.toSlashedString());
		if(exceptionRecord.isEmpty()){
			return mav;
		}
		String exceptionRecordId = exceptionRecord.get();
		exceptionRecordId = trimExceptionRecordId(exceptionRecordId);
		ExceptionRecord record = getExceptionRecord(exceptionRecordId);
		if(record == null){
			return new MessageMav("Exception record with id=" + exceptionRecordId + " does not exist");
		}
		HttpRequestRecordJspDto httpRequestRecordJspDto = null;
		mav.put("exceptionRecord", toJspDto(record));
		mav.put("coloredStackTrace", exceptionService.getColorized(record.getStackTrace()));
		mav.put("shortStackTrace", exceptionService.getShortStackTrace(record.getStackTrace()));
		mav.put("serviceName", record.getServiceName());
		HttpRequestRecord httpRequestRecord = getHttpRequestRecord(record);
		if(httpRequestRecord != null){
			httpRequestRecordJspDto = toJspDto(httpRequestRecord);
			mav.put("httpRequestRecord", httpRequestRecordJspDto);
		}
		mav.put("browsePath", paths.datarouter.exception.browse.toSlashedString());
		return mav;
	}

	@Handler
	public String mute(String type, String exceptionLocation, Boolean muted){
		return createOrUpdateMetadata(type, exceptionLocation, metadata -> metadata.setMuted(muted));
	}

	@Handler
	public String saveIssue(String type, String exceptionLocation, String issue){
		String cleanedIssue = Optional.of(issue)
				.map(String::trim)
				.filter(StringTool::notEmpty)
				.orElse(null);
		return createOrUpdateMetadata(type, exceptionLocation, metadata -> metadata.setIssue(cleanedIssue));
	}

	@Handler
	public Mav recordIssueAndRedirect(String type, String exceptionRecordId, String issue, OptionalBoolean muted){
		if(StringTool.isNullOrEmptyOrWhitespace(issue)){
			throw new IllegalArgumentException("Issue ID cannot be empty");
		}
		String trimmedIssue = issue.trim();
		String exceptionLocation = getExceptionRecord(exceptionRecordId).getExceptionLocation();
		createOrUpdateMetadata(type, exceptionLocation, metadata -> {
			if(!trimmedIssue.equals(metadata.getIssue())){
				metadata.setIssue(trimmedIssue);
				ExceptionCounters.inc("linked issue");
				ExceptionCounters.inc("linked issue " + StringTool.getStringBeforeFirstOccurrence('-', trimmedIssue));
			}
			if(BooleanTool.isFalseOrNull(metadata.getMuted())){
				muted
					.filter(BooleanTool::isTrue)
					.ifPresent($ -> {
						metadata.setMuted(true);
						ExceptionCounters.inc("muted");
					});
			}
		});
		return new GlobalRedirectMav(request.getRequestURI());
	}

	private String createOrUpdateMetadata(
			String type,
			String exceptionLocation,
			Consumer<ExceptionRecordSummaryMetadata> action){
		ExceptionRecordSummaryMetadataKey key = getExceptionRecordSummaryMetadataKey(type, exceptionLocation);
		ExceptionRecordSummaryMetadata metadata = exceptionSummaryMetadataDao.get(key);
		if(metadata == null){
			metadata = getExceptionRecordSummaryMetadata(key);
		}
		action.accept(metadata);
		exceptionSummaryMetadataDao.put(metadata);
		return "success";
	}

	private String trimExceptionRecordId(String exceptionRecordId){
		if(StringTool.containsCaseInsensitive(exceptionRecordId, "=")){
			return StringTool.getStringAfterLastOccurrence('=', exceptionRecordId);
		}
		return exceptionRecordId;
	}

	protected ExceptionRecord getExceptionRecord(String id){
		return exceptionRecordDao.get(new ExceptionRecordKey(id));
	}

	protected HttpRequestRecord getHttpRequestRecord(ExceptionRecord exceptionRecord){
		var key = new HttpRequestRecordByExceptionRecord(exceptionRecord);
		return httpRequestRecordDao.lookupUnique(key);
	}

	protected ExceptionRecordSummaryMetadataKey getExceptionRecordSummaryMetadataKey(
			String type,
			String exceptionLocation){
		return new ExceptionRecordSummaryMetadataKey(type, exceptionLocation);
	}

	protected ExceptionRecordSummaryMetadata getExceptionRecordSummaryMetadata(ExceptionRecordSummaryMetadataKey key){
		return new ExceptionRecordSummaryMetadata(key);
	}


	/*---------------------------- JspDtos ----------------------------------*/

	private ExceptionRecordJspDto toJspDto(ExceptionRecord exceptionRecord){
		return new ExceptionRecordJspDto(
				exceptionRecord.getKey().getId(),
				exceptionRecord.getCreated(),
				exceptionRecord.getServerName(),
				exceptionRecord.getType(),
				exceptionRecord.getAppVersion(),
				exceptionRecord.getExceptionLocation(),
				exceptionRecord.getCallOrigin(),
				exceptionGraphLink.getMetricLink(exceptionRecord),
				exceptionGraphLink.getCallOriginLink(exceptionRecord),
				exceptionGraphLink.getExactMetricLink(exceptionRecord));
	}

	public static class ExceptionRecordJspDto{

		private final String id;
		private final Date created;
		private final String serverName;
		private final String type;
		private final String appVersion;
		private final String exceptionLocation;
		private final String callOrigin;
		private final String metricLink;
		private final String callOriginLink;
		private final String exactMetricLink;

		public ExceptionRecordJspDto(
				String id,
				Date created,
				String serverName,
				String type,
				String appVersion,
				String exceptionLocation,
				String callOrigin,
				String metricLink,
				String callOriginLink,
				String exactMetricLink){
			this.id = id;
			this.created = created;
			this.serverName = serverName;
			this.type = type;
			this.appVersion = appVersion;
			this.exceptionLocation = exceptionLocation;
			this.callOrigin = callOrigin;
			this.metricLink = metricLink;
			this.callOriginLink = callOriginLink;
			this.exactMetricLink = exactMetricLink;
		}

		public String getId(){
			return id;
		}

		public Date getCreated(){
			return created;
		}

		public String getServerName(){
			return serverName;
		}

		public String getType(){
			return type;
		}

		public String getAppVersion(){
			return appVersion;
		}

		public String getExceptionLocation(){
			return exceptionLocation;
		}

		public String getCallOrigin(){
			return callOrigin;
		}

		public String getMetricLink(){
			return metricLink;
		}

		public String getCallOriginLink(){
			return callOriginLink;
		}

		public String getExactMetricLink(){
			return exactMetricLink;
		}

	}

	private HttpRequestRecordJspDto toJspDto(HttpRequestRecord httpRequestRecord){
		return new HttpRequestRecordJspDto(
				httpRequestRecord.getCreated(),
				httpRequestRecord.getReceivedAt(),
				httpRequestRecord.getDuration(),
				httpRequestRecord.getExceptionRecordId(),
				httpRequestRecord.getHttpMethod(),
				httpRequestRecord.getHttpParams(),
				httpRequestRecord.getProtocol(),
				httpRequestRecord.getHostname(),
				httpRequestRecord.getPort(),
				httpRequestRecord.getContextPath(),
				httpRequestRecord.getPath(),
				httpRequestRecord.getQueryString(),
				httpRequestRecord.getBinaryBody(),
				httpRequestRecord.getIp(),
				httpRequestRecord.getUserRoles(),
				httpRequestRecord.getUserToken(),
				httpRequestRecord.getHeaders(),
				httpRequestRecord.getOtherHeaders());
	}

	public static class HttpRequestRecordJspDto{

		private final Date created;
		private final Date receivedAt;
		private final Long duration;

		private final String exceptionRecordId;

		private final String httpMethod;
		private final String httpParams;

		private final String protocol;
		private final String hostname;
		private final int port;
		private final String contextPath;
		private final String path;
		private final String queryString;
		private final byte[] binaryBody;

		private final String ip;
		private final String userRoles;
		private final String userToken;

		private final Map<String,String> headers;
		private final String otherHeaders;

		public HttpRequestRecordJspDto(
				Date created,
				Date receivedAt,
				Long duration,
				String exceptionRecordId,
				String httpMethod,
				String httpParams,
				String protocol,
				String hostname,
				int port,
				String contextPath,
				String path,
				String queryString,
				byte[] binaryBody,
				String ip,
				String userRoles,
				String userToken,
				Map<String,String> headers,
				String otherHeaders){
			this.created = created;
			this.receivedAt = receivedAt;
			this.duration = duration;
			this.exceptionRecordId = exceptionRecordId;
			this.httpMethod = httpMethod;
			this.httpParams = httpParams;
			this.protocol = protocol;
			this.hostname = hostname;
			this.port = port;
			this.contextPath = contextPath;
			this.path = path;
			this.queryString = queryString;
			this.binaryBody = binaryBody;
			this.ip = ip;
			this.userRoles = userRoles;
			this.userToken = userToken;
			this.headers = headers;
			this.otherHeaders = otherHeaders;
		}

		public Map<String,String[]> getOtherHeadersMap(){
			return GsonTool.GSON.fromJson(otherHeaders, new TypeToken<Map<String,String[]>>(){}.getType());
		}

		public Map<String,String[]> getHttpParamsMap(){
			return GsonTool.GSON.fromJson(httpParams, new TypeToken<Map<String,String[]>>(){}.getType());
		}

		public Map<String,String> getCookiesMap(){
			return CookieTool.getMapFromString(headers.get(HttpHeaders.COOKIE), ";", "=");
		}

		public boolean isFromAjax(){
			return "XMLHttpRequest".equals(headers.get(HttpHeaders.X_REQUESTED_WITH));
		}

		public String getUrl(){
			return getProtocol() + "://" + hostname + ":" + port + (contextPath == null ? "" : contextPath) + path
					+ (queryString != null ? "?" + queryString : "");
		}

		public Map<String,String> getHeaders(){
			return headers;
		}

		public String getStringBody(){
			if(binaryBody != null){
				return new String(binaryBody);
			}
			return null;
		}

		public Date getCreated(){
			return created;
		}

		public Date getReceivedAt(){
			return receivedAt;
		}

		public Long getDuration(){
			return duration;
		}

		public String getExceptionRecordId(){
			return exceptionRecordId;
		}

		public String getHttpMethod(){
			return httpMethod;
		}

		public String getHttpParams(){
			return httpParams;
		}

		public String getProtocol(){
			return protocol;
		}

		public String getHostname(){
			return hostname;
		}

		public int getPort(){
			return port;
		}

		public String getContextPath(){
			return contextPath;
		}

		public String getPath(){
			return path;
		}

		public String getQueryString(){
			return queryString;
		}

		public byte[] getBinaryBody(){
			return binaryBody;
		}

		public String getIp(){
			return ip;
		}

		public String getUserRoles(){
			return userRoles;
		}

		public String getUserToken(){
			return userToken;
		}

	}

}
