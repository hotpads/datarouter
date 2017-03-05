package com.hotpads.handler.encoder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.exception.HandledException;
import com.hotpads.util.http.ResponseTool;

public class InputStreamHandlerEncoder implements HandlerEncoder{

	/*
	 * Methods that use this can specify whatever headers they want, for example a file download would include:
	 *   response.setContentType("application/x-download");
	 *   response.setHeader("Content-Disposition", "attachment;filename=someFile.txt");
	 */

	@Override
	public void finishRequest(Object result, ServletContext servletContext, HttpServletResponse response,
			HttpServletRequest request){
		if(result == null){
			return;
		}

		try(InputStream inputStream = (InputStream)result){
			int count;
			byte[] buffer = new byte[1024];
			BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
			while((count = inputStream.read(buffer)) > 0){
				outputStream.write(buffer, 0, count);
			}
			outputStream.flush();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendExceptionResponse(HandledException exception, ServletContext servletContext,
			HttpServletResponse response, HttpServletRequest request){
		ResponseTool.sendError(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
	}

}