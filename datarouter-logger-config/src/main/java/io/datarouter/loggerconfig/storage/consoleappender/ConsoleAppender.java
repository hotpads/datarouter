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
package io.datarouter.loggerconfig.storage.consoleappender;

import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class ConsoleAppender extends BaseDatabean<ConsoleAppenderKey,ConsoleAppender>{

	private String layout;
	private String target;

	private static class FieldKeys{
		private static final StringFieldKey layout = new StringFieldKey("layout");
		private static final StringFieldKey target = new StringFieldKey("target");
	}

	public static class ConsoleAppenderFielder extends BaseDatabeanFielder<ConsoleAppenderKey,ConsoleAppender>{

		public ConsoleAppenderFielder(){
			super(ConsoleAppenderKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ConsoleAppender consoleAppender){
			return List.of(
					new StringField(FieldKeys.layout, consoleAppender.layout),
					new StringField(FieldKeys.target, consoleAppender.target));
		}

	}

	public ConsoleAppender(){
		super(new ConsoleAppenderKey());
	}

	public ConsoleAppender(String name, String layout, Target target){
		super(new ConsoleAppenderKey(name));
		this.layout = layout;
		this.target = target.name();
	}

	@Override
	public Supplier<ConsoleAppenderKey> getKeySupplier(){
		return ConsoleAppenderKey::new;
	}

	public String getLayout(){
		return layout;
	}

	public Target getTarget(){
		return Target.valueOf(target);
	}

}
