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
package io.datarouter.client.mysql.ddl.generate;

import java.util.Optional;

public class Ddl{

	public final Optional<String> executeStatement;
	public final Optional<String> printStatement;
	public final boolean preventStartUp;

	public Ddl(Optional<String> executeStatement, Optional<String> printStatement, boolean preventStartUp){
		this.executeStatement = executeStatement;
		this.printStatement = printStatement;
		this.preventStartUp = preventStartUp;
	}

}
