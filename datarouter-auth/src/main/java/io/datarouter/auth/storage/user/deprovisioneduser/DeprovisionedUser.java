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
package io.datarouter.auth.storage.user.deprovisioneduser;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.auth.model.dto.DeprovisionedUserDto;
import io.datarouter.auth.model.dto.UserDeprovisioningStatusDto;
import io.datarouter.auth.role.Role;
import io.datarouter.enums.StringMappedEnum;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.StringListToBinaryCsvFieldCodec;
import io.datarouter.model.field.codec.StringMappedEnumFieldCodec;
import io.datarouter.model.field.imp.StringEncodedField;
import io.datarouter.model.field.imp.StringEncodedFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.Scanner;

public class DeprovisionedUser extends BaseDatabean<DeprovisionedUserKey,DeprovisionedUser>{

	private List<String> roles;
	private UserDeprovisioningStatus status;
	private Instant updated;

	public static class FieldKeys{
		public static final ByteArrayEncodedFieldKey<List<String>> roles
				= new ByteArrayEncodedFieldKey<>("roles", StringListToBinaryCsvFieldCodec.INSTANCE)
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final StringEncodedFieldKey<UserDeprovisioningStatus> status = new StringEncodedFieldKey<>(
				"status",
				new StringMappedEnumFieldCodec<>(UserDeprovisioningStatus.BY_PERSISTENT_STRING));
		public static final InstantFieldKey updated = new InstantFieldKey("updated");
	}

	public static class DeprovisionedUserFielder extends BaseDatabeanFielder<DeprovisionedUserKey,DeprovisionedUser>{

		public DeprovisionedUserFielder(){
			super(DeprovisionedUserKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DeprovisionedUser user){
			return List.of(
					new ByteArrayEncodedField<>(FieldKeys.roles, user.roles),
					new StringEncodedField<>(FieldKeys.status, user.status),
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
		return new DeprovisionedUserDto(
				getUsername(),
				Scanner.of(roles).sort(String.CASE_INSENSITIVE_ORDER).list(),
				status.dto);
	}

	@Override
	public Supplier<DeprovisionedUserKey> getKeySupplier(){
		return DeprovisionedUserKey::new;
	}

	public String getUsername(){
		return getKey().getUsername();
	}

	public Set<Role> getRoles(){
		return Scanner.of(roles)
				.map(Role::new)
				.collect(HashSet::new);
	}

	public void setRoles(Collection<Role> roles){
		this.roles = Scanner.of(roles)
				.map(Role::persistentString)
				.sort()
				.distinct()
				.list();
	}

	public UserDeprovisioningStatus getStatus(){
		return status;
	}

	public Instant getUpdated(){
		return updated;
	}

	public enum UserDeprovisioningStatus{

		DEPROVISIONED("deprovisioned", UserDeprovisioningStatusDto.DEPROVISIONED),
		FLAGGED("flagged", UserDeprovisioningStatusDto.FLAGGED);

		public static final StringMappedEnum<UserDeprovisioningStatus> BY_PERSISTENT_STRING
				= new StringMappedEnum<>(values(), value -> value.persistentString);

		private final String persistentString;
		private final UserDeprovisioningStatusDto dto;

		UserDeprovisioningStatus(String persistentString, UserDeprovisioningStatusDto dto){
			this.persistentString = persistentString;
			this.dto = dto;
		}

	}

}
