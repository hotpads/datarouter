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
package io.datarouter.secretweb.storage.oprecord;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.secret.service.SecretOp;
import io.datarouter.secret.service.SecretOpReason;
import io.datarouter.secret.service.SecretOpRecorder;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecord.DatarouterSecretOpRecordFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.util.string.StringTool;

@Singleton
public class DatarouterSecretOpRecordDao extends BaseDao implements SecretOpRecorder{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterSecretOpRecordDao.class);

	private final SortedMapStorage<DatarouterSecretOpRecordKey,DatarouterSecretOpRecord> node;

	public static class DatarouterSecretOpRecordDaoParams extends BaseDaoParams{

		public DatarouterSecretOpRecordDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	@Inject
	public DatarouterSecretOpRecordDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterSecretOpRecordDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, DatarouterSecretOpRecord::new, DatarouterSecretOpRecordFielder::new)
				.buildAndRegister();
	}

	@Override
	public void recordOp(String namespace, String name, SecretOp secretOp, SecretOpReason reason){
		String reasonString = reason.toString();
		if(reasonString.length() > CommonFieldSizes.DEFAULT_LENGTH_VARCHAR){
			logger.warn("SecretOpReason too long: {}. Max length={}", reasonString,
					CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
			reasonString = StringTool.trimToSize(reasonString, CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
		}
		node.put(new DatarouterSecretOpRecord(namespace, name, secretOp, reason.type, reasonString));
	}

}
