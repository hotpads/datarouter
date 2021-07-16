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
package io.datarouter.auth.storage.permissionrequest;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.auth.storage.userhistory.DatarouterUserHistory;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryKey;
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

	private String requestText;
	private DatarouterPermissionRequestResolution resolution;
	private Date resolutionTime;

	public static final Comparator<DatarouterPermissionRequest> REVERSE_CHRONOLOGICAL_COMPARATOR = Comparator
			.comparing(request -> request.getKey().getRequestTime(), Comparator.reverseOrder());

	public DatarouterPermissionRequest(){
		super(new DatarouterPermissionRequestKey());
	}

	public DatarouterPermissionRequest(
			Long userId,
			Date requestTime,
			String requestText,
			DatarouterPermissionRequestResolution resolution,
			Date resolutionTime){
		super(new DatarouterPermissionRequestKey(userId, requestTime));
		this.requestText = requestText;
		this.resolution = resolution;
		this.resolutionTime = resolutionTime;
	}

	public static class FieldKeys{
		public static final StringFieldKey requestText = new StringFieldKey("requestText")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEnumFieldKey<DatarouterPermissionRequestResolution> resolution =
				new StringEnumFieldKey<>("resolution", DatarouterPermissionRequestResolution.class);
		@SuppressWarnings("deprecation")
		public static final DateFieldKey resolutionTime = new DateFieldKey("resolutionTime");
	}

	public static class DatarouterPermissionRequestFielder
	extends BaseDatabeanFielder<DatarouterPermissionRequestKey,DatarouterPermissionRequest>{
		public DatarouterPermissionRequestFielder(){
			super(DatarouterPermissionRequestKey.class);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterPermissionRequest databean){
			return List.of(
					new StringField(FieldKeys.requestText, databean.requestText),
					new StringEnumField<>(FieldKeys.resolution, databean.resolution),
					new DateField(FieldKeys.resolutionTime, databean.resolutionTime));
		}
	}

	public Optional<DatarouterUserHistoryKey> toUserHistoryKey(){
		return Optional.ofNullable(resolutionTime)
				.map(Date::toInstant)
				.map(time -> new DatarouterUserHistoryKey(getKey().getUserId(), time));
	}

	private DatarouterPermissionRequest resolve(DatarouterPermissionRequestResolution resolution,
			Optional<Instant> resolutionTime){
		setResolution(resolution);
		setResolutionTime(resolutionTime.orElse(null));
		return this;
	}

	public DatarouterPermissionRequest changeUser(DatarouterUserHistory change){
		return resolve(DatarouterPermissionRequestResolution.USER_CHANGED, Optional.of(change.getKey().getTime()));
	}

	public DatarouterPermissionRequest supercede(){
		return resolve(DatarouterPermissionRequestResolution.SUPERCEDED, Optional.of(Instant.now()));
	}

	public DatarouterPermissionRequest decline(){
		return decline(Instant.now());
	}

	public DatarouterPermissionRequest decline(Instant time){
		return resolve(DatarouterPermissionRequestResolution.DECLINED, Optional.ofNullable(time));
	}

	@Override
	public Supplier<DatarouterPermissionRequestKey> getKeySupplier(){
		return DatarouterPermissionRequestKey::new;
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

	public Optional<Instant> getResolutionTime(){
		return Optional.ofNullable(resolutionTime)
				.map(Date::toInstant);
	}

	public void setResolutionTime(Instant resolutionTime){
		this.resolutionTime = Date.from(resolutionTime);
	}

	public enum DatarouterPermissionRequestResolution implements StringEnum<DatarouterPermissionRequestResolution>{
		SUPERCEDED("superceded"),//another request was made, so this one is no longer relevant
		USER_CHANGED("changed"),//user was changed since request
		DECLINED("declined");//request was manually declined

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
