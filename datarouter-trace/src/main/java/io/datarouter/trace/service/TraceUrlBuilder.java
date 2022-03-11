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
package io.datarouter.trace.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.trace.config.DatarouterTracePaths;
import io.datarouter.trace.settings.DatarouterTraceFilterSettingRoot;
import io.datarouter.web.config.ServletContextSupplier;

public interface TraceUrlBuilder{

	String buildTraceForCurrentServer(String traceId, String parentId);

	@Singleton
	class LocalTraceUrlBulder implements TraceUrlBuilder{

		@Inject
		private DatarouterTracePaths paths;
		@Inject
		private DatarouterTraceFilterSettingRoot settings;
		@Inject
		private ServletContextSupplier servletContext;

		@Override
		public String buildTraceForCurrentServer(String traceId, String parentId){
			return "https://"
					+ settings.traceDomain.get()
					+ servletContext.get().getContextPath()
					+ paths.datarouter.traces.toSlashedString()
					+ "?traceId=" + traceId
					+ "&parentId=" + parentId;
		}

	}

}
