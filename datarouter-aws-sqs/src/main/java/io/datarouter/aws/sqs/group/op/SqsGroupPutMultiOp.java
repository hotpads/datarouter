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
package io.datarouter.aws.sqs.group.op;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.sqs.model.SendMessageRequest;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.SqsClientManager;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.aws.sqs.op.SqsOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.util.bytes.StringByteTool;

public class SqsGroupPutMultiOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SqsOp<PK,D,F,Void>{

	private final Collection<D> databeans;
	private final byte[] collectionPrefix;
	private final byte[] collectionSeparator;
	private final byte[] collectionSuffix;
	private final int maxBoundedBytesPerMessage;
	private final SqsClientManager sqsClientManager;
	private final ClientId clientId;

	public SqsGroupPutMultiOp(
			Collection<D> databeans,
			Config config,
			BaseSqsNode<PK,D,F> sqsNode,
			SqsClientManager sqsClientManager,
			ClientId clientId){
		super(config, sqsNode);
		this.databeans = databeans;
		this.sqsClientManager = sqsClientManager;
		this.clientId = clientId;
		this.collectionPrefix = StringByteTool.getUtf8Bytes(codec.getCollectionPrefix());
		this.collectionSeparator = StringByteTool.getUtf8Bytes(codec.getCollectionSeparator());
		this.collectionSuffix = StringByteTool.getUtf8Bytes(codec.getCollectionSuffix());
		this.maxBoundedBytesPerMessage = BaseSqsNode.MAX_BYTES_PER_MESSAGE - collectionPrefix.length
				- collectionSuffix.length;
	}

	@Override
	protected Void run(){
		if(databeans.size() == 0){
			return null;
		}
		List<String> rejectedDatabeans = new ArrayList<>();
		List<byte[]> encodedDatabeans = new ArrayList<>();
		for(D databean : databeans){
			String databeanAsString = codec.toString(databean, fielder);
			byte[] databeanAsBytes = StringByteTool.getUtf8Bytes(databeanAsString);
			if(databeanAsBytes.length > maxBoundedBytesPerMessage){
				rejectedDatabeans.add(databeanAsString);
				continue;
			}
			encodedDatabeans.add(databeanAsBytes);
		}
		makeGroups(encodedDatabeans, collectionSeparator, maxBoundedBytesPerMessage).forEach(this::flush);
		if(!rejectedDatabeans.isEmpty()){
			throw new SqsDataTooLargeException(rejectedDatabeans);
		}
		return null;
	}

	private void flush(List<byte[]> group){
		if(group.isEmpty()){
			return;
		}
		String stringGroup = concatGroup(group, collectionPrefix, collectionSuffix, collectionSeparator);
		SendMessageRequest request = new SendMessageRequest(queueUrl, stringGroup);
		sqsClientManager.getAmazonSqs(clientId).sendMessage(request);
	}

	public static Set<List<byte[]>> makeGroups(
			List<byte[]> encodedDatabeans,
			byte[] separator,
			int maxBoundedBytesPerMessage){
		Set<List<byte[]>> groupsToFlush = new HashSet<>();
		List<byte[]> group = new ArrayList<>();
		int groupLengthWithoutSeparators = 0;
		for(byte[] encodedDatabean : encodedDatabeans){
			int totalGroupLength = groupLengthWithoutSeparators + encodedDatabean.length + separator.length * group
					.size();
			if(totalGroupLength > maxBoundedBytesPerMessage){
				groupsToFlush.add(group);
				group = new ArrayList<>();
				groupLengthWithoutSeparators = 0;
			}
			group.add(encodedDatabean);
			groupLengthWithoutSeparators += encodedDatabean.length;
		}
		if(!group.isEmpty()){
			groupsToFlush.add(group);
		}
		return groupsToFlush;
	}

	public static String concatGroup(List<byte[]> group, byte[] prefix, byte[] suffix, byte[] separator){
		ByteArrayOutputStream databeanGroup = new ByteArrayOutputStream();
		databeanGroup.write(prefix, 0, prefix.length);
		for(int i = 0; i < group.size(); i++){
			databeanGroup.write(group.get(i), 0, group.get(i).length);
			if(i < group.size() - 1){
				databeanGroup.write(separator, 0, separator.length);
			}
		}
		databeanGroup.write(suffix, 0, suffix.length);
		return StringByteTool.fromUtf8Bytes(databeanGroup.toByteArray());
	}

}
