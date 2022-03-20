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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.field.imp.list.DelimitedStringListField;
import io.datarouter.model.field.imp.list.DelimitedStringListFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class LoggerConfig extends BaseDatabean<LoggerConfigKey,LoggerConfig>{

	private LoggingLevel level;
	private Boolean additive;
	private List<String> appendersRef;
	private String email;
	private Date lastUpdated;
	private Long ttlMillis;

	private static class FieldKeys{
		private static final StringEnumFieldKey<LoggingLevel> level = new StringEnumFieldKey<>("level",
				LoggingLevel.class)
				.withSize(LoggingLevel.getSqlSize());
		private static final BooleanFieldKey additive = new BooleanFieldKey("additive");
		private static final DelimitedStringListFieldKey appendersRef = new DelimitedStringListFieldKey(
				"appendersRef", ",");
		private static final StringFieldKey email = new StringFieldKey("email");
		@SuppressWarnings("deprecation")
		private static final DateFieldKey lastUpdated = new DateFieldKey("lastUpdated");
		private static final LongFieldKey ttlMillis = new LongFieldKey("ttlMillis");
	}

	public static class LoggerConfigFielder extends BaseDatabeanFielder<LoggerConfigKey,LoggerConfig>{

		public LoggerConfigFielder(){
			super(LoggerConfigKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(LoggerConfig loggerConfig){
			return List.of(
					new StringEnumField<>(FieldKeys.level, loggerConfig.level),
					new BooleanField(FieldKeys.additive, loggerConfig.additive),
					new DelimitedStringListField(FieldKeys.appendersRef, loggerConfig.appendersRef),
					new StringField(FieldKeys.email, loggerConfig.email),
					new DateField(FieldKeys.lastUpdated, loggerConfig.lastUpdated),
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
			Date lastUpdated,
			Long ttlMillis){
		super(new LoggerConfigKey(name));
		this.level = level;
		this.additive = additive;
		this.appendersRef = appendersRef;
		this.email = email;
		this.lastUpdated = lastUpdated;
		this.ttlMillis = ttlMillis;
	}

	public LoggerConfig(
			String name,
			Level level,
			boolean additive,
			List<String> appendersRef,
			String email,
			Date lastUpdated,
			Long ttlMillis){
		this(name, LoggingLevel.fromString(level.name()), additive, appendersRef, email, lastUpdated, ttlMillis);
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

	public Instant getLastUpdated(){
		return Optional.ofNullable(lastUpdated)
				.map(Date::toInstant)
				.orElse(null);
	}

	public LoggingLevel getLevel(){
		return level;
	}

	public void setLevel(LoggingLevel level){
		this.level = level;
	}

	public String getName(){
		return getKey().getName();
	}

	public void setTtlMillis(Long ttlMillis){
		this.ttlMillis = ttlMillis;
	}

	public Long getTtlMillis(){
		return ttlMillis;
	}

}
