package com.hotpads.datarouter.node.scanner.primarykey;

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

//TODO should share a base class with MergeScanner?
public class PrimaryKeyMergeScanner<PK extends PrimaryKey<PK>,D extends Databean<PK>>
implements PeekableIterable<PK>,PeekableIterator<PK>{

	ArrayList<PeekableIterator<PK>> scanners;
	Map<PrimaryKeyScanner<PK,D>,PK> nextByScanner;
	
	PK peeked;
	
	public PrimaryKeyMergeScanner(Collection<PeekableIterable<PK>> scanners){
		this.scanners = ListTool.createArrayListWithSize(scanners);
		for(PeekableIterable<PK> scannerIterable : IterableTool.nullSafe(scanners)){
			this.scanners.add(scannerIterable.iterator());
		}
		this.nextByScanner = MapTool.createHashMap();
	}
	
	@Override
	public PeekableIterator<PK> iterator(){
		return this;
	};
	
	@Override
	public PK peek(){
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
		for(Iterator<PK> scanner : scanners){
			if(scanner.hasNext()){ return true; }
		}
		return false;
	}

	@Override
	public PK next() {
		//TODO use CollatedIterator code from commons-collections, or if google-guava ever releases it
		if(peeked!=null){
			PK next = peeked;
			peeked = null;
			return next;
		}
		if(scanners==null){ return null; }
		PeekableIterator<PK> minScanner = null;
		for(int i=0; i < scanners.size(); ++i){
			PeekableIterator<PK> testScanner = scanners.get(i);
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
