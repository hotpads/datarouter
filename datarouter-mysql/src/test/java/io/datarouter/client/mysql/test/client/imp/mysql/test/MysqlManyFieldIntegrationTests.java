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
package io.datarouter.client.mysql.test.client.imp.mysql.test;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.exception.DuplicateEntrySqlException;
import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.storage.test.node.basic.manyfield.BaseManyFieldIntegrationTests;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class MysqlManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterMysqlTestClientids.MYSQL, MysqlManyFieldTypeBeanFielder::new, Optional.empty());
	}

	@Test
	public void testNullKey(){
		var bean = new ManyFieldBean();
		dao.put(bean);
		Assert.assertNotNull(bean.getKey().getId());
	}

	@Test(expectedExceptions = DuplicateEntrySqlException.class)
	public void testPutMethod(){
		var bean = new ManyFieldBean();
		dao.put(bean);
		dao.putOrBust(bean);
	}

	public static class MysqlManyFieldTypeBeanFielder extends ManyFieldTypeBeanFielder{
	}

}
