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
package io.datarouter.web.handler.types;

import java.util.List;

import io.datarouter.web.api.web.BaseWebApi;
import io.datarouter.web.api.web.JsClientType;
import io.datarouter.web.api.web.WebApiType.NoOpWebApiType;
import io.datarouter.web.handler.BaseHandler.Handler;

public class TestWebApiHandler{

	public static class PrintIntList extends BaseWebApi<List<Integer>,NoOpWebApiType>{
		public List<Integer> numbers;

		public PrintIntList(){
			super(GET, null);
		}

		@Override
		public List<Class<? extends JsClientType>> getJsClientTypes(){
			return null;
		}
	}

	@Handler
	public List<Integer> printIntList(PrintIntList endpoint){
		return endpoint.numbers;
	}

}
