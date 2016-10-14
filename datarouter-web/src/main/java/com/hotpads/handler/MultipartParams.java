package com.hotpads.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.base.Preconditions;

public class MultipartParams extends Params{
	private final HttpServletRequest request;
	private final Map<String,FileItem> filesMap;

	public MultipartParams(HttpServletRequest request) throws FileUploadException{
		super(request);
		this.request = request;
		paramsMap.clear();
		filesMap = new HashMap<>();
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(this.request);
		for(FileItem item : items){
			if(item.isFormField()){
				paramsMap.put(item.getFieldName(), item.getString());
			} else {
				filesMap.put(item.getFieldName(), item);
			}
		}
	}

	@Override
	public FileItem requiredFile(String key){
		return Preconditions.checkNotNull(filesMap.get(key));
	}

	@Override
	public Optional<FileItem> optionalFile(String key){
		return Optional.ofNullable(filesMap.get(key));
	}

	@Override
	public Map<String,String> toMap(){
		return paramsMap;
	}

}
