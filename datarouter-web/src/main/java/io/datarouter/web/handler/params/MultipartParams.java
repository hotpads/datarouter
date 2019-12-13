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

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.util.number.NumberFormatter;

public class MultipartParams extends Params{

	private final HttpServletRequest request;
	private final Map<String,FileItem> filesMap;
	private final List<FileItem> fileItems;
	private final String defaultCharset;

	public MultipartParams(HttpServletRequest request, Charset defaultCharset) throws FileUploadException{
		super(request);
		this.request = request;
		this.filesMap = new LinkedHashMap<>();
		this.defaultCharset = defaultCharset == null ? null : defaultCharset.displayName();
		try(var $ = TracerTool.startSpan("read multipart")){
			TracerTool.appendToSpanInfo("content length", NumberFormatter.addCommas(request.getContentLength()));
			this.fileItems = new ServletFileUpload(newDiskItemFactory()).parseRequest(this.request);
			for(FileItem fileItem : this.fileItems){
				TracerTool.appendToSpanInfo("file size", NumberFormatter.addCommas(fileItem.getSize()));
			}
		}
		for(FileItem item : fileItems){
			// paramsMap already contents the query params, we still need to add the form params
			if(item.isFormField()){
				paramsMap.put(item.getFieldName(), item.getString());
			}else{
				filesMap.put(item.getFieldName(), item);
			}
		}
	}

	@Override
	public FileItem requiredFile(String key){
		return Objects.requireNonNull(filesMap.get(key));
	}

	@Override
	public Optional<FileItem> optionalFile(String key){
		return Optional.ofNullable(filesMap.get(key));
	}

	public List<FileItem> getFileItems(){
		return fileItems;
	}

	@Override
	public Map<String,String> toMap(){
		return paramsMap;
	}

	private DiskFileItemFactory newDiskItemFactory(){
		if(defaultCharset != null){
			DatarouterDiskFileItemFactory factory = new DatarouterDiskFileItemFactory();
			factory.setDefaultCharset(defaultCharset);
			return factory;
		}
		return new DiskFileItemFactory();
	}

}
