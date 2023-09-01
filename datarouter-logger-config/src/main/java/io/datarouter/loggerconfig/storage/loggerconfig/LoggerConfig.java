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
package io.datarouter.loggerconfig.storage.loggerconfig;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.codec.StringListToCsvFieldCodec;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.types.MilliTime;

public class LoggerConfig extends BaseDatabean<LoggerConfigKey,LoggerConfig>{

	private LoggingLevel level;
	private Boolean additive;
	private List<String> appendersRef;
	private String email;
	private MilliTime lastUpdatedMs;
	private Long ttlMillis;

	private static class FieldKeys{
		private static final StringEncodedFieldKey<LoggingLevel> level = new StringEncodedFieldKey<>(
				"level",
				new StringMappedEnumFieldCodec<>(LoggingLevel.BY_PERSISTENT_STRING))
				.withSize(LoggingLevel.BY_PERSISTENT_STRING.maxLength());
		private static final BooleanFieldKey additive = new BooleanFieldKey("additive");
		// StringListToCsvFieldCodec
		private static final StringEncodedFieldKey<List<String>> appendersRef = new StringEncodedFieldKey<>(
				"appendersRef", StringListToCsvFieldCodec.INSTANCE)
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		private static final StringFieldKey email = new StringFieldKey("email");
		private static final LongEncodedFieldKey<MilliTime> lastUpdatedMs = new LongEncodedFieldKey<>(
				"lastUpdatedMs", new MilliTimeFieldCodec());
		private static final LongFieldKey ttlMillis = new LongFieldKey("ttlMillis");
	}

	public static class LoggerConfigFielder extends BaseDatabeanFielder<LoggerConfigKey,LoggerConfig>{

		public LoggerConfigFielder(){
			super(LoggerConfigKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(LoggerConfig loggerConfig){
			return List.of(
					new StringEncodedField<>(FieldKeys.level, loggerConfig.level),
					new BooleanField(FieldKeys.additive, loggerConfig.additive),
					new StringEncodedField<>(FieldKeys.appendersRef, loggerConfig.appendersRef),
					new StringField(FieldKeys.email, loggerConfig.email),
					new LongEncodedField<>(FieldKeys.lastUpdatedMs, loggerConfig.lastUpdatedMs),
					new LongField(FieldKeys.ttlMillis, loggerConfig.ttlMillis));
		}

	}

	public LoggerConfig(){
		super(new LoggerConfigKey());
	}

	public LoggerConfig(
			String name,
			LoggingLevel level,
			boolean additive,
			List<String> appendersRef,
			String email,
			MilliTime lastUpdatedMs,
			Long ttlMillis){
		super(new LoggerConfigKey(name));
		this.level = LoggingLevel.BY_PERSISTENT_STRING.fromOrNull(level.name());
		this.additive = additive;
		this.appendersRef = appendersRef;
		this.email = email;
		this.lastUpdatedMs = lastUpdatedMs;
		this.ttlMillis = ttlMillis;
	}

	@Override
	public Supplier<LoggerConfigKey> getKeySupplier(){
		return LoggerConfigKey::new;
	}

	public Boolean getAdditive(){
		return additive;
	}

	public List<String> getAppendersRef(){
		return appendersRef;
	}

	public String getEmail(){
		return email;
	}

	public MilliTime getLastUpdated(){
		return lastUpdatedMs;
	}

	public LoggingLevel getLevel(){
		return level;
	}

	public void setLevel(LoggingLevel level){
		this.level = level;
	}

	public void setTtlMillis(Long ttlMillis){
		this.ttlMillis = ttlMillis;
	}

	public Long getTtlMillis(){
		return ttlMillis;
	}

}
