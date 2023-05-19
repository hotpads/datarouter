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
package io.datarouter.auth.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.role.Role;

public class DatarouterUserEditServiceTests{

	@Test
	public void testChangeListSingleAdd(){
		var before = generateRoleSet(List.of("user", "requestor"));
		var after = generateRoleSet(List.of("testerTools", "user", "requestor"));

		String output = DatarouterUserEditService.changeList("role", before, after);
		Assert.assertEquals(output, "role added: [testerTools]");
	}

	@Test
	public void testChangeListSingleRemove(){
		var before = generateRoleSet(List.of("user", "requestor", "testerTools"));
		var after = generateRoleSet(List.of("user", "requestor"));

		String output = DatarouterUserEditService.changeList("role", before, after);
		Assert.assertEquals(output, "role removed: [testerTools]");
	}

	@Test
	public void testChangeListMultiAddRemove(){
		var before = generateRoleSet(List.of("rep", "admin", "user", "requestor"));
		var after = generateRoleSet(List.of("viewerTools", "testerTools", "user", "requestor"));

		String output = DatarouterUserEditService.changeList("role", before, after);
		Assert.assertEquals(output, "roles added: [testerTools, viewerTools] roles removed: [admin, rep]");
	}

	@Test
	public void testChangeListNoChanges(){
		var before = generateRoleSet(List.of("user", "requestor"));
		var after = generateRoleSet(List.of("user", "requestor"));

		String output = DatarouterUserEditService.changeList("role", before, after);
		Assert.assertEquals(output, "No changes");
	}

	private static Set<String> generateRoleSet(List<String> roleStrings){
		return Scanner.of(roleStrings)
				.map(Role::new)
				.map(Role::toString)
				.collect(HashSet::new);
	}
}
