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
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUserKey;

public interface BaseDatarouterUserDao{

	DatarouterUser get(DatarouterUserKey key);
	DatarouterUser getByUserToken(DatarouterUserByUserTokenLookup key);
	DatarouterUser getByUsername(DatarouterUserByUsernameLookup key);

	List<DatarouterUser> getMulti(Collection<DatarouterUserKey> keys);
	List<DatarouterUser> getMultiByUserTokens(Collection<DatarouterUserByUserTokenLookup> keys);
	List<DatarouterUser> getMultiByUsername(Collection<DatarouterUserByUsernameLookup> keys);

	Optional<DatarouterUser> find(DatarouterUserKey key);
	Optional<DatarouterUser> find(DatarouterUserByUserTokenLookup key);

	Scanner<DatarouterUser> scan();

	void put(DatarouterUser databean);
	void putMulti(Collection<DatarouterUser> databeans);

	void delete(DatarouterUserKey key);

	long count();

	boolean exists(DatarouterUserByUserTokenLookup key);

	static class NoOpDatarouterUserDao implements BaseDatarouterUserDao{

		@Override
		public DatarouterUser get(DatarouterUserKey key){
			return null;
		}

		@Override
		public DatarouterUser getByUserToken(DatarouterUserByUserTokenLookup key){
			return null;
		}

		@Override
		public DatarouterUser getByUsername(DatarouterUserByUsernameLookup key){
			return null;
		}

		@Override
		public List<DatarouterUser> getMulti(Collection<DatarouterUserKey> key){
			return Collections.emptyList();
		}

		@Override
		public List<DatarouterUser> getMultiByUserTokens(Collection<DatarouterUserByUserTokenLookup> keys){
			return Collections.emptyList();
		}

		@Override
		public List<DatarouterUser> getMultiByUsername(Collection<DatarouterUserByUsernameLookup> keys){
			return Collections.emptyList();
		}

		@Override
		public Optional<DatarouterUser> find(DatarouterUserKey key){
			return Optional.empty();
		}

		@Override
		public Optional<DatarouterUser> find(DatarouterUserByUserTokenLookup key){
			return Optional.empty();
		}

		@Override
		public Scanner<DatarouterUser> scan(){
			return Scanner.empty();
		}

		@Override
		public void put(DatarouterUser databean){
		}

		@Override
		public void putMulti(Collection<DatarouterUser> databean){
		}

		@Override
		public void delete(DatarouterUserKey key){
		}

		@Override
		public long count(){
			return 0;
		}

		@Override
		public boolean exists(DatarouterUserByUserTokenLookup key){
			return false;
		}

	}

}
