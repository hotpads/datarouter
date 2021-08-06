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
package io.datarouter.filesystem.snapshot.key;

import java.util.Comparator;

public class SnapshotKey implements Comparable<SnapshotKey>{

	private static final Comparator<SnapshotKey> COMPARATOR = Comparator
			.comparing(SnapshotKey::getGroupId)
			.thenComparing(SnapshotKey::getSnapshotId);

	public final String groupId;
	public final String snapshotId;

	public SnapshotKey(String groupId, String snapshotId){
		this.groupId = groupId;
		this.snapshotId = snapshotId;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((snapshotId == null) ? 0 : snapshotId.hashCode());
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
		if(getClass() != obj.getClass()){
			return false;
		}
		SnapshotKey other = (SnapshotKey)obj;
		if(groupId == null){
			if(other.groupId != null){
				return false;
			}
		}else if(!groupId.equals(other.groupId)){
			return false;
		}
		if(snapshotId == null){
			if(other.snapshotId != null){
				return false;
			}
		}else if(!snapshotId.equals(other.snapshotId)){
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return "SnapshotKey [groupId=" + groupId + ", snapshotId=" + snapshotId + "]";
	}

	@Override
	public int compareTo(SnapshotKey other){
		return COMPARATOR.compare(this, other);
	}

	public String getGroupId(){
		return this.groupId;
	}

	public String getSnapshotId(){
		return this.snapshotId;
	}

}
