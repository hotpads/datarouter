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
package io.datarouter.email.dto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource{

	private final Supplier<InputStream> attachmentInputStreamSupplier;
	private final String contentType;
	private final String fileName;

	public InputStreamDataSource(DatarouterEmailFileAttachmentDto emailFileAttachmentDto){
		this.attachmentInputStreamSupplier = emailFileAttachmentDto.attachmentInputStreamSupplier;
		this.contentType = emailFileAttachmentDto.contentType;
		this.fileName = emailFileAttachmentDto.fileName;
	}

	@Override
	public InputStream getInputStream(){
		return attachmentInputStreamSupplier.get();
	}

	@Override
	public OutputStream getOutputStream(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType(){
		return contentType;
	}

	@Override
	public String getName(){
		return fileName;
	}

}
