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
package io.datarouter.httpclient.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;

public class HttpEntityLineStreamAccessor implements Consumer<HttpEntity>{

	private Stream<String> lineStream;

	@Override
	public void accept(HttpEntity httpEntity){
		InputStreamReader isr;
		try{
			isr = new InputStreamReader(httpEntity.getContent(), StandardCharsets.UTF_8);
		}catch(UnsupportedOperationException | IOException e){
			throw new RuntimeException(e);
		}
		lineStream = new BufferedReader(isr).lines();
	}

	public Stream<String> getLineStream(){
		return lineStream;
	}

}
