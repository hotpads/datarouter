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
package io.datarouter.changelog.storage;

import java.util.List;

import io.datarouter.instrumentation.changelog.ChangelogDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;

public class Changelog extends BaseDatabean<ChangelogKey,Changelog>{

	private String action;
	private String username;
	private String comment;

	public static class FieldKeys{
		public static final StringFieldKey action = new StringFieldKey("action");
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final StringFieldKey comment = new StringFieldKey("comment")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);;
	}

	public static class ChangelogFielder extends BaseDatabeanFielder<ChangelogKey,Changelog>{

		public ChangelogFielder(){
			super(ChangelogKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Changelog databean){
			return List.of(
					new StringField(FieldKeys.action, databean.action),
					new StringField(FieldKeys.username, databean.username),
					new StringField(FieldKeys.comment, databean.comment));
		}

	}

	public Changelog(){
		super(new ChangelogKey());
	}

	public Changelog(ChangelogDto dto){
		super(new ChangelogKey(dto.getReversedDateMs(), dto.changelogType, dto.name));
		this.action = dto.action;
		this.username = dto.username;
		this.comment = dto.comment;
	}

	@Override
	public Class<ChangelogKey> getKeyClass(){
		return ChangelogKey.class;
	}

	public String getAction(){
		return action;
	}

	public String getUsername(){
		return username;
	}

}