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
package io.datarouter.web.storage.payloadsampling.request;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.ByteArrayFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;
import io.datarouter.web.filter.payloadsampling.PayloadSamplingFilter;
import io.datarouter.web.storage.payloadsampling.PayloadSampleKey;

public class RequestPayloadSample extends BaseDatabean<PayloadSampleKey,RequestPayloadSample>{

	private String parameterMap;
	private byte[] binaryBody;
	private String encoding;
	private MilliTime lastUpdated;

	public static class FieldKeys{
		public static final StringFieldKey parameterMap = new StringFieldKey("parameterMap")
				.withSize(PayloadSamplingFilter.REQUEST_PARAM_MAP_JSON_LEN);
		public static final ByteArrayFieldKey binaryBody = new ByteArrayFieldKey("binaryBody")
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final StringFieldKey encoding = new StringFieldKey("encoding");
		public static final LongEncodedFieldKey<MilliTime> lastUpdated = new LongEncodedFieldKey<>("lastUpdated",
				new MilliTimeFieldCodec());
	}

	public static class RequestPayloadSampleFielder extends
			BaseDatabeanFielder<PayloadSampleKey,RequestPayloadSample>{

		public RequestPayloadSampleFielder(){
			super(PayloadSampleKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(RequestPayloadSample databean){
			return List.of(
					new StringField(FieldKeys.parameterMap, databean.parameterMap),
					new ByteArrayField(RequestPayloadSample.FieldKeys.binaryBody, databean.binaryBody),
					new StringField(FieldKeys.encoding, databean.encoding),
					new LongEncodedField<>(RequestPayloadSample.FieldKeys.lastUpdated, databean.lastUpdated));
		}

	}

	public RequestPayloadSample(){
		super(new PayloadSampleKey());
	}

	public RequestPayloadSample(PayloadSampleKey key,
			String parameterMap,
			byte[] binaryBody,
			String encoding){
		super(key);
		this.parameterMap = parameterMap;
		this.binaryBody = binaryBody;
		this.encoding = encoding;
		this.lastUpdated = MilliTime.now();
	}

	@Override
	public Supplier<PayloadSampleKey> getKeySupplier(){
		return PayloadSampleKey::new;
	}

	public String getParameterMap(){
		return parameterMap;
	}

	public byte[] getBinaryBody(){
		return binaryBody;
	}

	public String getEncoding(){
		return encoding;
	}

	public MilliTime getLastUpdated(){
		return lastUpdated;
	}

}
