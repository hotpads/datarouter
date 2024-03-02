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
package io.datarouter.web.util.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CachingHttpServletResponse extends HttpServletResponseWrapper{

	private ServletOutputStream outputStream;
	private PrintWriter writer;
	private CachingServletOutputStream cachedOutputStream;

	public CachingHttpServletResponse(HttpServletResponse response){
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException{
		if(writer != null){
			throw new IllegalStateException("getWriter() has already been called on this response.");
		}
		if(outputStream == null){
			outputStream = getResponse().getOutputStream();
			cachedOutputStream = new CachingServletOutputStream(outputStream);
		}
		return cachedOutputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException{
		if(outputStream != null){
			throw new IllegalStateException("getOutputStream() has already been called on this response.");
		}
		if(writer == null){
			cachedOutputStream = new CachingServletOutputStream(getResponse().getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(cachedOutputStream, getResponse().getCharacterEncoding()),
					true);
		}
		return writer;
	}

	@Override
	public void flushBuffer() throws IOException{
		if(writer != null){
			writer.flush();
		}else if(outputStream != null){
			cachedOutputStream.flush();
		}
	}

	public byte[] getCopy(){
		if(cachedOutputStream != null){
			return cachedOutputStream.getCopy();
		}else{
			return new byte[0];
		}
	}

	public boolean isOutputStreamCached(){
		if(cachedOutputStream != null){
			return cachedOutputStream.isOutputStreamWithinLimit();
		}
		return true;
	}

}
