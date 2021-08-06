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
package io.datarouter.gcp.spanner.test;

import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.spanner.SpannerTestNgModuleFactory;
import io.datarouter.gcp.spanner.test.generated.SpannerRandomGeneratedFieldBean;
import io.datarouter.gcp.spanner.test.generated.SpannerRandomGeneratedFieldDao;
import io.datarouter.scanner.Scanner;

@Guice(moduleFactory = SpannerTestNgModuleFactory.class)
public class SpannerRandomGeneratedFieldTests{

	@Inject
	private SpannerRandomGeneratedFieldDao dao;

	@AfterMethod
	public void deleteAll(){
		dao.deleteAll();
	}

	@Test
	public void testPut(){
		Scanner.of(new SpannerRandomGeneratedFieldBean("Test1"),
				new SpannerRandomGeneratedFieldBean("Test2"),
				new SpannerRandomGeneratedFieldBean(0L, "Test3")).flush(dao::putMulti);

		List<SpannerRandomGeneratedFieldBean> beans = dao.scan().list();
		Assert.assertEquals(beans.size(), 3);
		beans.forEach(bean -> Assert.assertNotNull(bean.getKey().getId()));
		Assert.assertEquals(beans.get(0).getKey().getId(), Long.valueOf(0L));
	}

}
