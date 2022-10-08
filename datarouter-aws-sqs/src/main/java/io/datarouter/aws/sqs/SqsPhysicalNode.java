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
package io.datarouter.aws.sqs;

import java.util.function.Supplier;

import io.datarouter.aws.sqs.service.QueueUrlAndName;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public interface SqsPhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends PhysicalNode<PK,D,F>{

	Supplier<QueueUrlAndName> getQueueUrlAndName();

	/**
	 * gets the automatically configured namespace. this might differ from manually specified namespaces.
	 * @return namespace
	 */
	String getAutomaticNamespace();

	/**
	 * builds the expected queue name
	 * @param environmentName environment to use
	 * @param serviceName serviceName to use
	 * @return automatically generated queue name for the parameters, or manually configured one if present
	 */
	String buildQueueName(String environmentName, String serviceName);

	boolean getAgeMonitoringStatusForMetricAlert();

}