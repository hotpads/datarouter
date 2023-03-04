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

import java.lang.reflect.Type;

import com.google.gson.JsonSyntaxException;

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.web.handler.encoder.JsonAwareHandlerCodec;

public interface JsonAwareHandlerDecoder extends HandlerDecoder, JsonAwareHandlerCodec{

	default Object decodeType(String string, Type type){
		try(var $ = TracerTool.startSpan(getClass().getSimpleName() + " deserialize",
				TraceSpanGroupType.SERIALIZATION)){
			TracerTool.appendToSpanInfo("characters", string.length());
			// this prevents empty strings from being decoded as null by gson
			Object obj;
			try{
				obj = getJsonSerializer().deserialize(string, type);
			}catch(JsonSyntaxException e){
				// If the JSON is malformed and String is expected, just assign the string
				if(type.equals(String.class)){
					return string;
				}
				throw new RuntimeException("failed to decode " + string + " to a " + type, e);
			}
			// deserialized successfully as null, but we want empty string instead of null for consistency with Params
			if(obj == null && type.equals(String.class) && !"null".equals(string)){
				return "";
			}
			if(obj == null){
				throw new RuntimeException("could not decode " + string + " to a non null " + type);
			}
			return obj;
		}
	}

}
