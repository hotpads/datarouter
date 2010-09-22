/**
 * 
 */
package com.hotpads.datarouter.node.base.writebehind;

import org.apache.log4j.Logger;

import com.hotpads.profile.count.collection.Counters;
import com.hotpads.profile.count.collection.archive.CountArchiveFlusher;
import com.hotpads.util.core.ExceptionTool;

public class OverdueWriteCanceller implements Runnable{
	static Logger logger = Logger.getLogger(CountArchiveFlusher.class);
	
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
						Counters.inc("writeBehind timeout on "+node.getName());
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