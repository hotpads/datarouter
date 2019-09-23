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
package io.datarouter.model.serialize.fielder;

import java.time.Duration;

public class TtlFielderConfig implements FielderConfigValue<TtlFielderConfig>{

	public static final FielderConfigKey<TtlFielderConfig> KEY = new FielderConfigKey<>("TTL");

	private final Duration ttl;

	public TtlFielderConfig(Duration ttl){
		this.ttl = ttl;
	}

	public Duration getTtl(){
		return ttl;
	}

	@Override
	public FielderConfigKey<TtlFielderConfig> getKey(){
		return KEY;
	}
}
