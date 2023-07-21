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
package io.datarouter.gcp.spanner.test.generated;

import java.util.Collection;

import io.datarouter.gcp.spanner.test.SpannerTestCliendIds;
import io.datarouter.gcp.spanner.test.generated.SpannerRandomGeneratedFieldBean.SpannerRandomGeneratedFieldBeanFielder;
import io.datarouter.gcp.spanner.test.generated.SpannerRandomGeneratedFieldBean.SpannerRandomGeneratedFieldBeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerRandomGeneratedFieldDao extends BaseDao implements TestDao{

	private final SortedMapStorage<SpannerRandomGeneratedFieldBeanKey,SpannerRandomGeneratedFieldBean> node;

	@Inject
	public SpannerRandomGeneratedFieldDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(
				SpannerTestCliendIds.SPANNER,
				SpannerRandomGeneratedFieldBean::new,
				SpannerRandomGeneratedFieldBeanFielder::new)
				.buildAndRegister();
	}

	public void deleteAll(){
		node.deleteAll();
	}

	public void putMulti(Collection<SpannerRandomGeneratedFieldBean> beans){
		node.putMulti(beans);
	}

	public Scanner<SpannerRandomGeneratedFieldBean> scan(){
		return node.scan();
	}
}
