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
package io.datarouter.web.html.j2html.bootstrap4;

import java.util.Map;

import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.css.DatarouterWebCssV2;
import io.datarouter.web.handler.mav.MavProperties;
import io.datarouter.web.html.j2html.DatarouterPageHead;
import io.datarouter.web.js.DatarouterWebJsTool;
import io.datarouter.web.navigation.DatarouterNavbarFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.web.requirejs.RequireJsTool;
import j2html.tags.EmptyTag;

public class Bootstrap4PageHead extends DatarouterPageHead{

	private static final DatarouterWebFiles FILES = new DatarouterWebFiles();

	public Bootstrap4PageHead(
			MavProperties mavProperties,
			String contextPath,
			String webappRequireJsConfigJsonString,
			String[] require,
			boolean isAdmin,
			String title,
			Map<String,String> httpEquivs){
		super(
				DatarouterWebCssV2.makeCssImportTags(contextPath),
				DatarouterWebRequireJs.makeImportTag(contextPath),
				DatarouterWebRequireJsV2.makeConfigScriptTag(contextPath),
				RequireJsTool.makeConfigScriptTag(webappRequireJsConfigJsonString),
				RequireJsTool.makeRequireScriptTag(require),
				isAdmin ? DatarouterNavbarFactory.makeNavbarV2CssImportTags(contextPath, mavProperties
						.getTomcatWebApps().size()) : new EmptyTag[]{},
				isAdmin ? DatarouterWebJsTool.makeJsImport(contextPath, FILES.js.navbarRequestTimingV2Js) : null,
				isAdmin ? DatarouterNavbarFactory.makeNavbarRequestTimingScriptV2(contextPath) : null,
				title,
				httpEquivs);
	}

}
