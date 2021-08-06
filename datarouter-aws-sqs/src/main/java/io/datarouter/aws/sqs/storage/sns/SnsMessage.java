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
package io.datarouter.aws.sqs.storage.sns;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.serialize.codec.FlatKeyJsonDatabeanCodec;

public class SnsMessage extends BaseDatabean<SnsMessageKey,SnsMessage>{

	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message").withColumnName("Message");
	}

	public static class SnsMessageFielder extends BaseDatabeanFielder<SnsMessageKey,SnsMessage>{

		public SnsMessageFielder(){
			super(SnsMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(SnsMessage snsMessage){
			return List.of(new StringField(FieldKeys.message, snsMessage.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public SnsMessage(){
		super(new SnsMessageKey());
	}

	@Override
	public Supplier<SnsMessageKey> getKeySupplier(){
		return SnsMessageKey::new;
	}

	public String getMessage(){
		return message;
	}

}
