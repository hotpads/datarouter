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
package io.datarouter.auth.storage.user.permissionrequest;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLog;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLogKey;
import io.datarouter.enums.StringMappedEnum;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeToLongFieldCodec;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;

public class PermissionRequest extends BaseDatabean<PermissionRequestKey,PermissionRequest>{

	private String requestText;
	private DatarouterPermissionRequestResolution resolution;
	private MilliTime resolutionTime;

	public static final Comparator<PermissionRequest> REVERSE_CHRONOLOGICAL_COMPARATOR = Comparator
			.comparing(request -> request.getKey().getRequestTime(), Comparator.reverseOrder());

	public PermissionRequest(){
		super(new PermissionRequestKey());
	}

	public PermissionRequest(
			Long userId,
			MilliTime requestTime,
			String requestText,
			DatarouterPermissionRequestResolution resolution,
			MilliTime resolutionTime){
		super(new PermissionRequestKey(userId, requestTime));
		this.requestText = requestText;
		this.resolution = resolution;
		this.resolutionTime = resolutionTime;
	}

	public static class FieldKeys{
		public static final StringFieldKey requestText = new StringFieldKey("requestText")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringEncodedFieldKey<DatarouterPermissionRequestResolution> resolution =
				new StringEncodedFieldKey<>(
				"resolution",
				new StringMappedEnumFieldCodec<>(DatarouterPermissionRequestResolution.BY_PERSISTENT_STRING));
		public static final LongEncodedFieldKey<MilliTime> resolutionTime = new LongEncodedFieldKey<>("resolutionTime",
				new MilliTimeToLongFieldCodec());
	}

	public static class PermissionRequestFielder
	extends BaseDatabeanFielder<PermissionRequestKey,PermissionRequest>{

		public PermissionRequestFielder(){
			super(PermissionRequestKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(PermissionRequest databean){
			return List.of(
					new StringField(FieldKeys.requestText, databean.requestText),
					new StringEncodedField<>(FieldKeys.resolution, databean.resolution),
					new LongEncodedField<>(FieldKeys.resolutionTime, databean.resolutionTime));
		}

	}

	public Optional<DatarouterUserHistoryLogKey> toUserHistoryKey(){
		return Optional.ofNullable(resolutionTime)
				.map(time -> new DatarouterUserHistoryLogKey(getKey().getUserId(), time));
	}

	private PermissionRequest resolve(
			DatarouterPermissionRequestResolution resolution,
			Optional<Instant> resolutionTime){
		setResolution(resolution);
		setResolutionTime(resolutionTime.orElse(null));
		return this;
	}

	public PermissionRequest changeUser(DatarouterUserHistoryLog change){
		return resolve(DatarouterPermissionRequestResolution.USER_CHANGED, Optional.of(change.getKey().getTime()));
	}

	public PermissionRequest supercede(){
		return resolve(DatarouterPermissionRequestResolution.SUPERCEDED, Optional.of(Instant.now()));
	}

	public PermissionRequest expire(){
		return resolve(DatarouterPermissionRequestResolution.EXPIRED, Optional.ofNullable(Instant.now()));
	}

	public PermissionRequest decline(Instant time){
		return resolve(DatarouterPermissionRequestResolution.DECLINED, Optional.ofNullable(time));
	}

	@Override
	public Supplier<PermissionRequestKey> getKeySupplier(){
		return PermissionRequestKey::new;
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
				.map(MilliTime::toInstant);
	}

	public void setResolutionTime(Instant resolutionTime){
		this.resolutionTime = MilliTime.of(resolutionTime);
	}

	public enum DatarouterPermissionRequestResolution{
		SUPERCEDED("superceded"),//another request was made, so this one is no longer relevant
		USER_CHANGED("changed"),//user was changed since request
		DECLINED("declined"), //request was manually declined
		EXPIRED("expired"),
		;

		public static final StringMappedEnum<DatarouterPermissionRequestResolution> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		public final String persistentString;

		DatarouterPermissionRequestResolution(String persistentString){
			this.persistentString = persistentString;
		}

	}
}
