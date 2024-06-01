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
package io.datarouter.graphql.config;

import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.PathNode;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGraphQlFiles extends FilesRoot{

	public final JspFiles jsp = branch(JspFiles::new, "jsp");

	public static class JspFiles extends PathNode{
		public final GraphQlFiles graphql = branch(GraphQlFiles::new, "graphql");
	}

	public static class GraphQlFiles extends PathNode{
		public final BuildFiles build = branch(BuildFiles::new, "build");
		public final PathNode playgroundJsp = leaf("playground.jsp");
	}

	public static class BuildFiles extends PathNode{
		public final StaticFiles staticFiles = branch(StaticFiles::new, "static");
		public final PathNode headers = leaf("_headers");
		public final PathNode redirects = leaf("_redirects");
		public final PathNode assetManifestJson = leaf("asset-manifest.json");
		public final PathNode faviconPng = leaf("favicon.png");
		public final PathNode indexHtml = leaf("index.html");
		public final PathNode logoPng = leaf("logo.png");
		public final PathNode middlewareHtml = leaf("middleware.html");
	}

	public static class StaticFiles extends PathNode{
		public final CssFiles css = branch(CssFiles::new, "css");
		public final JsFiles js = branch(JsFiles::new, "js");
	}

	public static class CssFiles extends PathNode{
		public final PathNode indexCss = leaf("index.css");
		public final PathNode indexCssMap = leaf("index.css.map");
		public final PathNode middlewareCss = leaf("middleware.css");
		public final PathNode middlewareCssMap = leaf("middleware.css.map");
	}

	public static class JsFiles extends PathNode{
		public final PathNode indexJs = leaf("index.js");
		public final PathNode indexJsMap = leaf("index.js.map");
		public final PathNode middlewareJs = leaf("middleware.js");
		public final PathNode middlewareJsMap = leaf("middleware.js.map");
	}

}
