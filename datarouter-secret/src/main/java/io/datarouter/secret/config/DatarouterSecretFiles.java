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
package io.datarouter.secret.config;

import javax.inject.Singleton;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.web.file.FilesRoot;

@Singleton
public class DatarouterSecretFiles extends FilesRoot{

	public final JspFiles jsp = branch(JspFiles::new, "jsp");
	public final JsFiles js = branch(JsFiles::new, "js");

	public static class JspFiles extends PathNode{
		public final PathNode secretsJsp = leaf("secrets.jsp");
	}

	public static class JsFiles extends PathNode{
		public final PathNode secretsJsx = leaf("secrets.jsx");
	}

}
