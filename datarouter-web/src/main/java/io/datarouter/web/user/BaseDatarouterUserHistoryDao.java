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
package io.datarouter.web.user;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistoryKey;

public interface BaseDatarouterUserHistoryDao{

	void put(DatarouterUserHistory databean);
	List<DatarouterUserHistory> getMulti(Collection<DatarouterUserHistoryKey> keys);

	static class NoOpDatarouterUserHistoryDao implements BaseDatarouterUserHistoryDao{

		@Override
		public void put(DatarouterUserHistory databean){
		}

		@Override
		public List<DatarouterUserHistory> getMulti(Collection<DatarouterUserHistoryKey> keys){
			return Collections.emptyList();
		}

	}

}
