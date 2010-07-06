package com.hotpads.profile.count;



public class CounterManager implements Counter{

	protected long managerStartMs;
	protected long latestStartMs;
	protected long nextStartMs;
	protected long periodMs;
	protected long retainForMs;
	protected Counter counter;

	protected int numToRetain;
	protected Counter[] archive;
//	protected Bits pendingFlushFlags;

	public CounterManager(long periodMs, long retainForMs){
		long now = System.currentTimeMillis();
		this.periodMs = periodMs;
		this.retainForMs = retainForMs;
		this.numToRetain = (int)(retainForMs / periodMs);
		this.managerStartMs = now - (now % periodMs);
		this.archive = new Counter[numToRetain];
//		this.pendingFlushFlags = new LongBits();
		this.roll();//init
	}
	
	public Counter getCounter(){
		return counter;
	}
	
	public synchronized void rollIfNecessary(){
		if(System.currentTimeMillis() >= nextStartMs){
			roll();
		}
	}
	
	public synchronized void roll(){
		//archive the old one
		Counter oldCounter = counter;
		long oldStart = latestStartMs;
		int oldIndex = (int)(oldStart % numToRetain);
		archive[oldIndex] = oldCounter;
		
		//init the new one
		long now = System.currentTimeMillis();
		latestStartMs = now - (now % periodMs);
		nextStartMs = latestStartMs + periodMs;
		counter = new AtomicCounter();
	}
	
//	public List<Counter> getUnflushedCounters(){
//		return pendingFlushFlags.get
//	}

	@Override
	public long increment(String key){
		return counter.increment(key);
	}

	@Override
	public long increment(String key, long delta){
		return counter.increment(key, delta);
	}
	
	
	
}
