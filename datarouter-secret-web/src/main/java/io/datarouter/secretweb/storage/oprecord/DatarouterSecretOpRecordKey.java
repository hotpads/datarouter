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

import java.time.Instant;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.number.RandomTool;

public class DatarouterSecretOpRecordKey extends BaseRegularPrimaryKey<DatarouterSecretOpRecordKey>{

	private Instant date;
	private String namespace;
	private String name;
	private Long random;

	public static class FieldKeys{
		public static final InstantFieldKey date = new InstantFieldKey("date");
		public static final StringFieldKey namespace = new StringFieldKey("namespace");
		public static final StringFieldKey name = new StringFieldKey("name");
		public static final LongFieldKey random = new LongFieldKey("random");
	}

	public DatarouterSecretOpRecordKey(){
	}

	public DatarouterSecretOpRecordKey(String namespace, String name){
		this(Instant.now(), namespace, name);
	}

	public DatarouterSecretOpRecordKey(Instant date, String namespace, String name){
		this.date = date;
		this.namespace = namespace;
		this.name = name;
		this.random = RandomTool.nextPositiveLong();
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new InstantField(FieldKeys.date, date),
				new StringField(FieldKeys.namespace, namespace),
				new StringField(FieldKeys.name, name),
				new LongField(FieldKeys.random, random));
	}

	public Instant getDate(){
		return date;
	}

	public String getNamespace(){
		return namespace;
	}

	public String getName(){
		return name;
	}

	public String getNamespacedName(){
		return namespace + name;
	}

	public Long getRandom(){
		return random;
	}

}
