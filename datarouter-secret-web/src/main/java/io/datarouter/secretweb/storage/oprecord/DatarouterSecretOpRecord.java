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
package io.datarouter.secretweb.storage.oprecord;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.secret.op.SecretOpReason.SecretOpReasonType;
import io.datarouter.secret.op.client.SecretClientOpType;

public class DatarouterSecretOpRecord
extends BaseDatabean<DatarouterSecretOpRecordKey,DatarouterSecretOpRecord>{

	private SecretClientOpType secretOp;
	private SecretOpReasonType secretOpReasonType;
	private String reason;

	public static class FieldKeys{
		public static final StringEncodedFieldKey<SecretClientOpType> secretOp = new StringEncodedFieldKey<>(
				"secretOp",
				new StringMappedEnumFieldCodec<>(SecretClientOpType.BY_PERSISTENT_STRING));
		public static final StringEncodedFieldKey<SecretOpReasonType> secretOpReasonType = new StringEncodedFieldKey<>(
				"secretOpReasonType",
				new StringMappedEnumFieldCodec<>(SecretOpReasonType.BY_PERSISTENT_STRING));
		public static final StringFieldKey opType = new StringFieldKey("opType");
		public static final StringFieldKey opReasonType = new StringFieldKey("opReasonType");
		public static final StringFieldKey reason = new StringFieldKey("reason");
	}

	public DatarouterSecretOpRecord(){
		super(new DatarouterSecretOpRecordKey());
	}

	public DatarouterSecretOpRecord(
			String namespace,
			String name,
			SecretClientOpType secretOp,
			SecretOpReasonType secretOpReasonType,
			String reason){
		super(new DatarouterSecretOpRecordKey(namespace, name));
		this.secretOp = secretOp;
		this.secretOpReasonType = secretOpReasonType;
		this.reason = reason;
	}

	public static class DatarouterSecretOpRecordFielder
	extends BaseDatabeanFielder<DatarouterSecretOpRecordKey,DatarouterSecretOpRecord>{

		public DatarouterSecretOpRecordFielder(){
			super(DatarouterSecretOpRecordKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DatarouterSecretOpRecord databean){
			return List.of(
					new StringEncodedField<>(FieldKeys.secretOp, databean.secretOp),
					new StringEncodedField<>(FieldKeys.secretOpReasonType, databean.secretOpReasonType),
					new StringField(FieldKeys.reason, databean.reason));
		}

	}

	@Override
	public Supplier<DatarouterSecretOpRecordKey> getKeySupplier(){
		return DatarouterSecretOpRecordKey::new;
	}

	public SecretClientOpType getSecretOp(){
		return secretOp;
	}

	public SecretOpReasonType getSecretOpReasonType(){
		return secretOpReasonType;
	}

	public String getReason(){
		return reason;
	}

}
