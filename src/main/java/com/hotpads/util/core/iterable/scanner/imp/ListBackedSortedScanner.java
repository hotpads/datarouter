package com.hotpads.util.core.iterable.scanner.imp;

import java.util.ArrayList;

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

//i think this class was mostly for tests.  i recommend not using
public class ListBackedSortedScanner<T extends Comparable<? super T>>
extends BaseSortedScanner<T>{

	protected ArrayList<T> list;
	protected int currentIndex;
	
	public ListBackedSortedScanner(Iterable<T> ins){
		if(ins instanceof ArrayList){
			this.list = (ArrayList<T>)ins;
		}else{
			this.list = DrIterableTool.createArrayListFromIterable(ins);
		}
		this.currentIndex = -1;
	}
	
	@Override
	public boolean advance() {
		++currentIndex;
		return currentIndex < list.size();
	}
	
	@Override
	public T getCurrent() {
		if(currentIndex < 0 || currentIndex >= list.size()){ return null; }
		return list.get(currentIndex);
	}
	
}
