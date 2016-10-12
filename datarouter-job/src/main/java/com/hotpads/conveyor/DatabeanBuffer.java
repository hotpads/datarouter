package com.hotpads.conveyor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class DatabeanBuffer<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{

	private final String name;
	private final Queue<D> queue;


	public DatabeanBuffer(String name, int maxSize){
		this.name = name;
		this.queue = new ArrayBlockingQueue<>(maxSize);
	}


	public String getName(){
		return name;
	}

	public boolean offer(D databean){
		boolean accepted = queue.offer(databean);
		if(!accepted){
			ConveyorCounters.inc(this, "offer rejected", 1);
		}
		return accepted;
	}

	public boolean offer(Collection<D> databeans){
		for(D databean : databeans){
			if(!offer(databean)){
				return false;
			}
		}
		return true;
	}

	public List<D> poll(int limit){
		List<D> result = new ArrayList<>();
		while(result.size() < limit){
			D databean = queue.poll();
			if(databean == null){
				break;
			}
			result.add(databean);
		}
		return result;
	}

}
