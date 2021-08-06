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
package io.datarouter.storage.config;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public class DatarouterPropertiesTests{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterPropertiesTests.class);

	@Inject
	private DatarouterProperties datarouterProperties;

	@Test
	public void testInternalConfigDirectory(){
		System.getProperties().forEach((key,value) -> {
			logger.warn("property-" + key + "=" + value);
		});
		System.getenv().forEach((key,value) -> {
			logger.warn("env-" + key + "=" + value);
		});
		Assert.assertNotEquals("production", datarouterProperties.getInternalConfigDirectory());
	}

	@Test
	public void isInTestng(){
		boolean isTestNgTest = Scanner.of(new Exception().getStackTrace())
				.anyMatch(element -> element.getClassName().startsWith("org.testng"));
		Assert.assertTrue(isTestNgTest);
	}

}
