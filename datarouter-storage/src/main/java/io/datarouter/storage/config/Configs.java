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
package io.datarouter.storage.config;

import java.time.Duration;

public class Configs{

	public static final boolean SLAVE_OK = true;
	public static final boolean USE_MASTER = !SLAVE_OK;

	public static Config slaveOk(){
		return new Config().setSlaveOk(true);
	}

	public static Config noTimeout(){
		return new Config().setTimeout(Duration.ofMillis(Long.MAX_VALUE));
	}

	public static Config insertOrBust(){
		return new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
	}

	public static Config updateOrBust(){
		return new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
	}

	public static Config insertOrUpdate(){
		return new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE);
	}

	public static Config updateOrInsert(){
		return new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT);
	}

	public static Config merge(){
		return new Config().setPutMethod(PutMethod.MERGE);
	}

}
