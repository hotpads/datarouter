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
package io.datarouter.web.handler.params;

import java.io.File;

import org.apache.commons.fileupload.disk.DiskFileItem;

import io.datarouter.util.string.StringTool;

//allows defaultCharset customization
@SuppressWarnings("serial")
class DatarouterDiskFileItem extends DiskFileItem{

	private String defaultCharset = DiskFileItem.DEFAULT_CHARSET;

	public DatarouterDiskFileItem(String fieldName, String contentType, boolean isFormField, String fileName,
			int sizeThreshold, File repository){
		super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
	}

	@Override
	public String getCharSet(){
		String charset = super.getCharSet();
		return StringTool.notEmpty(charset) ? charset : defaultCharset;
	}

	public void setDefaultCharset(String defaultCharset){
		this.defaultCharset = defaultCharset;
	}

	@Override
	public File getTempFile(){
		return super.getTempFile();
	}
}
