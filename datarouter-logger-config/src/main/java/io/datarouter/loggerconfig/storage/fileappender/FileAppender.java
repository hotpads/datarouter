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
package io.datarouter.loggerconfig.storage.fileappender;

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class FileAppender extends BaseDatabean<FileAppenderKey,FileAppender>{

	private String layout;
	private String fileName;

	public static class FieldKeys{
		public static final StringFieldKey layout = new StringFieldKey("layout");
		public static final StringFieldKey fileName = new StringFieldKey("fileName");
	}

	public static class FileAppenderFielder extends BaseDatabeanFielder<FileAppenderKey,FileAppender>{

		public FileAppenderFielder(){
			super(FileAppenderKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(FileAppender appender){
			return Arrays.asList(
					new StringField(FieldKeys.layout, appender.layout),
					new StringField(FieldKeys.fileName, appender.fileName));
		}

	}

	public FileAppender(){
		super(new FileAppenderKey());
	}

	public FileAppender(String name, String layout, String fileName){
		super(new FileAppenderKey(name));
		this.layout = layout;
		this.fileName = fileName;
	}

	@Override
	public Class<FileAppenderKey> getKeyClass(){
		return FileAppenderKey.class;
	}

	public String getLayout(){
		return layout;
	}

	public void setLayout(String layout){
		this.layout = layout;
	}

	public String getFileName(){
		return fileName;
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public String getName(){
		return getKey().getName();
	}

}
