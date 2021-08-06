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
package io.datarouter.conveyor.message;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.storage.serialize.codec.FlatKeyJsonDatabeanCodec;

/*
 * Useful databean for wrapping dtos to use in conveyors
 */
public class ConveyorMessage extends BaseDatabean<ConveyorMessageKey,ConveyorMessage>{

	private String message;

	private static class FieldKeys{
		private static final StringFieldKey message = new StringFieldKey("message").withColumnName("Message");
	}

	public static class ConveyorMessageFielder extends BaseDatabeanFielder<ConveyorMessageKey,ConveyorMessage>{

		public ConveyorMessageFielder(){
			super(ConveyorMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ConveyorMessage databean){
			return List.of(new StringField(FieldKeys.message, databean.message));
		}

		@Override
		public Class<? extends StringDatabeanCodec> getStringDatabeanCodecClass(){
			return FlatKeyJsonDatabeanCodec.class;
		}

	}

	public ConveyorMessage(){
		super(new ConveyorMessageKey());
	}

	public ConveyorMessage(String messageId, String message){
		super(new ConveyorMessageKey(messageId));
		this.message = message;
	}

	@Override
	public Supplier<ConveyorMessageKey> getKeySupplier(){
		return ConveyorMessageKey::new;
	}

	public String getMessage(){
		return message;
	}

}
