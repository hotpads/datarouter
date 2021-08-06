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
package io.datarouter.virtualnode.redundant.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.node.Node;

public class RedundantQueueNodeTool{
	private static final Logger logger = LoggerFactory.getLogger(RedundantQueueNodeTool.class);

	private static final String SQS_MSG = "The input receipt handle";
	private static final String SQS_KEYWORD = "is invalid";
	private static final String GCP_PUBSUB_MSG = "io.grpc.StatusRuntimeException:";
	private static final String GCP_PUBSUB_KEYWORD = "ack ID";

	public static void swallowIfNotFound(RuntimeException exception, Node<?,?,?> node){
		if(exception.getMessage().startsWith(SQS_MSG) && exception.getMessage().contains(SQS_KEYWORD) || exception
				.getMessage().startsWith(GCP_PUBSUB_MSG) && exception.getMessage().contains(GCP_PUBSUB_KEYWORD)){
			logger.info("not found in node={}", node, exception);
			return;
		}
		throw exception;
	}

}
