/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.number;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTool{

	private static final Random random = new Random();

	public static short nextPositiveByte(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){
				return (byte)(value % Byte.MAX_VALUE);
			}
		}
	}

	public static short nextPositiveShort(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){
				return (short)(value % Short.MAX_VALUE);
			}
		}
	}

	public static int nextPositiveInt(){
		return nextPositiveInt(random);
	}

	public static int nextPositiveInt(int max){
		return random.nextInt(max);
	}

	public static int nextPositiveInt(Random random){
		while(true){
			int value = random.nextInt();
			if(value > 0){
				return value;
			}
		}
	}

	public static long nextPositiveLong(long max){
		return ThreadLocalRandom.current().nextLong(max);
	}

	public static long nextPositiveLong(){
		return nextPositiveLong(random);
	}

	public static long nextPositiveLong(Random random){
		while(true){
			long value = random.nextLong();
			if(value > 0){
				return value;
			}
		}
	}

	/**
	 * @param min inclusive
	 * @param max inclusive
	 */
	public static int getRandomIntBetweenTwoNumbers(int min, int max){
		return (int)Math.floor(Math.random() * (max - min + 1)) + min;
	}

	public static int getRandomNonZeroPercent(){
		return getRandomIntBetweenTwoNumbers(1, 100);
	}
}
