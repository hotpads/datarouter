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
public class Outcome{

	private final boolean success;
	private final String reason;

	private Outcome(boolean success, String reason){
		this.success = success;
		this.reason = reason;
	}

	public static Outcome success(){
		return new Outcome(true, "");
	}

	public static Outcome success(String reason){
		return new Outcome(true, reason);
	}

	public static Outcome failure(String reason){
		return new Outcome(false, reason);
	}

	public String reason(){
		return reason;
	}

	public boolean failed(){
		return !success;
	}

	public boolean succeeded(){
		return success;
	}

}
