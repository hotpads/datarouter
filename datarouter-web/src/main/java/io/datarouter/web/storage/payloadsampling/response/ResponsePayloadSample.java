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
package io.datarouter.web.storage.payloadsampling.response;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSample;
import io.datarouter.web.storage.payloadsampling.request.RequestPayloadSample.FieldKeys;

public class ResponsePayloadSample extends BaseDatabean<PayloadSampleKey,ResponsePayloadSample>{

	private byte[] binaryBody;
	private String encoding;
	private MilliTime lastUpdated;

	public static class ResponsePayloadSampleFielder extends
			BaseDatabeanFielder<PayloadSampleKey,ResponsePayloadSample>{

		public ResponsePayloadSampleFielder(){
			super(PayloadSampleKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ResponsePayloadSample databean){
			return List.of(
					new ByteArrayField(RequestPayloadSample.FieldKeys.binaryBody, databean.binaryBody),
					new StringField(FieldKeys.encoding, databean.encoding),
					new LongEncodedField<>(RequestPayloadSample.FieldKeys.lastUpdated, databean.lastUpdated));
		}

	}

	public ResponsePayloadSample(){
		super(new PayloadSampleKey());
	}

	public ResponsePayloadSample(PayloadSampleKey key,
			byte[] binaryBody,
			String encoding){
		super(key);
		this.binaryBody = binaryBody;
		this.encoding = encoding;
		this.lastUpdated = MilliTime.now();
	}

	@Override
	public Supplier<PayloadSampleKey> getKeySupplier(){
		return PayloadSampleKey::new;
	}

	public byte[] getBinaryBody(){
		return binaryBody;
	}

	public String getEncoding(){
		return encoding;
	}

}
