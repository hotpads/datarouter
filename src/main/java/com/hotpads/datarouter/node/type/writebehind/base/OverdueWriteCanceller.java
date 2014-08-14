/**
 * 
 */
package com.hotpads.datarouter.node.type.writebehind.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.util.core.ExceptionTool;

public class OverdueWriteCanceller implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(CountArchiveFlusher.class);
	
	protected final BaseWriteBehindNode<?,?,?> node;
	
	public OverdueWriteCanceller(BaseWriteBehindNode<?,?,?> node){
		this.node = node;
	}

	@Override
	public void run(){
		try{
			if(node.outstandingWrites==null){ return; }
			while(true){
				OutstandingWriteWrapper writeWrapper = node.outstandingWrites.peek();//don't remove yet
				if(writeWrapper==null){ break; }
				boolean overdue = writeWrapper.getAgeMs() > node.timeoutMs;
				if(writeWrapper.getWrite().isDone() || overdue){ 
					if(overdue){
						logger.warn("cancelling overdue write on "+node.getName());
						DRCounters.incSuffixOp(null, "writeBehind timeout on "+node.getName());
					}
					node.outstandingWrites.poll(); 
					continue;
				}
				break;//wait to be triggered again
			}
		}catch(Exception e){
			logger.warn(ExceptionTool.getStackTraceAsString(e));
		}
	}
}
