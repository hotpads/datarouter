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
package io.datarouter.web.user.databean;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;

public class DatarouterPermissionRequest
extends BaseDatabean<DatarouterPermissionRequestKey,DatarouterPermissionRequest>{

	private DatarouterPermissionRequestKey key;
	private String requestText;
	private DatarouterPermissionRequestResolution resolution;
	private Date resolutionTime;

	public static final Comparator<DatarouterPermissionRequest> REVERSE_CHRONOLOGICAL_COMPARATOR = Comparator
			.comparing(request -> request.getKey().getRequestTime(), Comparator.reverseOrder());

	public DatarouterPermissionRequest(){
		this.key = new DatarouterPermissionRequestKey();
	}

	public DatarouterPermissionRequest(Long userId, Date requestTime, String requestText,
			DatarouterPermissionRequestResolution resolution, Date resolutionTime){
		this.key = new DatarouterPermissionRequestKey(userId, requestTime);
		this.requestText = requestText;
		this.resolution = resolution;
		this.resolutionTime = resolutionTime;
	}

	public static class FieldKeys{
		public static final StringFieldKey requestText = new StringFieldKey("requestText")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEnumFieldKey<DatarouterPermissionRequestResolution> resolution =
				new StringEnumFieldKey<>("resolution", DatarouterPermissionRequestResolution.class);
		public static final DateFieldKey resolutionTime = new DateFieldKey("resolutionTime");
	}

	public static class DatarouterPermissionRequestFielder
	extends BaseDatabeanFielder<DatarouterPermissionRequestKey,DatarouterPermissionRequest>{
		public DatarouterPermissionRequestFielder(){
			super(DatarouterPermissionRequestKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterPermissionRequest databean){
			return Arrays.asList(
					new StringField(FieldKeys.requestText, databean.requestText),
					new StringEnumField<>(FieldKeys.resolution, databean.resolution),
					new DateField(FieldKeys.resolutionTime, databean.resolutionTime));
		}
	}

	public DatarouterUserHistoryKey toUserHistoryKey(){
		Objects.requireNonNull(resolutionTime);
		return new DatarouterUserHistoryKey(getKey().getUserId(), resolutionTime);
	}

	private DatarouterPermissionRequest resolve(DatarouterPermissionRequestResolution resolution, Date resolutionTime){
		setResolution(resolution);
		setResolutionTime(resolutionTime);
		return this;
	}

	public DatarouterPermissionRequest changeUser(DatarouterUserHistory change){
		return resolve(DatarouterPermissionRequestResolution.USER_CHANGED, change.getKey().getTime());
	}

	public DatarouterPermissionRequest supercede(){
		return resolve(DatarouterPermissionRequestResolution.SUPERCEDED, new Date());
	}

	public DatarouterPermissionRequest deny(){
		return resolve(DatarouterPermissionRequestResolution.DENIED, new Date());
	}

	@Override
	public Class<DatarouterPermissionRequestKey> getKeyClass(){
		return DatarouterPermissionRequestKey.class;
	}

	@Override
	public DatarouterPermissionRequestKey getKey(){
		return key;
	}

	public String getRequestText(){
		return requestText;
	}

	public void setRequestText(String requestText){
		this.requestText = requestText;
	}

	public DatarouterPermissionRequestResolution getResolution(){
		return resolution;
	}

	public void setResolution(DatarouterPermissionRequestResolution resolution){
		this.resolution = resolution;
	}

	public Date getResolutionTime(){
		return resolutionTime;
	}

	public void setResolutionTime(Date resolutionTime){
		this.resolutionTime = resolutionTime;
	}

	public enum DatarouterPermissionRequestResolution implements StringEnum<DatarouterPermissionRequestResolution>{
		SUPERCEDED("superceded"),//another request was made, so this one is no longer relevant
		USER_CHANGED("changed"),//user was changed since request
		DENIED("denied");//request was manually denied

		private final String persistentString;

		DatarouterPermissionRequestResolution(String persistentString){
			this.persistentString = persistentString;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		@Override
		public DatarouterPermissionRequestResolution fromPersistentString(String str){
			return DatarouterEnumTool.getEnumFromString(values(), str, null);
		}
	}
}
