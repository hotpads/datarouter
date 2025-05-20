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
package io.datarouter.auth.storage.user.userhistory;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.enums.StringMappedEnum;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;

public class DatarouterUserHistoryLog extends BaseDatabean<DatarouterUserHistoryLogKey,DatarouterUserHistoryLog>{

	private Long editor;
	private DatarouterUserChangeType changeType;
	private String changes;

	public DatarouterUserHistoryLog(){
		super(new DatarouterUserHistoryLogKey());
	}

	public DatarouterUserHistoryLog(
			Long userId,
			MilliTime time,
			Long editor,
			DatarouterUserChangeType changeType,
			String changes){
		super(new DatarouterUserHistoryLogKey(userId, time));
		this.editor = editor;
		this.changeType = changeType;
		this.changes = changes;
	}


	public static class FieldKeys{
		public static final LongFieldKey editor = new LongFieldKey("editor");
		public static final StringEncodedFieldKey<DatarouterUserChangeType> changeType = new StringEncodedFieldKey<>(
				"changeType",
				new StringMappedEnumFieldCodec<>(DatarouterUserChangeType.BY_PERSISTENT_STRING));
		public static final StringFieldKey changes = new StringFieldKey("changes")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
	}

	public static class DatarouterUserHistoryLogFielder
	extends BaseDatabeanFielder<DatarouterUserHistoryLogKey,DatarouterUserHistoryLog>{

		public DatarouterUserHistoryLogFielder(){
			super(DatarouterUserHistoryLogKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUserHistoryLog databean){
			return List.of(
					new LongField(FieldKeys.editor, databean.editor),
					new StringEncodedField<>(FieldKeys.changeType, databean.changeType),
					new StringField(FieldKeys.changes, databean.changes));
		}
	}

	@Override
	public Supplier<DatarouterUserHistoryLogKey> getKeySupplier(){
		return DatarouterUserHistoryLogKey::new;
	}

	public Long getEditor(){
		return editor;
	}

	public DatarouterUserChangeType getChangeType(){
		return changeType;
	}

	public String getChanges(){
		return changes;
	}

	public void setChanges(String changes){
		this.changes = changes;
	}

	public PermissionRequest resolvePermissionRequest(PermissionRequest permissionRequest){
		permissionRequest.changeUser(this);
		return permissionRequest;
	}

	public enum DatarouterUserChangeType{
		CREATE("create"),//user created
		DEPROVISION("deprovision"),//user deprovisioned
		EDIT("edit"),//changes to roles or flags
		INFO("info"),//no change to this user
		RESET("reset"),//any kind of password/key/permission reset
		RESTORE("restore"),//user restored after being deprovisioned
		SAML("saml"),//permission changes detected due to SAML assertions
		;

		public static final StringMappedEnum<DatarouterUserChangeType> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		public final String persistentString;

		DatarouterUserChangeType(String persistentString){
			this.persistentString = persistentString;
		}

	}

}
