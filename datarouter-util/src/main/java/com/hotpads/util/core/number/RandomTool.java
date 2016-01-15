package com.hotpads.util.core.number;

import java.util.Random;

public class RandomTool{
	
	private static Random random = new Random();

	public static short nextPositiveByte(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){ return (byte)(value % Byte.MAX_VALUE); }
		}
	}

	public static short nextPositiveShort(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){ return (short)(value % Short.MAX_VALUE); }
		}
	}

	public static int nextPositiveInt(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){ return value; }
		}
	}
	
	public static long nextPositiveLong(){
		return nextPositiveLong(random);
	}

	public static long nextPositiveLong(Random random){
		while(true){
			long value = random.nextLong();
			if(value > 0){ return value; }
		}
	}
	
	/**
	 * 
	 * @param min inclusive
	 * @param max inclusive
	 */
	public static int getRandomIntBetweenTwoNumbers(int min, int max){
		return (int)Math.floor(Math.random() * (max - min +1)) + min;
	}
	
}
