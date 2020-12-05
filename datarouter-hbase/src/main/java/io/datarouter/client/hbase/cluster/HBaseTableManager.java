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
package io.datarouter.client.hbase.cluster;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.hadoop.hbase.client.Admin;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.balancer.HBaseBalancerFactory;
import io.datarouter.httpclient.response.Conditional;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeTool;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public class HBaseTableManager<PK extends PrimaryKey<PK>>{

	@Singleton
	public static class HBaseTableManagerFactory{

		@Inject
		private HBaseClientManager hbaseClientManager;
		@Inject
		private DrRegionListFactory hbaseRegionListFactory;
		@Inject
		private HBaseBalancerFactory hbaseBalancerFactory;

		public <PK extends PrimaryKey<PK>> HBaseTableManager<PK> make(Node<PK,?,?> node){
			PhysicalNode<PK,?,?> physicalNode = NodeTool.extractSinglePhysicalNode(node);
			ClientId clientId = physicalNode.getClientId();
			String tableName = physicalNode.getFieldInfo().getTableName();
			Admin admin = hbaseClientManager.getAdmin(clientId);
			DrServerList servers = new DrServerList(admin);
			Supplier<List<DrRegionInfo<?>>> regionSupplier = () -> hbaseRegionListFactory.make(
					clientId,
					servers,
					tableName,
					physicalNode,
					hbaseBalancerFactory.getBalancerForTable(clientId, tableName))
					.getRegions();
			return new HBaseTableManager<>(physicalNode.getFieldInfo().getPrimaryKeyClass(), regionSupplier);
		}

	}

	private final Class<PK> pkClass;
	private final Supplier<List<DrRegionInfo<?>>> regionSupplier;

	public HBaseTableManager(Class<PK> pkClass, Supplier<List<DrRegionInfo<?>>> regionSupplier){
		this.pkClass = pkClass;
		this.regionSupplier = regionSupplier;
	}

	public Scanner<PK> scanPartitionEndKeys(){
		return Scanner.of(regionSupplier.get())
				.include(region -> region.getPartition() == 0)
				.map(DrRegionInfo::getEndKeyTyped)
				.map(Conditional::orElseThrow)
				.map(Optional::orElseThrow)
				.map(pkClass::cast);
	}

}
