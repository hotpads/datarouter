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
package io.datarouter.storage.queue;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.serialize.codec.FlatKeyJsonDatabeanCodec;

public class StringQueueMessage extends BaseDatabean<StringQueueMessageKey,StringQueueMessage>{

	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message")
				.withColumnName("Message")
				.withSize(CommonFieldSizes.MAX_SQS_SIZE)
				.disableInvalidSizeLogging()
				.disableSizeValidation();
	}

	public static class StringQueueMessageFielder
	extends BaseDatabeanFielder<StringQueueMessageKey,StringQueueMessage>{

		public StringQueueMessageFielder(){
			super(StringQueueMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(StringQueueMessage sqsMessage){
			return List.of(new StringField(FieldKeys.message, sqsMessage.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	//be careful with message size when using this fielder (if too big, exceptions will be thrown)
	public static class UnlimitedSizeStringQueueMessageFielder
	extends BaseDatabeanFielder<StringQueueMessageKey,StringQueueMessage>{

		public UnlimitedSizeStringQueueMessageFielder(){
			super(StringQueueMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(StringQueueMessage databean){
			return List.of(new StringField(FieldKeys.message.withSize(Integer.MAX_VALUE), databean.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public StringQueueMessage(){
		super(new StringQueueMessageKey());
	}

	public StringQueueMessage(String messageId, String message){
		super(new StringQueueMessageKey(messageId));
		this.message = message;
	}

	@Override
	public Supplier<StringQueueMessageKey> getKeySupplier(){
		return StringQueueMessageKey::new;
	}

	public String getMessage(){
		return message;
	}

}
