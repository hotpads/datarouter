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
package io.datarouter.job.util;

/**
 * Boolean wrapper with reason
 */
public record DatarouterJobOutcome(
		boolean success,
		String reason){

	public static DatarouterJobOutcome makeSuccess(){
		return new DatarouterJobOutcome(true, "");
	}

	public static DatarouterJobOutcome makeSuccess(String reason){
		return new DatarouterJobOutcome(true, reason);
	}

	public static DatarouterJobOutcome makeFailure(String reason){
		return new DatarouterJobOutcome(false, reason);
	}

	public boolean failed(){
		return !success;
	}

	public DatarouterJobOutcome onFailure(Runnable runnable){
		if(!success){
			runnable.run();
		}
		return this;
	}

	public DatarouterJobOutcome onSuccess(Runnable runnable){
		if(success){
			runnable.run();
		}
		return this;
	}

}
