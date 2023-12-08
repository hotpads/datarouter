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
package io.datarouter.auth.storage.user.roleapprovals;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.roleapprovals.DatarouterUserRoleApproval.DatarouterUserRoleApprovalFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserRoleApprovalDao extends BaseDao{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterUserRoleApprovalDao.class);

	public record DatarouterUserRoleApprovalDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<
			DatarouterUserRoleApprovalKey,
			DatarouterUserRoleApproval,
			DatarouterUserRoleApprovalFielder> node;

	@Inject
	public DatarouterUserRoleApprovalDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterUserRoleApprovalDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<
							DatarouterUserRoleApprovalKey,
							DatarouterUserRoleApproval,
							DatarouterUserRoleApprovalFielder> node = nodeFactory.create(
									clientId,
									DatarouterUserRoleApproval::new,
									DatarouterUserRoleApprovalFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void put(DatarouterUserRoleApproval databean){
		node.put(databean);
	}

	public DatarouterUserRoleApproval get(DatarouterUserRoleApprovalKey key){
		return node.get(key);
	}

	public List<DatarouterUserRoleApproval> getAllForUser(DatarouterUser user){
		return node.scanWithPrefix(new DatarouterUserRoleApprovalKey(user.getUsername(), null, null, null)).list();
	}

	public List<DatarouterUserRoleApproval> getAllOutstandingApprovalsForUser(DatarouterUser user){
		return Scanner.of(getAllForUser(user))
				.include(roleApproval -> roleApproval.getAllApprovalRequirementsMetAt() == null)
				.list();
	}

	public void setAllRequirementsMetAtForUserRole(DatarouterUser user, String role){
		Instant allRequirementsMetAt = Instant.now();
		node.scanWithPrefix(new DatarouterUserRoleApprovalKey(user.getUsername(), role, null, null))
				.map(databean -> {
					// don't override previous rounds of approvals for this role.
					if(databean.getAllApprovalRequirementsMetAt() != null){
						return databean;
					}
					return databean.withAllApprovalRequirementsMetAt(allRequirementsMetAt);
				})
				.flush(node::putMulti);
	}

	public void deleteOutstandingApprovals(DatarouterUser user, String role, DatarouterUser editor){
		node.scanWithPrefix(new DatarouterUserRoleApprovalKey(
				user.getUsername(),
				role,
				editor.getUsername(),
				null))
				.include(databean -> databean.getAllApprovalRequirementsMetAt() == null)
				.map(DatarouterUserRoleApproval::getKey)
				.flush(node::deleteMulti);
	}

	public void deleteOutstandingApprovalsOfApprovalTypeForRole(String role, String approvalType){
		Objects.requireNonNull(role);
		Objects.requireNonNull(approvalType);
		logger.warn("Deleting outstanding role approvals for role={} and approvalType={}", role, approvalType);
		node.scan()
				.include(databean -> databean.getAllApprovalRequirementsMetAt() == null
					&& role.equals(databean.getKey().getRequestedRole())
					&& approvalType.equals(databean.getApprovalType()))
				.map(DatarouterUserRoleApproval::getKey)
				.flush(node::deleteMulti);
	}

}
