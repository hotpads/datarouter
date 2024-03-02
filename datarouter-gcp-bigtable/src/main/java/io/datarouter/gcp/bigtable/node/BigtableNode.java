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
package io.datarouter.gcp.bigtable.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.cloud.bigtable.data.v2.models.BulkMutation;
import com.google.cloud.bigtable.data.v2.models.Mutation;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.cloud.bigtable.data.v2.models.RowMutationEntry;
import com.google.protobuf.ByteString;

import io.datarouter.gcp.bigtable.service.BigtableClientManager;
import io.datarouter.gcp.bigtable.service.BigtableSchemaUpdateService;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import io.datarouter.util.tuple.Range;

public class BigtableNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BigtableReaderNode<EK,E,PK,D,F>
implements PhysicalSortedMapStorageNode<PK,D,F>{

	private final ClientTableNodeNames clientTableNodeNames;

	public BigtableNode(
			BigtableClientManager manager,
			ClientType<?,?> clientType,
			NodeParams<PK,D,F> params){
		super(manager, params, clientType);
		this.clientTableNodeNames = new ClientTableNodeNames(
				getFieldInfo().getClientId(),
				getFieldInfo().getTableName(),
				getName());
	}

	@Override
	public void delete(PK key, Config config){
		ByteString rowKey = toByteString(key);
		RowMutation mutation = RowMutation.create(clientTableNodeNames.getTableName(), rowKey);
		mutation.deleteRow();
		manager.getTableDataClient(clientTableNodeNames.getClientId())
				.mutateRow(mutation);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		Scanner.of(keys)
				.map(this::toByteString)
				.flush(this::deleteBatch);
	}

	@Override
	public void deleteAll(Config config){
		scanResults(Range.everything(), config, true)
				.map(Row::getKey)
				.batch(config.findRequestBatchSize().orElse(100))
				.forEach(this::deleteBatch);
	}

	@Override
	public void put(D databean, Config config){
		putMulti(Collections.singletonList(databean), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		bulkMutate(databeans);
	}

	private void deleteBatch(List<ByteString> batch){
		BulkMutation bulkMutation = BulkMutation.create(clientTableNodeNames.getTableName());
		Scanner.of(batch)
				.forEach(key -> {
					Mutation mutation = Mutation.create().deleteRow();
					bulkMutation.add(key, mutation);
				});
		manager.getTableDataClient(clientTableNodeNames.getClientId())
				.bulkMutateRows(bulkMutation);
	}

	private void bulkMutate(Collection<D> batch){
		BulkMutation bulkMutation = BulkMutation.create(clientTableNodeNames.getTableName());
		Scanner.of(batch)
				.forEach(item -> {
					int cellsPut = 0;
					ByteString rowKey = toByteString(item.getKey());
					RowMutationEntry mutation = RowMutationEntry.create(rowKey);
					for(Field<?> field : getFieldInfo().getNonKeyFieldsWithValues(item)){
						byte[] columnNameBytes = field.getKey().getColumnNameBytes();
						byte[] valueBytes = field.getValueBytes();
						if(valueBytes == null){
							mutation.deleteCells(
									BigtableSchemaUpdateService.DEFAULT_FAMILY_QUALIFIER,
									ByteString.copyFrom(columnNameBytes));
						}else{
							mutation.setCell(
									BigtableSchemaUpdateService.DEFAULT_FAMILY_QUALIFIER,
									ByteString.copyFrom(columnNameBytes),
									ByteString.copyFrom(valueBytes));
							cellsPut++;
						}
					}
					if(cellsPut == 0){
						mutation.setCell(
								BigtableSchemaUpdateService.DEFAULT_FAMILY_QUALIFIER,
								ByteString.copyFrom(DUMMY_COL_NAME_BYTES),
								ByteString.copyFrom(DUMMY_FIELD_VALUE));
					}
					bulkMutation.add(mutation);
				});
		manager.getTableDataClient(clientTableNodeNames.getClientId())
				.bulkMutateRows(bulkMutation);
	}

}
