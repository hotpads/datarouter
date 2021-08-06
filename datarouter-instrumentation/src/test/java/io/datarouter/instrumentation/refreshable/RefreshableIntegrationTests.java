/*
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
package io.datarouter.instrumentation.refreshable;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RefreshableIntegrationTests{

	private static class PredictableSupplier implements Supplier<String>{

		private int index = 0;
		private int count = 0;
		private final String[] values;

		//returns each value from get one time, then continues to return the last value forever
		public PredictableSupplier(String[] values){
			this.values = values;
		}

		@Override
		public String get(){
			count++;
			if(index < values.length - 1){
				return values[index++];
			}
			return values[index];
		}

		public int getCount(){
			return count;
		}

	}

	@Test
	public void testSuccessfulInit(){
		String value = "something";
		RefreshableSupplier<String> refreshableSupplier = new RefreshableStringSupplier(() -> value);
		Assert.assertEquals(refreshableSupplier.get(), value);
	}

	@Test
	public void testUnsuccessfulInit(){
		Assert.assertThrows(() -> new RefreshableStringSupplier(null));
		Assert.assertThrows(() -> new RefreshableStringSupplier(() -> null));
	}

	@Test
	public void testMinimumTtlBehavior(){
		String[] values = {"first", "second"};
		PredictableSupplier predictable = new PredictableSupplier(values);
		//refresh will not mutate unless 50ms have elapsed since last refresh
		RefreshableSupplier<String> refreshable = new RefreshableStringSupplier(predictable, Duration.ofMillis(50L),
				Duration.ofMillis(50L));

		//this all executes before the TTL passes, so nothing is mutated
		passTime();
		for(int i = 0; i < 100; i++){
			Assert.assertEquals(refreshable.get(), values[0]);
			Assert.assertTrue(Instant.now().isAfter(refreshable.refresh()));
		}

		//after the minimum TTL has passed, a refresh will trigger a mutation
		passTime(100L);
		Assert.assertTrue(getNowAndPassTime().isBefore(refreshable.refresh()));
		Assert.assertEquals(refreshable.get(), values[1]);
		Assert.assertEquals(refreshable.get(), values[1]);
	}

	@Test
	public void testZeroMinimumTtlBehavior(){
		String[] values = {"first", "second", "third"};
		PredictableSupplier predictable = new PredictableSupplier(values);
		//refresh will always attempt to refresh, since minimum TTL is zero
		RefreshableSupplier<String> refreshable = new RefreshableStringSupplier(predictable, Duration.ZERO, Duration
				.ZERO);

		//each value is returned in order
		for(int i = 0; i < values.length; i++){
			Assert.assertEquals(refreshable.get(), values[i]);
			Assert.assertEquals(refreshable.get(), values[i]);//does not change when called multiple times
			if(i < values.length - 1){
				//every refresh before the last will result in a newer Instant, since the object was mutated
				Assert.assertTrue(getNowAndPassTime().isBefore(refreshable.refresh()));
			}else{
				//the last refresh will not result in a newer Instant (since there was no mutation)
				Assert.assertTrue(getNowAndPassTime().isAfter(refreshable.refresh()));
			}
		}
		//additional refreshes will not result in any changes, since the internal supplier's values are not changing
		Assert.assertEquals(refreshable.get(), values[values.length - 1]);
		Assert.assertTrue(getNowAndPassTime().isAfter(refreshable.refresh()));
		Assert.assertEquals(refreshable.get(), values[values.length - 1]);
	}

	@Test
	public void testAttemptInterval(){
		String[] values = {"first", "first"};
		PredictableSupplier predictable = new PredictableSupplier(values);

		//one refresh happens on instantiation
		//subsequent refreshes will not be allowed within the attemptInterval
		Assert.assertEquals(predictable.getCount(), 0);
		RefreshableSupplier<String> refreshable = new RefreshableStringSupplier(predictable, Duration.ZERO, Duration
				.ofMillis(75L));
		Assert.assertEquals(predictable.getCount(), 1);
		Instant firstRefreshInstant = refreshable.refresh();

		//no refreshes are attempted for 50+ms due to attemptInterval
		for(int i = 0; i < 5; i++){
			Assert.assertEquals(refreshable.get(), values[0]);
			Assert.assertTrue(getNowAndPassTime().isAfter(refreshable.refresh()));
		}
		Assert.assertEquals(predictable.getCount(), 1);

		//after attemptInterval has passed, a new refresh attempt will be made, but since the value is the same, no
		//refresh will happen
		passTime(25L);
		Assert.assertEquals(predictable.getCount(), 1);
		Assert.assertEquals(refreshable.get(), values[0]);
		Assert.assertTrue(getNowAndPassTime().isAfter(refreshable.refresh()));
		Assert.assertEquals(refreshable.refresh(), firstRefreshInstant);
		Assert.assertEquals(predictable.getCount(), 2);
	}

	@Test
	public void testSupplierFailure(){
		String[] values = {"first", null};
		PredictableSupplier predictable = new PredictableSupplier(values);
		RefreshableSupplier<String> refreshable = new RefreshableStringSupplier(predictable, Duration.ZERO);

		//if null is returned, the supplier continues to return the last successful value without error
		for(int i = 0; i < 10; i++){
			Assert.assertEquals(refreshable.get(), values[0]);
			Assert.assertTrue(getNowAndPassTime().isAfter(refreshable.refresh()));
		}
	}

	private static void passTime(){
		passTime(5L);
	}

	private static void passTime(long duration){
		try{
			Thread.sleep(duration);
		}catch(InterruptedException e){
			throw new RuntimeException();
		}
	}

	//NOTE: getNowAndPassTime avoids issues with comparing Instants in test code, the TTL really is zero
	//passes ~10ms
	private static Instant getNowAndPassTime(){
		passTime();
		Instant now = Instant.now();
		passTime();
		return now;
	}

}
