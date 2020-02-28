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
package io.datarouter.client.mysql.connection;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class C3p0StatsServiceTests{

	@Test
	public void testExtractClientName(){
		String testValue = "testValue";
		Optional<String> clientName;
		clientName = C3p0StatsService.extractClientName("jdbc:mysql://host:port?param1Key=param1Value&"
				+ MysqlConnectionPoolHolder.CLIENT_NAME_KEY + testValue);
		Assert.assertEquals(clientName, Optional.of(testValue));
		clientName = C3p0StatsService.extractClientName("jdbc:mysql://host:port?param1Key=param1Value");
		Assert.assertEquals(clientName, Optional.empty());
	}

}
