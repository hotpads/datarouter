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
package io.datarouter.changelog.storage;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class ChangelogKey extends BaseRegularPrimaryKey<ChangelogKey>{

	private Long reversedDateMs;
	private String changelogType;
	private String name;

	public static class FieldKeys{
		public static final LongFieldKey reversedDateMs = new LongFieldKey("reversedDateMs");
		public static final StringFieldKey changelogType = new StringFieldKey("changelogType")
				.withSize(100);
		public static final StringFieldKey name = new StringFieldKey("name");
	}

	public ChangelogKey(){
	}

	public ChangelogKey(Long reversedDateMs, String changelogType, String name){
		this.reversedDateMs = reversedDateMs;
		this.changelogType = changelogType;
		this.name = name;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new LongField(FieldKeys.reversedDateMs, reversedDateMs),
				new StringField(FieldKeys.changelogType, changelogType),
				new StringField(FieldKeys.name, name));
	}

	public Long getReversedDateMs(){
		return reversedDateMs;
	}

	public String getChangelogType(){
		return changelogType;
	}

	public String getName(){
		return name;
	}

}
