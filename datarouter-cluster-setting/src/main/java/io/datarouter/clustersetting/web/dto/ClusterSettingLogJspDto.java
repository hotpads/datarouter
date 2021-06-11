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
package io.datarouter.clustersetting.web.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import io.datarouter.clustersetting.ClusterSettingLogAction;
import io.datarouter.clustersetting.ClusterSettingScope;
import io.datarouter.clustersetting.storage.clustersettinglog.ClusterSettingLog;
import io.datarouter.util.enums.StringEnum;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.ZonedDateFormaterTool;

public class ClusterSettingLogJspDto{

	private static final String TABLE_CELL_DEFAULT = "&nbsp;";

	private final String name;
	private final Date created;
	private final ClusterSettingScope scope;
	private final String serverType;
	private final String serverName;
	private final String value;
	private final ClusterSettingLogAction action;
	private final String changedBy;
	private final String comment;

	private final ZoneId zoneId;

	public ClusterSettingLogJspDto(ClusterSettingLog clusterSettingLog, ZoneId zoneId){
		this.name = clusterSettingLog.getKey().getName();
		this.created = clusterSettingLog.getKey().getCreated();
		this.scope = clusterSettingLog.getScope();
		this.serverType = clusterSettingLog.getServerType();
		this.serverName = clusterSettingLog.getServerName();
		this.value = clusterSettingLog.getValue();
		this.action = clusterSettingLog.getAction();
		this.changedBy = clusterSettingLog.getChangedBy();
		this.comment = clusterSettingLog.getComment();

		this.zoneId = zoneId;
	}

	public String getName(){
		return name;
	}

	public String[] getNameParts(){
		return name.split("\\.");
	}

	public String getCreated(){
		return ZonedDateFormaterTool.formatDateWithZone(created, zoneId);
	}

	public String getCreatedIsoLocalDateTime(){
		return ZonedDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public ClusterSettingScope getScope(){
		return scope;
	}

	public String getServerType(){
		return serverType;
	}

	public String getServerName(){
		return serverName;
	}

	public String getValue(){
		return value;
	}

	public ClusterSettingLogAction getAction(){
		return action;
	}

	public String getChangedBy(){
		return changedBy;
	}

	public String getComment(){
		return comment;
	}

	public String getHtmlSafeScope(){
		return htmlSafe(scope);
	}

	public String getHtmlSafeServerType(){
		return htmlSafe(serverType);
	}

	public String getHtmlSafeServerName(){
		return htmlSafe(serverName);
	}

	public String getHtmlSafeValue(){
		return htmlSafe(value);
	}

	public String getHtmlSafeAction(){
		return htmlSafe(action);
	}

	public String getHtmlSafeChangedBy(){
		return htmlSafe(changedBy);
	}

	public String getHtmlSafeComment(){
		return htmlSafe(comment);
	}

	private String htmlSafe(String str){
		return StringTool.isNullOrEmpty(str) ? TABLE_CELL_DEFAULT : str;
	}

	private <T extends StringEnum<T>> String htmlSafe(T stringEnum){
		return stringEnum.getPersistentString() == null ? TABLE_CELL_DEFAULT : stringEnum.toString();
	}

}
