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
package io.datarouter.aws.sqs.storage.sqs;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.serialize.codec.FlatKeyJsonDatabeanCodec;

public class SqsMessage extends BaseDatabean<SqsMessageKey,SqsMessage>{

	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message")
				.withColumnName("Message");
	}

	public static class SqsMessageFielder extends BaseDatabeanFielder<SqsMessageKey,SqsMessage>{

		public SqsMessageFielder(){
			super(SqsMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SqsMessage sqsMessage){
			return List.of(new StringField(FieldKeys.message, sqsMessage.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public SqsMessage(){
		super(new SqsMessageKey());
	}

	public SqsMessage(String messageId, String message){
		super(new SqsMessageKey(messageId));
		this.message = message;
	}

	@Override
	public Supplier<SqsMessageKey> getKeySupplier(){
		return SqsMessageKey::new;
	}

	public String getMessage(){
		return message;
	}

}
