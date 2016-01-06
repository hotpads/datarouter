package com.hotpads.util.core.iterable.scanner.imp;

import java.util.ArrayList;
import java.util.Collections;

import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.iterable.scanner.sorted.BaseSortedScanner;

// Currently only used in tests.
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
		Collections.sort(this.list);
		this.currentIndex = -1;
	}

	@Override
	public boolean advance(){
		++currentIndex;
		return currentIndex < list.size();
	}

	@Override
	public T getCurrent(){
		if(currentIndex < 0 || currentIndex >= list.size()){
			return null;
		}
		return list.get(currentIndex);
	}

	@Override
	public String toString(){
		return ListBackedSortedScanner.class.getSimpleName() + "[" + currentIndex + ":" + list.get(currentIndex) + "]";
	}

}
