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
package io.datarouter.clustersetting.web.override;

import static j2html.TagCreator.a;
import static j2html.TagCreator.b;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import javax.inject.Singleton;

import io.datarouter.clustersetting.ClusterSettingScope;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;

@Singleton
public class ClusterSettingOverrideHtml{

	/*---------- buttons -----------*/

	public ATag makeCreateButton(String name, String href){
		return a(name)
				.withClass("btn btn-primary")
				.withHref(href);
	}

	public ATag makeWarningButtonSmall(String name, String href){
		return a(name)
				.withClass("btn btn-warning btn-sm")
				.withHref(href);
	}

	public ATag makeDangerButtonSmall(String name, String href){
		return a(name)
				.withClass("btn btn-danger btn-sm")
				.withHref(href);
	}

	/*------------ summary for update/delete ---------*/

	public DivTag makeSummaryDiv(
			String name,
			ClusterSettingScope scope,
			String serverType,
			String serverName){
		var tableTag = table()
				.withClasses("table table-sm table-striped my-2 border")
				.withStyle("width:800px;");
		var trName = tr(
				td(b("Setting name:")),
				td(name));
		tableTag.with(trName);
		var trScope = tr(
				td(b("Scope:")),
				td(scope.display));
		tableTag.with(trScope);
		if(scope == ClusterSettingScope.SERVER_TYPE){
			var trServerType = tr(
					td(b("Server Type:")),
					td(serverType));
			tableTag.with(trServerType);
		}
		if(scope == ClusterSettingScope.SERVER_NAME){
			var trServerName = tr(
					td(b("Server Name:")),
					td(serverName));
			tableTag.with(trServerName);
		}
		return div(
				h5("Summary"),
				tableTag);
	}

}
