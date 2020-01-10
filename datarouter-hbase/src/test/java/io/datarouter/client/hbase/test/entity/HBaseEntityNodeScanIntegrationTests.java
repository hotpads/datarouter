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
package io.datarouter.client.hbase.test.entity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.entity.databean.DatarouterHBaseBeanTestEntityDao;
import io.datarouter.client.hbase.test.entity.databean.HBaseBeanTest;
import io.datarouter.client.hbase.test.entity.databean.HBaseBeanTestEntityKey;
import io.datarouter.client.hbase.test.entity.databean.HBaseBeanTestKey;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseEntityNodeScanIntegrationTests{

	/* Based on number of partitions and scattering prefix, this is the key order when inserted into hbase
	 *
	 * HBaseBeanTestEntityKey.aa/1
	 * HBaseBeanTestEntityKey.cc/1
	 * HBaseBeanTestEntityKey.ee/1
	 * HBaseBeanTestEntityKey.bb/1
	 * HBaseBeanTestEntityKey.dd/1
	 * HBaseBeanTestEntityKey.ff/1
	 * HBaseBeanTestEntityKey.ff/2
	 */

	private static final HBaseBeanTestEntityKey
			EK_aa_1 = new HBaseBeanTestEntityKey("aa", "1"),
			EK_bb_1 = new HBaseBeanTestEntityKey("bb", "1"),
			EK_cc_1 = new HBaseBeanTestEntityKey("cc", "1"),
			EK_dd_1 = new HBaseBeanTestEntityKey("dd", "1"),
			EK_ee_1 = new HBaseBeanTestEntityKey("ee", "1"),
			EK_ff_1 = new HBaseBeanTestEntityKey("ff", "1"),
			EK_ff_2 = new HBaseBeanTestEntityKey("ff", "2");

	private static final List<HBaseBeanTestEntityKey> EKS = Arrays.asList(
			EK_aa_1,
			EK_bb_1,
			EK_cc_1,
			EK_dd_1,
			EK_ee_1,
			EK_ff_1,
			EK_ff_2);

	private static final List<HBaseBeanTest> BEANS = Arrays.asList(
			new HBaseBeanTest(new HBaseBeanTestKey(EK_aa_1, "kid", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_bb_1, "cat", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_bb_1, "pig", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_cc_1, "hen", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_cc_1, "rat", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_cc_1, "bat", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_dd_1, "emu", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_dd_1, "bee", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_dd_1, "ant", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_dd_1, "ape", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_ee_1, "cow", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ee_1, "hog", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ee_1, "eel", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ee_1, "man", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ee_1, "yak", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_ff_1, "ass", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ff_1, "doe", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ff_1, "cob", "c"), "f3"),

			new HBaseBeanTest(new HBaseBeanTestKey(EK_ff_2, "pen", "c"), "f3"),
			new HBaseBeanTest(new HBaseBeanTestKey(EK_ff_2, "cod", "c"), "f3"));

	@Inject
	private DatarouterHBaseBeanTestEntityDao dao;

	@BeforeTest
	private void beforeTest(){
		dao.deleteMultiEntities(EKS);
		dao.putMulti(BEANS);
	}

	@AfterTest
	private void afterTest(){
		dao.deleteMultiEntities(EKS);
	}

	@Test
	public void testGet(){
		Assert.assertEquals(dao.getEntity(EK_cc_1).getNumDatabeans(), 3);
	}

	@Test
	public void testListExclusive(){
		test(EK_aa_1, true, 20, 7);
		test(EK_aa_1, false, 20, 6);
		test(EK_aa_1, false, 6, 6);
		test(EK_bb_1, false, 200, 5);
		test(EK_cc_1, false, 1, 1);
		test(EK_cc_1, false, 2, 2);
		test(EK_cc_1, false, 3, 3);
		test(EK_cc_1, false, 4, 4);
		test(EK_cc_1, false, 5, 4);
		test(EK_cc_1, false, 10, 4);
		test(EK_dd_1, false, 0, 0);
		test(EK_ee_1, false, 1, 1);
		test(EK_ee_1, false, 1000, 2);
		test(EK_ff_1, false, 1, 1);
		test(EK_ff_1, false, 2, 1);
	}

	private void test(HBaseBeanTestEntityKey key, boolean startKeyInclusive, int limit, int expected){
		List<HBaseBeanTestEntityKey> keys = dao.getEntityKeys(key, startKeyInclusive, limit);
		Assert.assertEquals(keys.size(), expected);
	}

}
