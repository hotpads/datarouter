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

import static j2html.TagCreator.script;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import io.datarouter.gson.GsonTool;
import io.datarouter.pathnode.PathNode;
import j2html.TagCreator;
import j2html.tags.specialized.ScriptTag;

public class RequireJsTool{

	private static final Gson GSON = GsonTool.GSON.newBuilder()
			.setPrettyPrinting()
			.create();

	public static ScriptTag makeRequireJsImportTag(String contextPath, PathNode path){
		return script()
				.withSrc(contextPath + path.toSlashedString());
	}

	public static ScriptTag makeConfigScriptTag(String config){
		String rawHtml = String.format("require.config(%s)", config);
		return script(TagCreator.rawHtml(rawHtml));
	}

	public static String makeConfigJsonString(
			String contextPath,
			Map<String,PathNode> scriptPathByName,
			Map<String,List<String>> shimsByName){
		LinkedHashMap<String,String> paths = new LinkedHashMap<>();
		scriptPathByName.forEach((k, v) -> paths.put(k, formatScriptPath(v)));
		Map<String,RequireJsShim> shim = new LinkedHashMap<>();
		shimsByName.forEach((k, v) -> shim.put(k, new RequireJsShim(v)));
		RequireJsConfigParam requireJsConfigParam = new RequireJsConfigParam(contextPath + "/", paths, shim);
		return GSON.toJson(requireJsConfigParam);
	}

	public static ScriptTag makeRequireScriptTag(String... names){
		String rawHtml = String.format("require(%s)", makeRequireParams(names));
		return script(TagCreator.rawHtml(rawHtml));
	}

	public static ScriptTag makeRequireScriptTagWithCallback(String[] names, String callback){
		String rawHtml = String.format("require(%s, function(){%s})", makeRequireParams(names), callback);
		return script(TagCreator.rawHtml(rawHtml));
	}

	public static String makeRequireParams(String... names){
		return Arrays.stream(names)
				.collect(Collectors.joining("', '", "['", "']"));
	}

	private static String formatScriptPath(PathNode path){
		String pathString = path.toSlashedString();
		//it doesn't like the leading slash, even if there's no trailing slash on the contextPath
		String withoutLeadingSlash = pathString.substring(1, pathString.length());
		//remove .js extensions
		String withoutExtension = withoutLeadingSlash.substring(0, withoutLeadingSlash.length() - 3);
		return withoutExtension;
	}

	public static class RequireJsConfigParam{

		final String baseUrl;
		final Map<String,String> paths;
		final Map<String,RequireJsShim> shim;
		final boolean enforceDefine;

		public RequireJsConfigParam(String baseUrl, Map<String,String> paths, Map<String,RequireJsShim> shim){
			this.baseUrl = baseUrl;
			this.paths = paths;
			this.shim = shim;
			this.enforceDefine = false;
		}

	}

	public static class RequireJsShim{

		final List<String> deps;

		public RequireJsShim(List<String> deps){
			this.deps = deps;
		}

	}

}
