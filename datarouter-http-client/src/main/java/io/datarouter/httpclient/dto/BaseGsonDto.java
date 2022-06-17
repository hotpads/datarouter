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
package io.datarouter.httpclient.dto;

import io.datarouter.gson.serialization.GsonTool;

/**
 * Convenience class with low performance hashCode/equals and a toString method that uses default Gson options which
 * may differ from those in your application.
 */
public abstract class BaseGsonDto{

	@Override
	public int hashCode(){
		return toJson().hashCode();
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		BaseGsonDto other = (BaseGsonDto)obj;
		return toJson().equals(other.toJson());
	}

	@Override
	public String toString(){
		return toJson();
	}

	private String toJson(){
		return GsonTool.GSON.toJson(this);
	}

}
