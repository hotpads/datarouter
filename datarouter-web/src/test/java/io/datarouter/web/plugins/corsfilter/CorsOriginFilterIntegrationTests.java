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
package io.datarouter.web.plugins.corsfilter;

import java.util.List;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Module;

import io.datarouter.web.plugins.corsfilter.CorsFilterPlugin.CorsFilterPluginBuilder;
import io.datarouter.web.plugins.corsfilter.CorsOriginFilterIntegrationTests.CorsFilterTestNgModuleFactory;
import io.datarouter.web.test.DatarouterWebTestNgModuleFactory;
import jakarta.inject.Inject;

@Guice(moduleFactory = CorsFilterTestNgModuleFactory.class)
public class CorsOriginFilterIntegrationTests{

	@Inject
	private CorsOriginFilter originFilter;

	@Test
	public void testMatchOrigin(){
		Assert.assertFalse(originFilter.matchOrigin(null));
		Assert.assertFalse(originFilter.matchOrigin("nomatch"));
		Assert.assertFalse(originFilter.matchOrigin("https://bad.com"));

		Assert.assertTrue(originFilter.matchOrigin("https://localhost"));
		Assert.assertTrue(originFilter.matchOrigin("https://localhost:8443"));
		Assert.assertTrue(originFilter.matchOrigin("http://exact-match.com"));
		Assert.assertTrue(originFilter.matchOrigin("https://a.com"));
		Assert.assertTrue(originFilter.matchOrigin("https://b.com"));
	}

	public static class CorsFilterTestNgModuleFactory extends DatarouterWebTestNgModuleFactory{

		private static final CorsFilterPlugin PLUGIN = new CorsFilterPluginBuilder()
				.allowLocalhost()
				.allow("http://exact-match.com")
				.allow(Pattern.compile("https://[ab]\\.com"))
				.build();

		@Override
		protected List<Module> getTestOverriders(){
			return List.of(PLUGIN);
		}

	}

}
