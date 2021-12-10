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
package io.datarouter.storage.serialize;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.serialize.JsonDatabeanTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBeanKey;
import io.datarouter.storage.test.node.basic.manyfield.TestEnum;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.util.lang.ReflectionTool;

public class JsonDatabeanToolTests{

	private final ManyFieldTypeBeanFielder fielder = new ManyFieldTypeBeanFielder();
	private final SortedBeanFielder sortedBeanFielder = new SortedBeanFielder();

	private static ManyFieldBean makeTestBean(){
		ManyFieldBean bean = new ManyFieldBean(33333L);
		bean.setBooleanField(false);
		bean.setByteField((byte)-55);
		bean.setDoubleField(-79.245);
		bean.setFloatField(45.12345f);
		bean.setIntegerField(-9876);
		bean.setIntEnumField(TestEnum.fish);
		bean.setLongDateField(new Date());
		bean.setLongField(-87658765876L);
		bean.setShortField((short)-30000);
		bean.setStringEnumField(TestEnum.beast);
		bean.setStringField("_%crazy-string\\asdf");
		return bean;
	}

	@Test
	public void testRoundTrip(){
		ManyFieldBeanKey keyIn = new ManyFieldBeanKey(12345L);
		JsonObject keyJsonObject = JsonDatabeanTool.primaryKeyToJson(keyIn, fielder.getKeyFielder());
		ManyFieldBeanKey keyOut = JsonDatabeanTool.primaryKeyFromJson(ManyFieldBeanKey.class, fielder.getKeyFielder(),
				keyJsonObject);
		Assert.assertEquals(keyOut, keyIn);

		ManyFieldBean beanIn = makeTestBean();

		JsonObject databeanJson = JsonDatabeanTool.databeanToJson(beanIn, fielder);
		Supplier<ManyFieldBean> supplier = ReflectionTool.supplier(ManyFieldBean.class);
		ManyFieldBean beanOut = JsonDatabeanTool.databeanFromJson(supplier, fielder, databeanJson);
		Assert.assertTrue(beanIn.equalsAllPersistentFields(beanOut));
	}

	@Test
	public void testBeanWithJson(){
		ManyFieldBean beanIn = makeTestBean();
		JsonObject databeanJson = JsonDatabeanTool.databeanToJson(beanIn, fielder);
		JsonObject innerJson = new JsonObject();
		innerJson.addProperty("snorkle", "bazooka");
		databeanJson.add(ManyFieldBean.FieldKeys.stringField.getName(), innerJson);
		beanIn.setStringField(innerJson.toString());
		Supplier<ManyFieldBean> supplier = ReflectionTool.supplier(ManyFieldBean.class);
		ManyFieldBean beanOut = JsonDatabeanTool.databeanFromJson(supplier, fielder, databeanJson);
		Assert.assertTrue(beanIn.equalsAllPersistentFields(beanOut));
	}

	@Test
	public void testMultiRoundTrip(){
		SortedBeanKey key0 = new SortedBeanKey("a", "b", 0, "d");
		SortedBeanKey key1 = new SortedBeanKey("a", "b", 1, "dasdf");
		SortedBeanKey key2 = new SortedBeanKey("a", "basdf", 2, "sdsdsd");
		List<SortedBeanKey> keysIn = List.of(key0, key1, key2);
		JsonArray jsonKeys = JsonDatabeanTool.primaryKeysToJson(keysIn, sortedBeanFielder.getKeyFielder());
		List<SortedBeanKey> keysOut = JsonDatabeanTool.primaryKeysFromJson(SortedBeanKey.class, sortedBeanFielder
				.getKeyFielder(), jsonKeys);
		Assert.assertEquals(keysOut.size(), 3);
		Assert.assertEquals(keysOut.toArray(), keysIn.toArray());

		SortedBean bean0 = new SortedBean(key0, "1", 2L, null, 45.67d);
		SortedBean bean1 = new SortedBean(key1, "ert", -987654L, "cheesetoast", -45.67d);
		List<SortedBean> databeansIn = List.of(bean0, bean1);
		JsonArray jsonDatabeans = JsonDatabeanTool.databeansToJson(databeansIn, sortedBeanFielder);
		List<SortedBean> databeansOut = JsonDatabeanTool.databeansFromJson(ReflectionTool.supplier(SortedBean.class),
				sortedBeanFielder, jsonDatabeans);
		Assert.assertEquals(databeansOut.size(), 2);
		Assert.assertEquals(databeansOut.toArray(), databeansIn.toArray());
		Assert.assertEquals(Scanner.of(databeansOut).map(Databean::getKey).list(), keysIn.subList(0,2));
	}

}
