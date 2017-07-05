package io.datarouter.util.concurrent;

import java.util.concurrent.Semaphore;

public class SemaphoreTool{

	public static void acquire(Semaphore semaphore){
		try{
			semaphore.acquire();
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

}
