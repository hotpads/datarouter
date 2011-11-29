package com.hotpads.datarouter.node.scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.PeekableIterator;

@Deprecated//migrate to com.hotpads.util.core.iterable.scanner.collate.PriorityQueueCollator
public class MergeScanner<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
implements PeekableIterable<D>,PeekableIterator<D>{

	ArrayList<PeekableIterator<D>> scanners;
	Map<Scanner<PK,D>,D> nextByScanner;
	
	D peeked;
	
	public MergeScanner(Collection<PeekableIterable<D>> scanners){
		this.scanners = ListTool.createArrayListWithSize(scanners);
		for(PeekableIterable<D> scannerIterable : IterableTool.nullSafe(scanners)){
			this.scanners.add(scannerIterable.iterator());
		}
		this.nextByScanner = MapTool.createHashMap();
	}
	
	@Override
	public PeekableIterator<D> iterator(){
		return this;
	};
	
	@Override
	public D peek(){
		if(peeked!=null){
			return peeked;
		}
		if(hasNext()){
			peeked = next();
		}
		return peeked;
	}

	@Override
	public boolean hasNext() {
		if(peeked!=null){ return true; }
		if(scanners==null){ return false; }
		for(Iterator<D> scanner : scanners){
			if(scanner.hasNext()){ return true; }
		}
		return false;
	}

	@Override
	public D next() {
		//TODO use CollatedIterator code from commons-collections, or if google-guava ever releases it
		if(peeked!=null){
			D next = peeked;
			peeked = null;
			return next;
		}
		if(scanners==null){ return null; }
		PeekableIterator<D> minScanner = null;
		for(int i=0; i < scanners.size(); ++i){
			PeekableIterator<D> testScanner = scanners.get(i);
			if(!testScanner.hasNext()){ continue; }
			if(minScanner==null){ 
				minScanner = testScanner;
				continue;
			}else{
				if(testScanner.peek().compareTo(minScanner.peek()) < 0){
					minScanner = testScanner;
				}
			}
		}
		if(minScanner==null){
			return null;
		}
		return minScanner.next();
	}

	@Override
	public void remove() {
		throw new DataAccessException("cannot modify a scanner");
	}
	
	
}
