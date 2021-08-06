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
package io.datarouter.virtualnode.writebehind.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.util.DatarouterCounters;

public class OverdueWriteCanceller implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(OverdueWriteCanceller.class);

	protected final BaseWriteBehindNode<?,?,?> node;

	public OverdueWriteCanceller(BaseWriteBehindNode<?,?,?> node){
		this.node = node;
	}

	@Override
	public void run(){
		try{
			if(node.outstandingWrites == null){
				return;
			}
			while(true){
				OutstandingWriteWrapper writeWrapper = node.outstandingWrites.peek();//don't remove yet
				if(writeWrapper == null){
					break;
				}
				long ageMs = writeWrapper.getAgeMs();
				boolean overdue = ageMs > node.timeoutMs;
				if(writeWrapper.write.isDone() || overdue){
					if(overdue){
						logger.warn("cancelling overdue write on {} ageMs={}", writeWrapper.opDesc, ageMs);
						DatarouterCounters.incOp(null, "writeBehind timeout on " + node);
					}
					node.outstandingWrites.poll();
					continue;
				}
				break;//wait to be triggered again
			}
		}catch(Exception e){
			logger.warn("", e);
		}
	}

}
