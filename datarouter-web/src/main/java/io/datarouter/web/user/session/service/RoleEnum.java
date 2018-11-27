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
package io.datarouter.web.user.session.service;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.enums.StringEnum;
import io.datarouter.web.user.role.DatarouterUserRole;

//NOTE: binding/injection of this class can be tricky. It should be injected like "roles" in the test below.
public interface RoleEnum<T> extends StringEnum<T>{

	Role getRole();

	Class<? extends RoleEnumIntegrationTests> getTestClass();

	abstract class RoleEnumIntegrationTests{

		@Inject
		private RoleEnum<? extends RoleEnum<?>> roles;

		@Test
		public final void testRoleEnumContainsNecessaryBaseRolePersistentStrings(){
			for(DatarouterUserRole baseRole : DatarouterUserRole.values()){
				String baseString = baseRole.getPersistentString();
				try{
					RoleEnum<?> impl = roles.fromPersistentString(baseString);
					Assert.assertEquals(impl.getPersistentString(), baseString);
				}catch(RuntimeException e){
					Assert.fail(roles.getClass() + " is missing persistent string " + baseString);
				}
			}
		}

	}

}
