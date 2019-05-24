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
package io.datarouter.client.mysql;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.web.MysqlWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.client.ClientTypeRegistry;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

@Singleton
public class MysqlClientType implements ClientType<MysqlClientNodeFactory,MysqlClientManager>{

	private static final String NAME = "mysql";

	@Inject
	public MysqlClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, MysqlWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<MysqlClientNodeFactory> getClientNodeFactoryClass(){
		return MysqlClientNodeFactory.class;
	}

	@Override
	public Class<MysqlClientManager> getClientManagerClass(){
		return MysqlClientManager.class;
	}

	@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
	public static class MysqlClientTypeIntegrationTests{
		@Inject
		private ClientTypeRegistry clientTypeRegistry;

		@Test
		public void testClassLocation(){
			Assert.assertEquals(clientTypeRegistry.get(NAME).getClass(), MysqlClientType.class);
		}
	}

}
