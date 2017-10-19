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
package io.datarouter.httpclient.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class CachingServletInputStream extends ServletInputStream{

	private final InputStream cachedInputStream;
	private boolean finished = false;

	public CachingServletInputStream(InputStream cachedInputStream){
		Objects.requireNonNull(cachedInputStream, "inputStream must not be null");
		this.cachedInputStream = cachedInputStream;
	}

	@Override
	public int read() throws IOException{
		int data = cachedInputStream.read();
		if(data == -1){
			finished = true;
		}
		return data;
	}

	@Override
	public boolean isFinished(){
		return finished;
	}

	@Override
	public boolean isReady(){
		return true;
	}

	@Override
	public void setReadListener(ReadListener readListener){
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException{
		super.close();
		cachedInputStream.close();
	}

}