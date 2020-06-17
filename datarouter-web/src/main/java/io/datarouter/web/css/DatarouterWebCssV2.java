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
package io.datarouter.web.css;

import java.util.Arrays;
import java.util.List;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebFiles;
import j2html.tags.EmptyTag;

public class DatarouterWebCssV2{

	private static final DatarouterWebFiles FILES = new DatarouterWebFiles();

	private static final List<PathNode> CSS_PATHS = Arrays.asList(
			FILES.css.commonB4Css,
			FILES.fontAwesome.css.fontAwesomeBaseCss,
			FILES.fontAwesome.css.fontAwesomeRegularCss,
			FILES.fontAwesome.css.fontAwesomeSolidCss,
			FILES.bootstrap.v4.css.bootstrapCss,
			FILES.bootstrap.v4.css.datarouterBoostrap4OverrideCss);

	public static EmptyTag[] makeCssImportTags(String contextPath){
		return DatarouterWebCssTool.makeCssImportTags(contextPath, CSS_PATHS);
	}

}
