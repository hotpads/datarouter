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
package io.datarouter.storage.callsite;


//for some reason, the eclipse error highligher hates the name CallsiteStatKey, so add a random "X"
public class CallsiteStatKeyX{
	private String callsite;
	private String nodeName;

	public CallsiteStatKeyX(String callsite, String nodeName){
		this.callsite = callsite;
		this.nodeName = nodeName;
	}

	public String getCallsite(){
		return callsite;
	}

	public String getNodeName(){
		return nodeName;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (callsite == null ? 0 : callsite.hashCode());
		result = prime * result + (nodeName == null ? 0 : nodeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(!(obj instanceof CallsiteStatKeyX)){
			return false;
		}
		CallsiteStatKeyX other = (CallsiteStatKeyX)obj;
		if(callsite == null){
			if(other.callsite != null){
				return false;
			}
		}else if(!callsite.equals(other.callsite)){
			return false;
		}
		if(nodeName == null){
			if(other.nodeName != null){
				return false;
			}
		}else if(!nodeName.equals(other.nodeName)){
			return false;
		}
		return true;
	}

}
