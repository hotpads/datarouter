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
package io.datarouter.clustersetting.config;

import javax.inject.Singleton;

import io.datarouter.pathnode.PathNode;
import io.datarouter.pathnode.PathsRoot;

@Singleton
public class DatarouterClusterSettingPaths extends PathNode implements PathsRoot{

	public final DatarouterPaths datarouter = branch(DatarouterPaths::new, "datarouter");

	public static class DatarouterPaths extends PathNode{
		public final SettingPaths settings = branch(SettingPaths::new, "settings");
	}

	public static class SettingPaths extends PathNode{
		public final PathNode browseSettings = leaf("browseSettings");
		public final PathNode create = leaf("create");
		public final PathNode delete = leaf("delete");
		public final PathNode isRecognizedRoot = leaf("isRecognizedRoot");
		public final PathNode roots = leaf("roots");
		public final PathNode searchSettingNames = leaf("searchSettingNames");
		public final PathNode update = leaf("update");
		public final PathNode updateSettingTags = leaf("updateSettingTags");
		public final PathNode tags = leaf("tags");
		public final SettingBrowse browse = branch(SettingBrowse::new, "browse");
		public final SettingLog log = branch(SettingLog::new, "log");
		public final SettingOverrides overrides = branch(SettingOverrides::new, "overrides");
	}

	public static class SettingBrowse extends PathNode{
		public final PathNode all = leaf("all");
	}

	public static class SettingLog extends PathNode{
		public final PathNode all = leaf("all");
		public final PathNode node = leaf("node");
		public final PathNode setting = leaf("setting");
		public final PathNode single = leaf("single");
	}

	public static class SettingOverrides extends PathNode{
		public final PathNode view = leaf("view");
		public final PathNode create = leaf("create");
		public final PathNode update = leaf("update");
		public final PathNode delete = leaf("delete");
	}

}
