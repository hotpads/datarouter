/**
 *
 */
package com.hotpads.datarouter.node.type.writebehind.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.DRCounters;

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
				boolean overdue = writeWrapper.getAgeMs() > node.timeoutMs;
				if(writeWrapper.write.isDone() || overdue){
					if(overdue){
						logger.warn("cancelling overdue write on {}", writeWrapper.opDesc);
						DRCounters.incOp(null, "writeBehind timeout on " + node);
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
