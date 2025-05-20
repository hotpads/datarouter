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
package io.datarouter.web.handler.encoder;

import io.datarouter.gson.GsonJsonSerializer;
import io.datarouter.json.JsonSerializer;
import io.datarouter.web.exception.ExceptionHandlingConfig;
import io.datarouter.web.handler.types.DefaultDecoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Hard-coded to GsonJsonSerializer which uses Datarouter's root Gson instance.
 */
public class DatarouterDefaultHandlerCodec extends BaseHandlerCodec{

	public static final DatarouterDefaultHandlerCodec INSTANCE = new DatarouterDefaultHandlerCodec();

	private static final JsonSerializer JSON_SERIALIZER = GsonJsonSerializer.DEFAULT;

	private DatarouterDefaultHandlerCodec(){
		super(DatarouterDefaultHandlerEncoder.class, DatarouterDefaultHandlerDecoder.class);
	}

	@Singleton
	public static class DatarouterDefaultHandlerEncoder extends DefaultEncoder{

		@Inject
		public DatarouterDefaultHandlerEncoder(
				MavEncoder mavEncoder,
				InputStreamHandlerEncoder inputStreamHandlerEncoder,
				ExceptionHandlingConfig exceptionHandlingConfig){
			super(mavEncoder,
					inputStreamHandlerEncoder,
					new JsonEncoder(JSON_SERIALIZER, exceptionHandlingConfig));
		}

	}

	@Singleton
	public static class DatarouterDefaultHandlerDecoder extends DefaultDecoder{

		@Inject
		public DatarouterDefaultHandlerDecoder(){
			super(JSON_SERIALIZER);
		}

	}

}