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
package io.datarouter.web.user.authenticate.saml;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.saml.CustomSamlConfigParamsSupplier;
import io.datarouter.auth.authenticate.saml.CustomSamlConfigParamsSupplier.CustomSamlConfigParam;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.ServletContextSupplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// useful when there is a path re-write

// TODO braydonh: figure out how to move this out of dr-web
@Singleton
public class CustomSamlConfigService extends SamlConfigService{
	private static final Logger logger = LoggerFactory.getLogger(CustomSamlConfigService.class);

	@Inject
	private CustomSamlConfigParamsSupplier paramsSupplier;
	@Inject
	private ServletContextSupplier contextSupplier;

	@Override
	public String getRequestUrl(HttpServletRequest request){
		String host = Optional.ofNullable(request.getHeader("host")).orElse("");
		Optional<CustomSamlConfigParam> matchingHostedSiteParam = Scanner.of(paramsSupplier.get())
				.include(hostedSiteParam -> hostedSiteParam.domain().equals(host))
				.findFirst();
		logger.debug("does the domain match? matchingHostedSiteParam={} acceptedDomains={} requestDomain={}",
				matchingHostedSiteParam,
				paramsSupplier.get(),
				host);
		if(matchingHostedSiteParam.isPresent()){
			String path = contextSupplier.getContextPath() + matchingHostedSiteParam.get().appRoot().toSlashedString();
			String requestUrl = request.getRequestURL().toString();
			requestUrl = requestUrl.replace(path, "");
			String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
			logger.info("overriding requestUrl to={}", requestUrl);
			return requestUrl + queryString;
		}
		return super.getRequestUrl(request);
	}

}
