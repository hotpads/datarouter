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
package io.datarouter.web.digest;

import io.datarouter.email.link.DatarouterEmailLinkClient;
import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlHeading;
import io.datarouter.instrumentation.relay.rml.RmlText;
import io.datarouter.pathnode.PathNode;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.config.service.DomainFinder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DailyDigestRmlService{

	@Inject
	private DomainFinder domainFinder;
	@Inject
	private ServletContextSupplier servletContext;
	@Inject
	private DatarouterEmailLinkClient linkClient;

	public RmlHeading makeHeading(String title, PathNode path){
		String link = "https://" + domainFinder.getRelativeDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "");
		return makeHeading(title, link);
	}

	public RmlHeading makeHeading(String title, PathNode path, String pathSupplement){
		String link = "https://" + domainFinder.getRelativeDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "") + pathSupplement;
		return makeHeading(title, link);
	}

	public RmlHeading makeHeading(String title, DatarouterLink datarouterLink){
		String link = linkClient.toUrl(datarouterLink);
		return makeHeading(title, link);
	}

	public static RmlHeading makeHeading(String title){
		return Rml.heading(3, Rml.text(title));
	}

	public static RmlHeading makeHeading(String title, String url){
		return Rml.heading(3, Rml.text(title).link(url));
	}

	public RmlText makeLink(String text, PathNode path){
		return makeLink(text, path, "");
	}

	public RmlText makeLink(String text, PathNode path, String pathSupplement){
		String link = "https://" + domainFinder.getRelativeDomainPreferPublic() + servletContext.get().getContextPath()
				+ path.join("/", "/", "") + pathSupplement;
		return Rml.text(text).link(link);
	}

}
