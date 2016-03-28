package com.hotpads.util.core.collections;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class LimitedLinkedList<E>extends LinkedList<E>{

	private int maxSize;

	public LimitedLinkedList(int maxSize){
		this.maxSize = maxSize;
	}

	public int getMaxSize(){
		return maxSize;
	}

	public void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}

	@Override
	public boolean add(E ex){
		addLast(ex);
		return true;
	}

	@Override
	public boolean offer(E ex){
		return offerLast(ex);
	}

	@Override
	public boolean addAll(Collection<? extends E> collection){
		if(collection.isEmpty()){
			return false;
		}
		for(E item : collection){
			add(item);
		}
		return true;
	}

	@Override
	public void addFirst(E ex){
		offerFirst(ex);
	}

	@Override
	public void addLast(E ex){
		offerLast(ex);
	}

	@Override
	public boolean offerFirst(E ex){
		if(this.size() >= maxSize){
			this.removeLast();
		}
		super.addFirst(ex);
		return true;
	}

	@Override
	public boolean offerLast(E ex){
		if(this.size() >= maxSize){
			this.removeFirst();
		}
		super.addLast(ex);
		return true;
	}
}
