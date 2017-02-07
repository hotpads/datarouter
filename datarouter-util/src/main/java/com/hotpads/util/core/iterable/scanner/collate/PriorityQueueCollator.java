package com.hotpads.util.core.iterable.scanner.collate;

import java.util.PriorityQueue;

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class PriorityQueueCollator<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	//all scanners in pq should be active, meaning scanner.getCurrent() != null
	private final PriorityQueue<SortedScanner<T>> pq = new PriorityQueue<>();
	private final Long limit;
	private long offset;
	private T current;
	private SortedScanner<T> nextScanner;

	public PriorityQueueCollator(Iterable<? extends SortedScanner<T>> scanners, Long limit){
		this.limit = limit;
		this.offset = 0;
		for(SortedScanner<T> scanner : DrIterableTool.nullSafe(scanners)){
			if(scanner.advance()){
				pq.add(scanner);
			}
		}
		this.nextScanner = pq.poll();
	}

	public PriorityQueueCollator(Iterable<? extends SortedScanner<T>> scanners){
		this(scanners, null);
	}


	@Override
	public T getCurrent() {
		return current;
	}


	@Override
	public boolean advance(){
		current = null;
		if(limit != null && offset >= limit){
			return false;
		}
		if(nextScanner == null){
			return false;
		}
		current = nextScanner.getCurrent();
		updateNextScanner();
		++offset;
		return true;
	}


	private void updateNextScanner(){
		if( ! nextScanner.advance()){//this scanner finished.  grab one from the pq
			nextScanner = pq.poll();
			return;
		}
		if(pq.isEmpty()){
			return;//we're on the last scanner, so nothing else to check
		}
		if(nextScanner.compareTo(pq.peek()) < 0){//optimization to skip the pq
			return;//currentScanner is still the best choice
		}
		//optimization failed.  get a new scanner from the PriorityQueue
		SortedScanner<T> tempScanner = nextScanner;
		nextScanner = pq.poll();//should not be null
		pq.add(tempScanner);

	}

}
