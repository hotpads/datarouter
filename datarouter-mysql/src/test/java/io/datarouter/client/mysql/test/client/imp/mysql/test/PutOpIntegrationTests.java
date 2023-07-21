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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.test.client.insert.DatarouterPutOpTestDao;
import io.datarouter.client.mysql.test.client.insert.PutOpTestBean;
import io.datarouter.client.mysql.test.client.insert.PutOpTestBeanKey;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class PutOpIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterPutOpTestDao dao;

	@BeforeClass
	public void beforeClass(){
		resetTable();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	private void resetTable(){
		dao.deleteAll();
	}

	@Test
	public void testDefault(){
		Result result = test("testDefault", new Config());
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "qux");
	}

	@Test
	public void testInsertIgnore(){
		Result result = test("testInsertIgnore", new Config().setPutMethod(PutMethod.INSERT_IGNORE));
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "baz");
	}

	@Test
	public void testInsertOnDuplicateUpdate(){
		var config = new Config().setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		Result result = test("testInsertOnDuplicateUpdate", config);
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "qux");
	}

	@Test
	public void testInsertOrBust(){
		var config = new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
		Result result = test("testInsertOrBust", config, false, true);
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "baz");
	}

	@Test
	public void testInsertOrUpdate(){
		Result result = test("testInsertOrUpdate", new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "qux");
	}

	@Test
	public void testUpdateOrInsert(){
		Result result = test("testUpdateOrInsert", new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT));
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "qux");
	}

	@Test
	public void testUpdateOrBust(){
		var config = new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
		Result result = test("testUpdateOrBust", config, true, true);
		Assert.assertNull(result.left());
		Assert.assertNull(result.right());
	}

	@Test
	public void testMerge(){
		Result result = test("testMerge", new Config().setPutMethod(PutMethod.MERGE));
		Assert.assertEquals(result.left(), "baz");
		Assert.assertEquals(result.right(), "qux");
	}

	private Result test(String testName, Config config){
		return test(testName, config, false, false);
	}

	private Result test(
			String testName,
			Config config,
			boolean expectedFirstCaught,
			boolean expectedSecondCaught){
		var bean = new PutOpTestBean(testName, "bar", "baz");
		var bean2 = new PutOpTestBean(testName, "bar", "qux");
		try{
			dao.put(bean, config);
			Assert.assertFalse(expectedFirstCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedFirstCaught);
		}
		String before = nullSafeGetC(dao.get(new PutOpTestBeanKey(testName, "bar")));
		try{
			dao.put(bean2, config);
			Assert.assertFalse(expectedSecondCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedSecondCaught);
		}
		String after = nullSafeGetC(dao.get(new PutOpTestBeanKey(testName, "bar")));

		return new Result(before, after);
	}

	private String nullSafeGetC(PutOpTestBean bean){
		if(bean == null){
			return null;
		}
		return bean.getC();
	}

	@Test
	public void testMultiInsert(){
		int testBatchSize = 10;
		int totalCount = (int) (testBatchSize * 2.5);
		var config = new Config()
				.setRequestBatchSize(testBatchSize)
				.setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		List<PutOpTestBean> databeans = new ArrayList<>();
		for(int count = 0; count < totalCount; count++){
			databeans.add(new PutOpTestBean("testMultiInsert", randomString(), randomString()));
		}
		dao.putMulti(databeans, config);
		Assert.assertEquals(Scanner.of(databeans).map(Databean::getKey).listTo(dao::getMulti).size(), totalCount);
	}


	//Assert mysql is rejecting fields longer than the field size
	@Test
	public void testFieldSize(){
		int maxSize = PutOpTestBeanKey.FieldKeys.first.getSize();
		var sb = new StringBuilder();
		for(int i = 0; i < maxSize; ++i){
			sb.append("0");
		}
		sb.append("excess");//extra chars
		String first = sb.toString();
		Assert.assertTrue(first.length() > maxSize);
		var bean = new PutOpTestBean(first, "bar", "baz");
		Assert.assertThrows(IllegalArgumentException.class, () -> dao.put(bean, new Config()));
	}

	private static final String randomString(){
		return UUID.randomUUID().toString();
	}

	private record Result(
			String left,
			String right){
	}

}
