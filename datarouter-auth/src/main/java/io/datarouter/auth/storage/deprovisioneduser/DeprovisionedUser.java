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
package io.datarouter.auth.storage.deprovisioneduser;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.datarouter.auth.web.deprovisioning.DeprovisionedUserDto;
import io.datarouter.auth.web.deprovisioning.UserDeprovisioningStatusDto;
import io.datarouter.enums.StringEnum;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.field.imp.enums.StringEnumField;
import io.datarouter.model.field.imp.enums.StringEnumFieldKey;
import io.datarouter.model.field.imp.list.DelimitedStringListField;
import io.datarouter.model.field.imp.list.DelimitedStringListFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.web.user.session.service.Role;

public class DeprovisionedUser extends BaseDatabean<DeprovisionedUserKey,DeprovisionedUser>{

	private List<String> roles;
	private UserDeprovisioningStatus status;
	private Instant updated;

	public static class FieldKeys{
		public static final DelimitedStringListFieldKey roles = new DelimitedStringListFieldKey("roles");
		public static final StringEnumFieldKey<UserDeprovisioningStatus> status = new StringEnumFieldKey<>("status",
				UserDeprovisioningStatus.class);
		public static final InstantFieldKey updated = new InstantFieldKey("updated");
	}

	public static class DeprovisionedUserFielder extends BaseDatabeanFielder<DeprovisionedUserKey,DeprovisionedUser>{

		public DeprovisionedUserFielder(){
			super(DeprovisionedUserKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DeprovisionedUser user){
			return List.of(
					new DelimitedStringListField(FieldKeys.roles, user.roles),
					new StringEnumField<>(FieldKeys.status, user.status),
					new InstantField(FieldKeys.updated, user.updated));
		}

	}

	public DeprovisionedUser(){
		super(new DeprovisionedUserKey(null));
	}

	public DeprovisionedUser(String username, Collection<Role> roles, UserDeprovisioningStatus status){
		super(new DeprovisionedUserKey(username));
		setRoles(roles);
		this.status = status;
		this.updated = Instant.now();
	}

	public DeprovisionedUserDto toDto(){
		return new DeprovisionedUserDto(getUsername(), roles, status.dto);
	}

	@Override
	public Supplier<DeprovisionedUserKey> getKeySupplier(){
		return DeprovisionedUserKey::new;
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

	public UserDeprovisioningStatus getStatus(){
		return status;
	}

	public Instant getUpdated(){
		return updated;
	}

	//TODO delete after migration
	public void setUpdated(Instant updated){
		this.updated = updated;
	}

	public static enum UserDeprovisioningStatus implements StringEnum<UserDeprovisioningStatus>{

		DEPROVISIONED("deprovisioned", UserDeprovisioningStatusDto.DEPROVISIONED),
		FLAGGED("flagged", UserDeprovisioningStatusDto.FLAGGED),
		;

		private final String persistentString;
		public final UserDeprovisioningStatusDto dto;

		UserDeprovisioningStatus(String persistentString, UserDeprovisioningStatusDto dto){
			this.persistentString = persistentString;
			this.dto = dto;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		@Override
		public UserDeprovisioningStatus fromPersistentString(String string){
			return fromPersistentStringStatic(string);
		}

		public static UserDeprovisioningStatus fromPersistentStringStatic(String string){
			return StringEnum.getEnumFromString(values(), string, null);
		}

	}

}
