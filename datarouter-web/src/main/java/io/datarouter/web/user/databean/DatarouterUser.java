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
package io.datarouter.web.user.databean;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.StringListToBinaryCsvFieldCodec;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayEncodedField;
import io.datarouter.model.field.imp.array.ByteArrayEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.key.unique.base.BaseStringUniqueKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.IterableScanner;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.role.Role;
import io.datarouter.web.user.session.service.SessionBasedUser;

public class DatarouterUser extends BaseDatabean<DatarouterUserKey,DatarouterUser> implements SessionBasedUser{

	private String username;
	private String userToken;
	private String passwordSalt;
	private String passwordDigest;
	private Boolean enabled;
	private List<String> roles;
	private Date created;
	private Date lastLoggedIn;
	private String zoneId;

	public static class FieldKeys{
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
		public static final StringFieldKey passwordSalt = new StringFieldKey("passwordSalt");
		public static final StringFieldKey passwordDigest = new StringFieldKey("passwordDigest")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final BooleanFieldKey enabled = new BooleanFieldKey("enabled");
		public static final ByteArrayEncodedFieldKey<List<String>> roles
				= new ByteArrayEncodedFieldKey<>("roles", StringListToBinaryCsvFieldCodec.INSTANCE)
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		@SuppressWarnings("deprecation")
		public static final DateFieldKey created = new DateFieldKey("created");
		@SuppressWarnings("deprecation")
		public static final DateFieldKey lastLoggedIn = new DateFieldKey("lastLoggedIn");
		public static final StringFieldKey zoneId = new StringFieldKey("zoneId");
	}

	public static class DatarouterUserFielder extends BaseDatabeanFielder<DatarouterUserKey,DatarouterUser>{

		public DatarouterUserFielder(){
			super(DatarouterUserKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(DatarouterUser user){
			return List.of(
					new StringField(FieldKeys.username, user.username),
					new StringField(FieldKeys.userToken, user.userToken),
					new StringField(FieldKeys.passwordSalt, user.passwordSalt),
					new StringField(FieldKeys.passwordDigest, user.passwordDigest),
					new BooleanField(FieldKeys.enabled, user.enabled),
					new ByteArrayEncodedField<>(FieldKeys.roles, user.roles),
					new DateField(FieldKeys.created, user.created),
					new DateField(FieldKeys.lastLoggedIn, user.lastLoggedIn),
					new StringField(FieldKeys.zoneId, user.zoneId));
		}

		@Override
		public Map<String,List<Field<?>>> getUniqueIndexes(DatarouterUser databean){
			Map<String,List<Field<?>>> indexesByName = new TreeMap<>();
			indexesByName.put("unique_username", new DatarouterUserByUsernameLookup(null).getFields());
			indexesByName.put("unique_userToken", new DatarouterUserByUserTokenLookup(null).getFields());
			return indexesByName;
		}

	}

	public DatarouterUser(){
		super(new DatarouterUserKey());
	}

	public DatarouterUser(Long id){
		super(new DatarouterUserKey(id));
	}

	public DatarouterUser(Long id, String username){
		this(id);
		this.username = username;
	}

	@Override
	public Supplier<DatarouterUserKey> getKeySupplier(){
		return DatarouterUserKey::new;
	}

	public static class DatarouterUserByUsernameLookup extends BaseStringUniqueKey<DatarouterUserKey>{

		public DatarouterUserByUsernameLookup(String username){
			super(username);
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.username, id));
		}

	}

	public static class DatarouterUserByUserTokenLookup extends BaseStringUniqueKey<DatarouterUserKey>{

		public DatarouterUserByUserTokenLookup(String userToken){
			super(userToken);
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.userToken, id));
		}
	}

	public Collection<Role> getRoles(){
		return IterableScanner.ofNullable(roles)
				.map(Role::new)
				.list();
	}

	public void setRoles(Collection<Role> roles){
		this.roles = Scanner.of(roles)
				.map(Role::getPersistentString)
				.sort()
				.distinct()
				.list();
	}

	public DatarouterUser addRoles(Collection<Role> roles){
		setRoles(Scanner.concat(getRoles(), roles).collect(HashSet::new));
		return this;
	}

	public DatarouterUser removeRoles(Collection<Role> roles){
		Set<Role> newRoles = new HashSet<>(getRoles());
		newRoles.removeAll(roles);
		setRoles(newRoles);
		return this;
	}

	public static boolean equals(DatarouterUser first, DatarouterUser second){
		return first.equals(second)
				&& Objects.equals(first.getUsername(), second.getUsername())
				&& Objects.equals(first.getUserToken(), second.getUserToken())
				&& Objects.equals(first.getPasswordSalt(), second.getPasswordSalt())
				&& Objects.equals(first.getPasswordDigest(), second.getPasswordDigest())
				&& Objects.equals(first.getEnabled(), second.getEnabled())
				&& Objects.equals(first.getRoles(), second.getRoles())
				&& Objects.equals(first.getCreated(), second.getCreated())
				&& Objects.equals(first.getLastLoggedIn(), second.getLastLoggedIn())
				&& Objects.equals(first.getZoneId(), second.getZoneId());
	}

	@Deprecated // use getCreatedInstant
	public Date getCreated(){
		return created;
	}

	public Instant getCreatedInstant(){
		return created.toInstant();
	}

	public void setCreated(Date created){
		this.created = created;
	}

	@Override
	public String getUsername(){
		return this.username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getPasswordDigest(){
		return this.passwordDigest;
	}

	public void setPasswordDigest(String passwordDigest){
		this.passwordDigest = passwordDigest;
	}

	@Override
	public Long getId(){
		return getKey().getId();
	}

	public String getPasswordSalt(){
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt){
		this.passwordSalt = passwordSalt;
	}

	public String getUserToken(){
		return userToken;
	}

	@Override
	public String getToken(){
		return getUserToken();
	}

	public void setUserToken(String userToken){
		this.userToken = userToken;
	}

	public Boolean getEnabled(){
		return enabled;
	}

	@Override
	public Boolean isEnabled(){
		return getEnabled();
	}

	public void setEnabled(Boolean enabled){
		this.enabled = enabled;
	}

	public Instant getLastLoggedIn(){
		return Optional.ofNullable(lastLoggedIn)
				.map(Date::toInstant)
				.orElse(null);
	}

	public void setLastLoggedIn(Instant lastLoggedIn){
		if(lastLoggedIn == null){
			this.lastLoggedIn = null;
		}else{
			this.lastLoggedIn = Date.from(lastLoggedIn);
		}
	}

	@Override
	public Optional<ZoneId> getZoneId(){
		return Optional.ofNullable(zoneId)
				.map(ZoneId::of);
	}

	public void setZoneId(ZoneId zoneId){
		this.zoneId = zoneId.getId();
	}

}
