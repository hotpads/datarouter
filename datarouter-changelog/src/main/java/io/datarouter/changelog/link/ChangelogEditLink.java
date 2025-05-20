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
package io.datarouter.changelog.link;

import io.datarouter.changelog.config.DatarouterChangelogPaths;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.types.MilliTimeReversed;

public class ChangelogEditLink extends DatarouterLink{

	public static final String P_reversedDateMs = "reversedDateMs",
			P_changelogType = "changelogType",
			P_name = "name";

	public final MilliTimeReversed reversedDateMs;
	public final String changelogType;
	public final String name;

	public ChangelogEditLink(MilliTimeReversed reversedDateMs, String changelogType, String name){
		super(new DatarouterChangelogPaths().datarouter.changelog.edit);
		this.reversedDateMs = reversedDateMs;
		this.changelogType = changelogType;
		this.name = name;
	}

}
