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
package io.datarouter.joblet.storage.jobletrequest;

import java.util.List;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.key.FieldlessIndexEntryPrimaryKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.storage.node.op.index.IndexUsage;
import io.datarouter.storage.node.op.index.IndexUsage.IndexUsageType;

@IndexUsage(usageType = IndexUsageType.IGNORE_USAGE)
public class JobletRequestByTypeAndDataSignatureKey
extends BaseRegularPrimaryKey<JobletRequestByTypeAndDataSignatureKey>
implements FieldlessIndexEntryPrimaryKey<JobletRequestByTypeAndDataSignatureKey,JobletRequestKey,JobletRequest>{

	private String type;
	private Long dataSignature;
	private Integer executionOrder;
	private Long created;
	private Integer batchSequence;

	// used by datarouter reflection
	public JobletRequestByTypeAndDataSignatureKey(){
	}

	public JobletRequestByTypeAndDataSignatureKey(
			String type,
			Long dataSignature,
			Integer executionOrder,
			Long created,
			Integer batchSequence){
		this.type = type;
		this.dataSignature = dataSignature;
		this.executionOrder = executionOrder;
		this.created = created;
		this.batchSequence = batchSequence;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(JobletRequestKey.FieldKeys.type, type),
				new LongField(JobletRequest.FieldKeys.dataSignature, dataSignature),
				new IntegerField(JobletRequestKey.FieldKeys.executionOrder, executionOrder),
				new LongField(JobletRequestKey.FieldKeys.created, created),
				new IntegerField(JobletRequestKey.FieldKeys.batchSequence, batchSequence));
	}

	@Override
	public JobletRequestKey getTargetKey(){
		return new JobletRequestKey(
				type,
				executionOrder,
				created,
				batchSequence);
	}

	@Override
	public FieldlessIndexEntry<
			JobletRequestByTypeAndDataSignatureKey,
			JobletRequestKey,
			JobletRequest>
	createFromDatabean(JobletRequest target){
		var index = new JobletRequestByTypeAndDataSignatureKey(
				target.getKey().getType(),
				target.getDataSignature(),
				target.getKey().getExecutionOrder(),
				target.getKey().getCreated(),
				target.getKey().getBatchSequence());
		return new FieldlessIndexEntry<>(JobletRequestByTypeAndDataSignatureKey::new, index);
	}

	public Long getDataSignature(){
		return dataSignature;
	}

}
