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
	private final Map<String,String> paramsMap;
	private final Map<String,FileItem> filesMap;

	public MultipartParams(HttpServletRequest request){
		super(request);
		this.request = request;
		paramsMap = new HashMap<>();
		filesMap = new HashMap<>();
		try{
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(this.request);
			for(FileItem item : items){
				if(item.isFormField()){
					paramsMap.put(item.getFieldName(), item.getString());
				} else {
					filesMap.put(item.getFieldName(), item);
				}
			}
		}catch(FileUploadException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String required(String key){
		return Preconditions.checkNotNull(paramsMap.get(key));
	}

	@Override
	public Optional<String> optional(String key){
		return Optional.ofNullable(paramsMap.get(key));
	}

	public FileItem requiredFile(String key){
		return Preconditions.checkNotNull(filesMap.get(key));
	}

	public Optional<FileItem> optionalFile(String key){
		return Optional.ofNullable(filesMap.get(key));
	}

	@Override
	public Map<String,String> toMap(){
		return paramsMap;
	}

}
