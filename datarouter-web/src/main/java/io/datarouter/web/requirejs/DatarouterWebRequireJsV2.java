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
package io.datarouter.web.requirejs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.DatarouterWebFiles;
import j2html.tags.ContainerTag;

public class DatarouterWebRequireJsV2{

	private static final DatarouterWebFiles FILES = new DatarouterWebFiles();
	private static final Map<String,PathNode> SCRIPTS = new LinkedHashMap<>();//ordering might matter

	public static final String JQUERY = register(
			"jquery",
			FILES.jquery.jqueryJs);
	public static final String JQUERY_UI = register(
			"jquery-ui",
			FILES.jquery.jqueryUiJs);
	public static final String JQUERY_VALIDATE = register(
			"jquery.validate",
			FILES.jquery.jqueryValidateJs);
	public static final String BOOTSTRAP = register(
			"bootstrap",
			FILES.bootstrap.v4.js.bootstrapJs);
	public static final String SORTTABLE = register(
			"sorttable",
			FILES.sorttable.sorttableJs);
	public static final String MULTIPLE_SELECT = register(
			"multiple-select",
			FILES.jeeAssets.multipleSelect.multipleSelectJs);
	public static final String DYGRAPH = register(
			"dygraph",
			FILES.dygraph.dygraphCombinedJs);
	public static final String CHART = register(
			"chart",
			FILES.chart.chartJs);
	public static final String DYGRAPH_EXTRA = register(
			"dygraph-extra",
			FILES.dygraph.dygraphExtraJs);
	public static final String GOOG = register(
			"goog",
			FILES.requirejs.plugins.googJs);

	private static String register(String name, PathNode pathNode){
		SCRIPTS.put(name, pathNode);
		return name;
	}

	private static final Map<String,List<String>> SHIMS = new LinkedHashMap<>();//ordering might matter
	static{
		SHIMS.put(BOOTSTRAP, List.of(JQUERY));
		SHIMS.put(DYGRAPH_EXTRA, List.of(DYGRAPH));
		SHIMS.put(MULTIPLE_SELECT, List.of(JQUERY));
		SHIMS.put(JQUERY_VALIDATE, List.of(JQUERY));
	}

	public static ContainerTag<?> makeImportTag(String contextPath){
		return RequireJsTool.makeRequireJsImportTag(contextPath, FILES.requirejs.requireJs);
	}

	public static ContainerTag<?> makeConfigScriptTag(String contextPath){
		return RequireJsTool.makeConfigScriptTag(makeConfigJsonString(contextPath));
	}

	public static String makeConfigJsonString(String contextPath){
		return RequireJsTool.makeConfigJsonString(contextPath, SCRIPTS, SHIMS);
	}

}
