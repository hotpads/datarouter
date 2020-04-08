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
package io.datarouter.auth.storage.deprovisioneduser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.auth.web.deprovisioning.DeprovisionedUserDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.array.DelimitedStringArrayField;
import io.datarouter.model.field.imp.array.DelimitedStringArrayFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.session.service.Role;

public class DeprovisionedUser extends BaseDatabean<DeprovisionedUserKey,DeprovisionedUser>{

	private List<String> roles;

	public static class FieldKeys{
		public static final BooleanFieldKey enabled = new BooleanFieldKey("enabled");
		public static final DelimitedStringArrayFieldKey roles = new DelimitedStringArrayFieldKey("roles");
	}

	public static class DeprovisionedUserFielder extends BaseDatabeanFielder<DeprovisionedUserKey,DeprovisionedUser>{

		public DeprovisionedUserFielder(){
			super(DeprovisionedUserKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DeprovisionedUser user){
			return Arrays.asList(new DelimitedStringArrayField(FieldKeys.roles, user.roles));
		}

	}

	public DeprovisionedUser(){
		super(new DeprovisionedUserKey(null));
	}

	public DeprovisionedUser(String username, Collection<Role> roles){
		super(new DeprovisionedUserKey(username));
		setRoles(roles);
	}

	public DeprovisionedUserDto toDto(){
		return new DeprovisionedUserDto(getUsername(), Scanner.of(roles).sorted(String.CASE_INSENSITIVE_ORDER).list());
	}

	@Override
	public Class<DeprovisionedUserKey> getKeyClass(){
		return DeprovisionedUserKey.class;
	}

	public String getUsername(){
		return getKey().getUsername();
	}

	public Set<Role> getRoles(){
		return roles.stream()
				.map(Role::new)
				.collect(Collectors.toSet());
	}

	public void setRoles(Collection<Role> roles){
		this.roles = roles.stream()
				.map(Role::getPersistentString)
				.sorted()
				.distinct()
				.collect(Collectors.toList());
	}

}
