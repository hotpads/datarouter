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
package io.datarouter.plugin.copytable.web;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.plugin.copytable.config.DatarouterCopyTablePaths;
import io.datarouter.web.config.ServletContextSupplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SystemTableLinks{

	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DatarouterCopyTablePaths paths;


	public String listSystemTables(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.systemTableCopier.listSystemTables.toSlashedString());
		return uriBuilder.toString();
	}

	public String migrateSystemTables(){
		var uriBuilder = new URIBuilder()
				.setPath(contextSupplier.getContextPath()
						+ paths.datarouter.systemTableCopier.migrateSystemTables.toSlashedString());
		return uriBuilder.toString();
	}

}