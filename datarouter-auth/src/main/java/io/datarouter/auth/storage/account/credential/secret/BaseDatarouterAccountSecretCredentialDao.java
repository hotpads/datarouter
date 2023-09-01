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
package io.datarouter.auth.storage.account.credential.secret;

import java.util.Collection;

import io.datarouter.scanner.Scanner;

public interface BaseDatarouterAccountSecretCredentialDao{

	void insertOrBust(DatarouterAccountSecretCredential databean);
	void updateIgnore(DatarouterAccountSecretCredential databean);
	void updateMultiIgnore(Collection<DatarouterAccountSecretCredential> databeans);
	DatarouterAccountSecretCredential get(DatarouterAccountSecretCredentialKey key);
	Scanner<DatarouterAccountSecretCredential> scan();
	Scanner<DatarouterAccountSecretCredential> scanMulti(Collection<DatarouterAccountSecretCredentialKey> keys);
	void delete(DatarouterAccountSecretCredentialKey key);

}
