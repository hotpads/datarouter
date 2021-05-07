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
package io.datarouter.auth.storage.account;

import java.util.Collection;
import java.util.Optional;

import io.datarouter.scanner.Scanner;

public interface BaseDatarouterAccountCredentialDao{

	void put(DatarouterAccountCredential databean);
	void insertOrBust(DatarouterAccountCredential databean);
	void updateIgnore(DatarouterAccountCredential databean);
	void updateMultiIgnore(Collection<DatarouterAccountCredential> databeans);
	DatarouterAccountCredential get(DatarouterAccountCredentialKey key);
	Optional<DatarouterAccountCredential> find(DatarouterAccountCredentialKey key);
	Scanner<DatarouterAccountCredential> scan();
	Scanner<DatarouterAccountCredential> scanMulti(Collection<DatarouterAccountCredentialKey> keys);
	Scanner<DatarouterAccountCredentialKey> scanKeys();
	Scanner<DatarouterAccountCredential> scanByAccountName(Collection<String> accountNames);
	boolean exists(DatarouterAccountCredentialKey key);
	void delete(DatarouterAccountCredentialKey key);
	void deleteByAccountName(String accountName);

}
