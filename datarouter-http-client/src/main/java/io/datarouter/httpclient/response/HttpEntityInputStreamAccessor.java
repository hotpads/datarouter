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
package io.datarouter.httpclient.response;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;

public class HttpEntityInputStreamAccessor implements Consumer<HttpEntity>{

	private InputStream inputStream;
	private IOException capturedException;

	@Override
	public void accept(HttpEntity httpEntity){
		try{
			this.inputStream = httpEntity == null ? null : httpEntity.getContent();
		}catch(IOException e){
			this.capturedException = e;
		}
	}

	public InputStream getInputStream() throws IOException{
		if(capturedException != null){
			throw capturedException;
		}
		return inputStream;
	}

}
