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
package io.datarouter.auth.storage.userhistory;

import java.time.Instant;
import java.util.List;

import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;

public class DatarouterUserHistory extends BaseDatabean<DatarouterUserHistoryKey,DatarouterUserHistory>{

	private Long editor;
	private DatarouterUserChangeType changeType;
	private String changes;

	public DatarouterUserHistory(){
		super(new DatarouterUserHistoryKey());
	}

	public DatarouterUserHistory(
			Long userId,
			Instant time,
			Long editor,
			DatarouterUserChangeType changeType,
			String changes){
		super(new DatarouterUserHistoryKey(userId, time));
		this.editor = editor;
		this.changeType = changeType;
		this.changes = changes;
	}


	public static class FieldKeys{
		public static final LongFieldKey editor = new LongFieldKey("editor");
		public static final StringEnumFieldKey<DatarouterUserChangeType> changeType =
				new StringEnumFieldKey<>("changeType", DatarouterUserChangeType.class);
		public static final StringFieldKey changes = new StringFieldKey("changes")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
	}

	public static class DatarouterUserHistoryFielder
	extends BaseDatabeanFielder<DatarouterUserHistoryKey,DatarouterUserHistory>{

		public DatarouterUserHistoryFielder(){
			super(DatarouterUserHistoryKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUserHistory databean){
			return List.of(
					new LongField(FieldKeys.editor, databean.editor),
					new StringEnumField<>(FieldKeys.changeType, databean.changeType),
					new StringField(FieldKeys.changes, databean.changes));
		}
	}

	@Override
	public Class<DatarouterUserHistoryKey> getKeyClass(){
		return DatarouterUserHistoryKey.class;
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

	public DatarouterPermissionRequest resolvePermissionRequest(DatarouterPermissionRequest permissionRequest){
		permissionRequest.changeUser(this);
		return permissionRequest;
	}

	public enum DatarouterUserChangeType implements StringEnum<DatarouterUserChangeType>{
		CREATE("create"),//user created
		EDIT("edit"),//changes to roles or flags
		RESET("reset"),//any kind of password/key reset
		DEPROVISION("deprovision"),//user deprovisioned
		RESTORE("restore"),//user restored after being deprovisioned
		INFO("info"),//no change to this user
		;

		private final String persistentString;

		DatarouterUserChangeType(String persistentString){
			this.persistentString = persistentString;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		@Override
		public DatarouterUserChangeType fromPersistentString(String str){
			return DatarouterEnumTool.getEnumFromString(values(), str, null);
		}
	}
}
