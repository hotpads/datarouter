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
package io.datarouter.web.test;

import io.datarouter.storage.client.ClientId;

public class DatarouterTestClientIds{

	public static final ClientId
			bigTable = new ClientId("drTestBigTable", true),
			elasticacheMemcached = new ClientId("drTestElasticacheMemcached", true),
			hbase = new ClientId("drTestHBase", true),
			mysql0 = new ClientId("drTestMysql0", true),
			memcached = new ClientId("drTestMemcached", true),
			memory = new ClientId("memory0", true),
			redis = new ClientId("drTestRedis", true),
			spanner = new ClientId("drTestSpanner", true),
			sqs = new ClientId("drTestSqs", true);
}