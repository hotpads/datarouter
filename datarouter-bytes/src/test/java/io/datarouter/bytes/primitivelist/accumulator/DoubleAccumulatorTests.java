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
package io.datarouter.bytes.primitivelist.accumulator;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DoubleAccumulatorTests{

	@Test
	public void testEmpty(){
		var accumulator = new DoubleAccumulator();
		Assert.assertEquals(accumulator.stream().count(), 0);
		Assert.assertEquals(accumulator.size(), 0);
		Assert.assertTrue(accumulator.isEmpty());
	}

	@Test
	public void testSmall(){
		var accumulator = new DoubleAccumulator();
		accumulator.add(0.1);
		accumulator.add(0.2);
		Assert.assertEquals(
				accumulator.stream().toArray(),
				new double[]{0.1, 0.2});
		Assert.assertEquals(accumulator.get(0), 0.1);
		Assert.assertEquals(accumulator.get(1), 0.2);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> accumulator.get(2));

	}

	@Test
	public void testMultiPage(){
		double[] inputs = new double[13];
		for(int i = 0; i < inputs.length; ++i){
			inputs[i] = i;
		}
		var accumulator = new DoubleAccumulator(10);
		accumulator.addMulti(inputs);
		Assert.assertEquals(
				accumulator.toPrimitiveArray(),
				inputs);
		Assert.assertEquals(
				accumulator.stream().toArray(),
				inputs);
	}

	@Test
	public void testSet(){
		var accumulator = new DoubleAccumulator(2);
		accumulator.addAll(List.of(0.1, 0.2, 0.3, 0.4, 0.5));
		accumulator.set(4, -0.5);
		Assert.assertEquals(accumulator, List.of(0.1, 0.2, 0.3, 0.4, -0.5));
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> accumulator.set(5, 0));
	}
}
