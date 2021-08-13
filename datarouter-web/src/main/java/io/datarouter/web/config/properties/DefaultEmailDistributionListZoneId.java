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
package io.datarouter.web.config.properties;

import java.time.ZoneId;
import java.util.function.Supplier;

import javax.inject.Singleton;

/**
 * An injectable zoneId for where most of the people on a email distribution list are located
 */
@Singleton
public class DefaultEmailDistributionListZoneId implements Supplier<ZoneId>{

	public final ZoneId zoneId;

	public DefaultEmailDistributionListZoneId(ZoneId zoneId){
		this.zoneId = zoneId;
	}

	@Override
	public ZoneId get(){
		return zoneId;
	}

}
