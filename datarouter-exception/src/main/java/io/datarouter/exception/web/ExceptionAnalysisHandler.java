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
package io.datarouter.exception.web;

import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;

import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.exception.config.DatarouterExceptionFiles;
import io.datarouter.exception.config.DatarouterExceptionPaths;
import io.datarouter.exception.service.ExceptionGraphLink;
import io.datarouter.exception.storage.exceptionrecord.DatarouterExceptionRecordDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.exception.storage.httprecord.DatarouterHttpRequestRecordDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.gson.GsonTool;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.types.MilliTime;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.util.ExceptionService;
import io.datarouter.web.util.http.CookieTool;
import jakarta.inject.Inject;

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
	private ExceptionGraphLink exceptionGraphLink;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;

	// TODO use J2Html
	@Handler
	public Mav details(@Param(P_exceptionRecord) Optional<String> exceptionRecord){
		Mav mav = new Mav(files.jsp.datarouter.exception.exceptionDetailsJsp);
		mav.put("detailsPath", paths.datarouter.exception.details.toSlashedString());
		if(exceptionRecord.isEmpty()){
			return mav;
		}
		String exceptionRecordId = exceptionRecord.get();
		exceptionRecordId = trimExceptionRecordId(exceptionRecordId);
		ExceptionRecord record = getExceptionRecord(exceptionRecordId);
		if(record == null){
			return makeExceptionDoesNotExistMav(exceptionRecordId);
		}
		mav.put("exceptionRecord", toJspDto(record));
		mav.put("coloredStackTrace", exceptionService.getColorized(record.getStackTrace()));
		mav.put("shortStackTrace", exceptionService.getShortStackTrace(record.getStackTrace()));
		mav.put("serviceName", record.getServiceName());
		findHttpRequestRecord(record)
				.map(this::toJspDto)
				.ifPresent(dto -> mav.put("httpRequestRecord", dto));
		return mav;
	}

	private String trimExceptionRecordId(String exceptionRecordId){
		if(StringTool.containsCaseInsensitive(exceptionRecordId, "=")){
			return StringTool.getStringAfterLastOccurrence('=', exceptionRecordId);
		}
		return exceptionRecordId;
	}

	private ExceptionRecord getExceptionRecord(String id){
		return exceptionRecordDao.get(new ExceptionRecordKey(id));
	}

	private Optional<HttpRequestRecord> findHttpRequestRecord(ExceptionRecord exceptionRecord){
		return httpRequestRecordDao.scanByExceptionRecordIdPrefix(exceptionRecord.getKey().getId())
				.findFirst();
	}

	private static Mav makeExceptionDoesNotExistMav(String exceptionRecordId){
		return new MessageMav("Exception record with id=" + exceptionRecordId + " does not exist");
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
				exceptionGraphLink.getExactMetricLink(exceptionRecord),
				currentUserSessionInfoService.getZoneId(request));
	}

	public static class ExceptionRecordJspDto{

		private final String id;
		private final MilliTime created;
		private final String serverName;
		private final String type;
		private final String appVersion;
		private final String exceptionLocation;
		private final String callOrigin;
		private final String metricLink;
		private final String callOriginLink;
		private final String exactMetricLink;
		private final ZoneId zoneId;

		public ExceptionRecordJspDto(
				String id,
				MilliTime created,
				String serverName,
				String type,
				String appVersion,
				String exceptionLocation,
				String callOrigin,
				String metricLink,
				String callOriginLink,
				String exactMetricLink,
				ZoneId zoneId){
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
			this.zoneId = zoneId;
		}

		public String getId(){
			return id;
		}

		public String getCreated(){
			return created.format(zoneId);
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
				httpRequestRecord.getCreatedAt().toDate(),
				httpRequestRecord.getReceivedAt().toDate(),
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
				httpRequestRecord.getTraceId(),
				httpRequestRecord.getParentId(),
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
		private final String traceId;
		private final String parentId;

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
				String traceId,
				String parentId,
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
			this.traceId = traceId;
			this.parentId = parentId;
			this.headers = headers;
			this.otherHeaders = otherHeaders;
		}

		@SuppressWarnings("deprecation")
		public Map<String,String[]> getOtherHeadersMap(){
			return GsonTool.withUnregisteredEnums().fromJson(
					otherHeaders,
					new TypeToken<Map<String,String[]>>(){}.getType());
		}

		@SuppressWarnings("deprecation")
		public Map<String,String[]> getHttpParamsMap(){
			return GsonTool.withUnregisteredEnums().fromJson(
					httpParams,
					new TypeToken<Map<String,String[]>>(){}.getType());
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

		public String getPrettyPrintedJsonBody(){
			try{
				return GsonTool.prettyPrint(getStringBody());
			}catch(Exception e){
				return null;
			}
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

		public String getTraceId(){
			return traceId;
		}

		public String getParentId(){
			return parentId;
		}

	}

}
