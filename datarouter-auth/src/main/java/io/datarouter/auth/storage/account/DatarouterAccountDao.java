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
package io.datarouter.auth.storage.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthExecutors.DatarouterAccountByApiKeyCacheExecutor;
import io.datarouter.auth.config.DatarouterAuthSettingRoot;
import io.datarouter.auth.storage.account.DatarouterAccount.DatarouterAccountFielder;
import io.datarouter.auth.storage.account.DatarouterAccountCredential.DatarouterAccountCredentialFielder;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.index.UniqueIndexReader;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterAccountDao extends BaseDao implements BaseDatarouterAccountDao{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountDao.class);

	public static class DatarouterAccountDaoParams extends BaseRedundantDaoParams{

		public final Optional<String> accountTableName;
		public final Optional<String> credentialTableName;

		public DatarouterAccountDaoParams(List<ClientId> clientIds){
			super(clientIds);
			accountTableName = Optional.empty();
			credentialTableName = Optional.empty();
		}

		public DatarouterAccountDaoParams(List<ClientId> clientIds, String accountTableName,
				String credentialTableName){
			super(clientIds);
			Require.isTrue(StringTool.notNullNorEmpty(accountTableName));
			Require.isTrue(StringTool.notNullNorEmpty(credentialTableName));
			this.accountTableName = Optional.of(accountTableName);
			this.credentialTableName = Optional.of(credentialTableName);
		}

	}

	private final SortedMapStorageNode<DatarouterAccountKey,DatarouterAccount,DatarouterAccountFielder> node;
	private final IndexedSortedMapStorageNode<DatarouterAccountCredentialKey,DatarouterAccountCredential,
			DatarouterAccountCredentialFielder> newNode;
	private final UniqueIndexReader<DatarouterAccountCredentialKey,DatarouterAccountCredential,
			DatarouterAccountCredentialByAccountNameKey, FieldlessIndexEntry<
			DatarouterAccountCredentialByAccountNameKey,DatarouterAccountCredentialKey, DatarouterAccountCredential>>
			byAccountName;

	private final AtomicReference<Map<String,DatarouterAccount>> accountByApiKeyCache;
	private final AtomicReference<Map<String,DatarouterAccountCredential>> accountCredentialByApiKeyCache;

	private final Supplier<Boolean> shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount;

	@Inject
	public DatarouterAccountDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			IndexingNodeFactory indexingNodeFactory,
			DatarouterAccountDaoParams params,
			DatarouterAccountByApiKeyCacheExecutor executor,
			DatarouterAuthSettingRoot settings){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var builder = nodeFactory.create(clientId, DatarouterAccount::new, DatarouterAccountFielder::new)
							.withIsSystemTable(true);
					params.accountTableName.ifPresent(builder::withTableName);

					SortedMapStorageNode<DatarouterAccountKey,DatarouterAccount,DatarouterAccountFielder> node = builder
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);

		newNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					var builder = nodeFactory.create(clientId, DatarouterAccountCredential
							::new, DatarouterAccountCredentialFielder::new)
							.withIsSystemTable(true);
					params.credentialTableName.ifPresent(builder::withTableName);

					IndexedSortedMapStorageNode<DatarouterAccountCredentialKey,DatarouterAccountCredential,
							DatarouterAccountCredentialFielder> node = builder.build();
					return node;
				})
				.listTo(RedundantIndexedSortedMapStorageNode::new);
		byAccountName = indexingNodeFactory.createKeyOnlyManagedIndex(DatarouterAccountCredentialByAccountNameKey.class,
				newNode).build();
		datarouter.register(newNode);

		accountByApiKeyCache = new AtomicReference<>(getAccountsByApiKey());
		accountCredentialByApiKeyCache = new AtomicReference<>(getAccountCredentialsByApiKey());
		executor.scheduleWithFixedDelay(this::refreshCaches, 30, 30, TimeUnit.SECONDS);

		this.shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount = settings
				.shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount;
	}

	@Override
	public void put(DatarouterAccount databean){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			node.put(databean);
		}
		newNode.put(toNew(databean));
	}

	@Override
	public void putMulti(Collection<DatarouterAccount> databeans){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			node.putMulti(databeans);
		}
		Scanner.of(databeans).map(DatarouterAccountDao::toNew).flush(newNode::putMulti);
	}

	@Override
	public DatarouterAccount get(DatarouterAccountKey key){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.get(key);
		}
		return byAccountName.scanDatabeansWithPrefix(new DatarouterAccountCredentialByAccountNameKey(key
				.getAccountName(), null))
				.findFirst()//there won't be any duplicates yet, so this is fine
				.map(DatarouterAccountDao::fromNew)
				.orElse(null);
	}

	@Override
	public List<DatarouterAccount> getMulti(Collection<DatarouterAccountKey> keys){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.getMulti(keys);
		}
		return Scanner.of(keys)
				.map(DatarouterAccountKey::getAccountName)
				.map(accountName -> new DatarouterAccountCredentialByAccountNameKey(accountName, null))
				.listTo(byAccountName::scanDatabeansWithPrefixes)
				.sorted(new AccountComparator())
				.deduplicateBy(DatarouterAccountCredential::getAccountName)
				.map(DatarouterAccountDao::fromNew)
				.list();
	}

	@Override
	public Scanner<DatarouterAccount> scan(){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.scan();
		}
		return newNode.scan()
				.map(DatarouterAccountDao::fromNew);
	}

	@Override
	public Scanner<DatarouterAccountKey> scanKeys(){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.scanKeys();
		}
		return newNode.scan()
				.map(DatarouterAccountCredential::getAccountName)
				.map(DatarouterAccountKey::new);
	}

	@Override
	public boolean exists(DatarouterAccountKey key){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.exists(key);
		}
		return byAccountName.scanKeysWithPrefix(new DatarouterAccountCredentialByAccountNameKey(key.getAccountName(),
				null))
				.hasAny();
	}

	@Override
	public void delete(DatarouterAccountKey key){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			node.delete(key);
		}
		byAccountName.scanKeysWithPrefix(new DatarouterAccountCredentialByAccountNameKey(key.getAccountName(), null))
				.map(DatarouterAccountCredentialByAccountNameKey::getTargetKey)
				.flush(newNode::deleteMulti);
	}

	@Override
	public Optional<DatarouterAccount> find(DatarouterAccountKey key){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return node.find(key);
		}
		return byAccountName.scanDatabeansWithPrefix(new DatarouterAccountCredentialByAccountNameKey(key
				.getAccountName(), null))
				.findFirst()
				.map(DatarouterAccountDao::fromNew);
	}

	@Override
	public Optional<DatarouterAccount> getFromAccountByApiKeyCache(String apiKey){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			return Optional.ofNullable(accountByApiKeyCache.get().get(apiKey));
		}
		return Optional.ofNullable(accountCredentialByApiKeyCache.get().get(apiKey))
				.map(DatarouterAccountDao::fromNew);
	}

	private Map<String,DatarouterAccount> getAccountsByApiKey(){
		return node.scan().toMap(DatarouterAccount::getApiKey);
	}

	private Map<String,DatarouterAccountCredential> getAccountCredentialsByApiKey(){
		return newNode.scan().toMap(databean -> databean.getKey().getApiKey());
	}

	private void refreshCaches(){
		if(!shouldUseDatarouterAccountCredentialInsteadOfDatarouterAccount.get()){
			accountByApiKeyCache.set(getAccountsByApiKey());
		}
		accountCredentialByApiKeyCache.set(getAccountCredentialsByApiKey());
	}

	//temporary

	public void migrate(){
		List<String> migrated = new ArrayList<>();
		Set<String> alreadyPresent = newNode.scanKeys()
				.map(DatarouterAccountCredentialKey::getApiKey)
				.collect(Collectors.toSet());
		node.scan()
				.exclude(account -> alreadyPresent.contains(account.getApiKey()))
				.map(DatarouterAccountDao::toNew)
				.each(cred -> migrated.add(cred.getAccountName() + "->" + cred.getKey().getApiKey()))
				.flush(newNode::putMulti);
		logger.warn("migrated accounts: {}", Scanner.of(migrated).collect(Collectors.joining(",", "(", ")")));
	}

	public boolean check(){
		Map<String,DatarouterAccount> accounts = node.scan().toMap(account -> account.getKey().getAccountName());
		Map<String,DatarouterAccountCredential> creds = newNode.scan().toMap(account -> account.getKey().getApiKey());
		List<String> differences = new ArrayList<>();
		accounts.values().forEach(account -> {
			DatarouterAccountCredential cred = creds.get(account.getApiKey());
			if(cred == null){
				differences.add("missing cred for account=" + account);
			}else{
				if(!same(account, cred)){
					differences.add("not same: account=" + account + ",cred=" + cred);
				}
			}
		});
		creds.values().forEach(cred -> {
			if(!accounts.containsKey(cred.getAccountName())){
				differences.add("missing account for cred=" + cred);
			}
		});
		if(!differences.isEmpty()){
			logger.warn("differences exist: {}", Scanner.of(differences).collect(Collectors.joining(",", "[", "]")));
			return true;
		}
		return false;
	}

	private static boolean same(DatarouterAccount account, DatarouterAccountCredential cred){
		return
				Objects.equals(account.getKey().getAccountName(), cred.getAccountName())
				&& Objects.equals(account.getApiKey(), cred.getKey().getApiKey())
				&& Objects.equals(account.getCreated(), cred.getCreated())
				&& Objects.equals(account.getCreator(), cred.getCreatorUsername())
				&& Objects.equals(account.getEnableUserMappings(), cred.getEnableUserMappings())
				&& Objects.equals(account.getLastUsed(), cred.getLastUsed())
				&& Objects.equals(account.getSecretKey(), cred.getSecretKey());
	}

	private static final class AccountComparator implements Comparator<DatarouterAccountCredential>{

		@Override
		public int compare(DatarouterAccountCredential o1, DatarouterAccountCredential o2){
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getAccountName(), o2.getAccountName());
		}

	}

	private static DatarouterAccount fromNew(DatarouterAccountCredential newOne){
		return new DatarouterAccount(newOne.getAccountName(), newOne.getKey().getApiKey(), newOne.getSecretKey(), newOne
				.getCreated(), newOne.getCreatorUsername(), newOne.getLastUsed(), newOne.getEnableUserMappings());
	}

	private static DatarouterAccountCredential toNew(DatarouterAccount oldOne){
		return new DatarouterAccountCredential(oldOne.getApiKey(), oldOne.getKey().getAccountName(), oldOne
				.getSecretKey(), oldOne.getCreated(), oldOne.getCreator(), oldOne.getLastUsed(), oldOne
				.getEnableUserMappings());
	}

}
