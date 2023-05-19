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
package io.datarouter.web.user.role;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DatarouterRoleManagerIntegrationTests{

	private DatarouterRoleManager roleManager = new DatarouterRoleManager();

	@Test
	private final void testRoleEnumContainsNecessaryBaseRolePersistentStrings(){
		for(DatarouterUserRole baseRole : DatarouterUserRole.values()){
			String baseString = baseRole.getPersistentString();
			try{
				RoleEnum<?> impl = roleManager.getRoleEnum().fromPersistentString(baseString);
				Assert.assertEquals(impl.getPersistentString(), baseString);
			}catch(RuntimeException e){
				Assert.fail(roleManager.getRoleEnum().getClass() + " is missing persistent string " + baseString);
			}
		}
	}

}
