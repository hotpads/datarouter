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
package io.datarouter.client.mysql.test.test.client.insert.generated;

import java.util.Collection;

import io.datarouter.client.mysql.test.client.insert.generated.PutOpGeneratedTestBean;
import io.datarouter.client.mysql.test.client.insert.generated.PutOpGeneratedTestBeanKey;
import io.datarouter.scanner.Scanner;

public interface PutOpIdGeneratedTest<PK extends PutOpGeneratedTestBeanKey<PK>,D extends PutOpGeneratedTestBean<PK,D>>{

	Scanner<PK> scanKeys();
	void put(D databean);
	void putMulti(Collection<D> databeans);
	void deleteAll();

}
