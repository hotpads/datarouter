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
package io.datarouter.storage.node.op.raw.index;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.config.Config;

public interface IndexListener<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>{

	void onPut(D databean, Config config);
	void onPutMulti(Collection<D> databeans, Config config);

	void onDelete(PK key, Config config);
	void onDeleteDatabean(D databean, Config config);
	void onDeleteMulti(Collection<PK> keys, Config config);
	void onDeleteMultiDatabeans(Collection<D> databeans, Config config);
	void onDeleteAll(Config config);

}
