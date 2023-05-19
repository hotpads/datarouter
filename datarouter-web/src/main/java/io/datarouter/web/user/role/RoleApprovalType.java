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
package io.datarouter.web.user.role;

import java.util.Objects;

public class RoleApprovalType implements Comparable<RoleApprovalType>{

	public final String persistentString;

	public RoleApprovalType(String persistentString){
		this.persistentString = persistentString;
	}

	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public int compareTo(RoleApprovalType other){
		return persistentString.compareTo(other.getPersistentString());
	}

	@Override
	public int hashCode(){
		return persistentString.hashCode();
	}

	@Override
	public String toString(){
		return persistentString;
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof RoleApprovalType)){
			return false;
		}
		return RoleApprovalType.equals(this, (RoleApprovalType)obj);
	}

	static boolean equals(RoleApprovalType first, RoleApprovalType second){
		if(first == null){
			return second == null;
		}
		return second != null && Objects.equals(first.getPersistentString(), second.getPersistentString());
	}

}
