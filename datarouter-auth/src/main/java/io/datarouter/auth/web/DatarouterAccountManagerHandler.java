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
package io.datarouter.auth.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthFiles;
import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.service.DatarouterAccountAvailableEndpointsProvider;
import io.datarouter.auth.service.DefaultDatarouterAccountAvailableEndpointsProvider;
import io.datarouter.auth.service.DefaultDatarouterAccountKeysSupplier;
import io.datarouter.auth.storage.account.BaseDatarouterAccountDao;
import io.datarouter.auth.storage.account.DatarouterAccount;
import io.datarouter.auth.storage.account.DatarouterAccountKey;
import io.datarouter.auth.storage.accountpermission.BaseDatarouterAccountPermissionDao;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission;
import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermissionKey;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.react.bootstrap4.Bootstrap4ReactPageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJs;
import io.datarouter.web.user.session.CurrentSessionInfo;

public class DatarouterAccountManagerHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountManagerHandler.class);

	private final BaseDatarouterAccountDao datarouterAccountDao;
	private final BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao;
	private final DatarouterProperties datarouterProperties;
	private final DatarouterAuthFiles files;
	private final DatarouterAccountAvailableEndpointsProvider datarouterAccountAvailableEndpointsProvider;
	private final Bootstrap4ReactPageFactory reactPageFactory;
	private final DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys;
	private final CurrentSessionInfo currentSessionInfo;

	private final String path;

	@Inject
	public DatarouterAccountManagerHandler(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao,
			DatarouterProperties datarouterProperties,
			DatarouterAuthFiles files,
			DatarouterAuthPaths paths,
			DefaultDatarouterAccountAvailableEndpointsProvider defaultDatarouterAccountAvailableEndpointsProvider,
			Bootstrap4ReactPageFactory reactPageFactory,
			DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys,
			CurrentSessionInfo currentSessionInfo){
		this(datarouterAccountDao,
				datarouterAccountPermissionDao,
				datarouterProperties,
				files,
				defaultDatarouterAccountAvailableEndpointsProvider,
				reactPageFactory,
				defaultDatarouterAccountKeys,
				currentSessionInfo,
				paths.admin.accounts.toSlashedString());
	}

	protected DatarouterAccountManagerHandler(
			BaseDatarouterAccountDao datarouterAccountDao,
			BaseDatarouterAccountPermissionDao datarouterAccountPermissionDao,
			DatarouterProperties datarouterProperties,
			DatarouterAuthFiles files,
			DatarouterAccountAvailableEndpointsProvider datarouterAccountAvailableEndpointsProvider,
			Bootstrap4ReactPageFactory reactPageFactory,
			DefaultDatarouterAccountKeysSupplier defaultDatarouterAccountKeys,
			CurrentSessionInfo currentSessionInfo,
			String path){
		this.datarouterAccountDao = datarouterAccountDao;
		this.datarouterAccountPermissionDao = datarouterAccountPermissionDao;
		this.datarouterProperties = datarouterProperties;
		this.files = files;
		this.datarouterAccountAvailableEndpointsProvider = datarouterAccountAvailableEndpointsProvider;
		this.reactPageFactory = reactPageFactory;
		this.defaultDatarouterAccountKeys = defaultDatarouterAccountKeys;
		this.currentSessionInfo = currentSessionInfo;
		this.path = path;
	}

	@Handler(defaultHandler = true)
	public Mav index(){
		return reactPageFactory.startBuilder(request)
				.withTitle("Datarouter Account Manager")
				.withRequires(DatarouterWebRequireJs.SORTTABLE)
				.withReactScript(files.js.accountManagerJsx)
				.withJsStringConstant("REACT_BASE_PATH", request.getContextPath() + path + "/")
				.buildMav();
	}

	@Handler
	public List<DatarouterAccountDetails> list(){
		return datarouterAccountDao.scan()
				.map(this::getDetailsForAccount)
				.list();
	}

	@Handler
	public DatarouterAccountDetails getDetails(String accountName){
		return getDetailsForAccountName(accountName);
	}

	@Handler
	public DatarouterAccountDetails add(String accountName){
		Require.isTrue(!accountName.isEmpty());
		String creator = getSessionInfo().getRequiredSession().getUsername();
		DatarouterAccount account = new DatarouterAccount(accountName, new Date(), creator);
		datarouterAccountDao.put(account);
		logAction(accountName, "add");
		return getDetailsForAccount(account);
	}

	@Handler
	public DatarouterAccountDetails resetApiKeyToDefault(String accountName) throws Exception{
		if(!isServerTypeDev()){
			throw new Exception("Default apiKey is only allowed for dev serverType.");
		}
		return updateAccount(accountName,
				account -> account.resetApiKeyToDefault(defaultDatarouterAccountKeys.getDefaultApiKey()),
				"resetApiKeyToDefault");
	}

	@Handler
	public DatarouterAccountDetails resetSecretKeyToDefault(String accountName) throws Exception{
		if(!isServerTypeDev()){
			throw new Exception("Default secretKey is only allowed for dev serverType.");
		}
		return updateAccount(accountName,
				account -> account.resetSecretKeyToDefault(defaultDatarouterAccountKeys.getDefaultSecretKey()),
				"resetSecretKeyToDefault");
	}

	@Handler
	public DatarouterAccountDetails generateApiKey(String accountName){
		return updateAccount(accountName, DatarouterAccount::resetApiKey, "generateApiKey");
	}

	@Handler
	public DatarouterAccountDetails generateSecretKey(String accountName){
		return updateAccount(accountName, DatarouterAccount::resetSecretKey, "generateSecretKey");
	}

	@Handler
	public DatarouterAccountDetails toggleUserMappings(String accountName){
		return updateAccount(accountName, DatarouterAccount::toggleUserMappings, "toggleUserMappings");
	}

	@Handler
	public void delete(String accountName){
		DatarouterAccountKey accountKey = new DatarouterAccountKey(accountName);
		datarouterAccountDao.delete(accountKey);
		DatarouterAccountPermissionKey prefix = new DatarouterAccountPermissionKey(accountName);
		datarouterAccountPermissionDao.deleteWithPrefix(prefix);
		logAction(accountName, "delete");
	}

	@Handler
	public List<String> getAvailableEndpoints(){
		List<String> availableEndpoints = new ArrayList<>();
		availableEndpoints.add(DatarouterAccountPermissionKey.ALL_ENDPOINTS);
		availableEndpoints.addAll(datarouterAccountAvailableEndpointsProvider.getAvailableEndpoints());
		return availableEndpoints;
	}

	@Handler
	public DatarouterAccountDetails addPermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.put(new DatarouterAccountPermission(accountName, endpoint));
		logAction(accountName, "addPermission");
		return getDetails(accountName);
	}

	@Handler
	public DatarouterAccountDetails deletePermission(String accountName, String endpoint){
		datarouterAccountPermissionDao.delete(new DatarouterAccountPermissionKey(accountName, endpoint));
		logAction(accountName, "deletePermission");
		return getDetails(accountName);
	}

	@Handler
	public boolean isServerTypeDev(){
		return StringTool.equalsCaseInsensitive(datarouterProperties.getServerTypeString(), ServerType.DEV
				.getPersistentString());
	}

	private DatarouterAccountDetails updateAccount(String accountName, Consumer<DatarouterAccount> updateFunction,
			String logMessage){
		DatarouterAccount account = datarouterAccountDao.get(new DatarouterAccountKey(accountName));
		updateFunction.accept(account);
		datarouterAccountDao.put(account);
		logAction(accountName, logMessage);
		return getDetails(accountName);
	}

	private DatarouterAccountDetails getDetailsForAccount(DatarouterAccount account){
		List<TextPermission> permissions = datarouterAccountPermissionDao
				.scanKeysWithPrefix(new DatarouterAccountPermissionKey(account.getKey().getAccountName()))
				.map(TextPermission::create)
				.list();
		return new DatarouterAccountDetails(account, permissions);
	}

	public DatarouterAccountDetails getDetailsForAccountName(String accountName){
		DatarouterAccount account = datarouterAccountDao.get(new DatarouterAccountKey(accountName));
		return getDetailsForAccount(account);
	}

	private void logAction(String account, String action){
		logger.warn("account={} action={} by={}", account, action, currentSessionInfo.getNonEmptyUsernameOrElse(request,
				"unknown"));
	}

	public static class DatarouterAccountDetails{

		public final DatarouterAccount account;
		public final List<TextPermission> permissions;

		public DatarouterAccountDetails(DatarouterAccount account, List<TextPermission> permissions){
			this.account = account;
			this.permissions = permissions;
		}

	}

	public static class AvailableRouteSet{

		public final String name;
		public final String className;
		public final List<String> rules;

		public AvailableRouteSet(String name, String className, List<String> rules){
			this.name = name;
			this.className = className;
			this.rules = rules;
		}

	}

	public static class TextPermission{

		public final String accountName;
		public final String endpoint;

		public TextPermission(String accountName, String endpoint){
			this.accountName = accountName;
			this.endpoint = endpoint;
		}

		public static TextPermission create(DatarouterAccountPermissionKey permission){
			return new TextPermission(permission.getAccountName(), permission.getEndpoint());
		}

	}

}
