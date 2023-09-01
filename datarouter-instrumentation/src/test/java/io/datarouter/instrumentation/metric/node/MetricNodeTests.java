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
package io.datarouter.instrumentation.metric.node;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.count.CountCollector;
import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.metric.node.MetricNodeTests.MetricNodeExample.VariableOne;
import io.datarouter.instrumentation.metric.node.MetricNodeTests.MetricNodeExample.VariableTwo;

public class MetricNodeTests{

	private static final MetricNodeExample METRICS = new MetricNodeExample();

	@Test
	public void testPaths(){
		Assert.assertEquals(
				METRICS.literalOne.toMetricName(),
				"Example root literal-one");
		Assert.assertEquals(
				METRICS.varOne("123abc").toMetricName(),
				"Example root 123abc");
		Assert.assertEquals(
				METRICS.varOne("123abc").literalTwo.toMetricName(),
				"Example root 123abc literal-two");
	}

	@Test
	public void incHeartbeat(){
		var testCountCollector = new CountCollector(){

			public long count = 0;

			@Override
			public void stopAndFlushAll(){
			}

			@Override
			public long increment(String key, long delta){
				return count += delta;
			}

			@Override
			public long increment(String key){
				return count++;
			}

		};

		Counters.addCollector(testCountCollector);
		METRICS.literalOne.count();
		Assert.assertEquals(testCountCollector.count, 1);
		METRICS.literalOne.count(3);
		Assert.assertEquals(testCountCollector.count, 4);
		METRICS.literalOne.count(-2);
		Assert.assertEquals(testCountCollector.count, 2);
	}

	@Test
	public void testNodeReuse(){
		var varOne = METRICS.varOne("abc");

		Assert.assertEquals(varOne.toMetricName(), "Example root abc");
		Assert.assertEquals(varOne.literalTwo.toMetricName(), "Example root abc literal-two");
		Assert.assertEquals(varOne.literalTwo.literal("custom").toMetricName(), "Example root abc literal-two custom");
	}

	@Test
	public void testOverride(){
		//skip single node
		Assert.assertEquals(
				METRICS.override(MetricNode::new, ".*").toMetricName(),
				"Example root .*");
		Assert.assertEquals(
				METRICS.override(VariableOne::new, ".*").literalTwo.toMetricName(),
				"Example root .* literal-two");
		//skip multiple nodes
		Assert.assertEquals(
				METRICS.override(VariableTwo::new, ".*").literalThree.toMetricName(),
				"Example root .* literal-three");
	}

	protected static class MetricNodeExample extends BaseMetricRoot{

		public MetricNodeExample(){
			super("Example root");
		}

		public final MetricNode literalOne = literal("literal-one");

		public VariableOne varOne(String varOne){
			return variable(VariableOne::new, varOne);
		}

		public static class VariableOne extends MetricNodeVariable<VariableOne>{

			public final MetricNode literalTwo = literal("literal-two");

			public VariableOne(){
				super("varOne", "Variable one description", VariableOne::new);
			}

			public VariableTwo varTwo(String varTwo){
				return variable(VariableTwo::new, varTwo);
			}
		}

		public static class VariableTwo extends MetricNodeVariable<VariableTwo>{

			public final MetricNode literalThree = literal("literal-three");

			public VariableTwo(){
				super("varTwp", "Variable two description", VariableTwo::new);
			}
		}

	}

}
