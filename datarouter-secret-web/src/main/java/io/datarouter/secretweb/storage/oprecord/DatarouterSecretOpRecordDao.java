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
package io.datarouter.secretweb.storage.oprecord;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.Scanner;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.service.SecretOpRecorder;
import io.datarouter.secret.service.SecretOpRecorderSupplier;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecord.DatarouterSecretOpRecordFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.util.string.StringTool;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSecretOpRecordDao extends BaseDao implements SecretOpRecorder{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterSecretOpRecordDao.class);

	public static class DatarouterSecretOpRecordDaoParams extends BaseRedundantDaoParams{
		public DatarouterSecretOpRecordDaoParams(List<ClientId> clientId){
			super(clientId);
		}

	}

	private final SortedMapStorageNode<DatarouterSecretOpRecordKey,DatarouterSecretOpRecord,
	DatarouterSecretOpRecordFielder> node;

	@Inject
	public DatarouterSecretOpRecordDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterSecretOpRecordDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<
							DatarouterSecretOpRecordKey,
							DatarouterSecretOpRecord,
							DatarouterSecretOpRecordFielder> node = nodeFactory.create(
									clientId,
									DatarouterSecretOpRecord::new,
									DatarouterSecretOpRecordFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	@Override
	public void recordOp(SecretOp<?,?,?,?> secretOp){
		String reasonString = secretOp.getSecretOpReason().toString();
		if(reasonString.length() > CommonFieldSizes.DEFAULT_LENGTH_VARCHAR){
			logger.warn("SecretOpReason too long: {}. Max length={}", reasonString, CommonFieldSizes
					.DEFAULT_LENGTH_VARCHAR);
			reasonString = StringTool.trimToSize(reasonString, CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
		}
		node.put(new DatarouterSecretOpRecord(
				secretOp.getNamespace(),
				secretOp.getName(),
				secretOp.getSecretClientOpType(),
				secretOp.getSecretOpReason().type(),
				reasonString));
	}

	public DatabeanVacuum<DatarouterSecretOpRecordKey,DatarouterSecretOpRecord> makeVacuum(){
		Instant deleteBeforeTime = Instant.now().minus(30L, ChronoUnit.DAYS);
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> databean.getKey().getDate().isBefore(deleteBeforeTime),
				node::deleteMulti)
				.build();
	}

	@Singleton
	public static class DaoSecretOpRecorderSupplier implements SecretOpRecorderSupplier{

		@Inject
		private DatarouterInjector injector;

		@Override
		public SecretOpRecorder get(){
			return injector.getInstance(DatarouterSecretOpRecordDao.class);
		}

	}

}
