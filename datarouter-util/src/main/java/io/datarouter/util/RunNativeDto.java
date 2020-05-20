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
package io.datarouter.util;

public class RunNativeDto{

	public final String stdout;
	public final String stderr;
	public final int exitVal;

	public RunNativeDto(String stdout, String stderr, int exitVal){
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitVal = exitVal;
	}

}
