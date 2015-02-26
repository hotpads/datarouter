package com.hotpads.util.core.iterable.scanner.collate;

import java.util.PriorityQueue;

import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;
import com.hotpads.util.core.iterable.scanner.sorted.SortedScanner;

public class PriorityQueueCollator<T extends Comparable<? super T>> 
extends BaseSortedScanner<T>
implements Collator<T> {

	protected PriorityQueue<SortedScanner<T>> pq = new PriorityQueue<SortedScanner<T>>();
	
	public PriorityQueueCollator(Iterable<? extends SortedScanner<T>> scanners){
		for(SortedScanner<T> scanner : IterableTool.nullSafe(scanners)){
			if(scanner.getCurrent()==null){
				if(!scanner.advance()){ continue; }//omit this empty scanner
			}
			pq.add(scanner);
		}
	}
	
	@Override
	public void add(SortedScanner<T> scanner) {
		//scanner should already be positioned at first element by this point
		pq.add(scanner);
	}
	
	@Override
	public T getCurrent() {
		if(pq.isEmpty()){ return null; }
		return pq.peek().getCurrent();
	}
	
	@Override
	public boolean advance() {
		if(pq.isEmpty()){ return false; }
		SortedScanner<T> previousHead = pq.poll();
		if(previousHead.advance()){
			pq.add(previousHead);
		}
		return true;
	}
	
}
