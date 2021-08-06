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
package io.datarouter.client.mysql.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.testng.IClassListener;
import org.testng.ITestClass;

/**
 * TestNG listener for turning on sql logging
 */
public class JdbcSpyToggler implements IClassListener{

	@Override
	public void onBeforeClass(ITestClass testClass){
		Configurator.setLevel(testClass.getName(), Level.INFO);
		Configurator.setLevel("net.sf.log4jdbc", Level.DEBUG);
		Configurator.setLevel("jdbc.sqltiming", Level.DEBUG);
	}

	@Override
	public void onAfterClass(ITestClass testClass){
		Configurator.setLevel("net.sf.log4jdbc", Level.OFF);
		Configurator.setLevel("jdbc.sqltiming", Level.OFF);
	}

}
