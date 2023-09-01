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
package io.datarouter.instrumentation.metric.token;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.instrumentation.metric.token.MetricToken.MetricLiteral;
import io.datarouter.instrumentation.metric.token.MetricToken.MetricTokenVariable;

public class MetricTokenBuilderTests{

	@Test
	public void testPattern(){
		var builder = new MetricTokenBuilder(new MetricLiteral("Root"))
				.and(new MetricTokenVariable("varA", "Variable A").as("123"))
				.and(new MetricLiteral("and"))
				.and(new MetricTokenVariable("varB", "Variable B").as("456"));

		Assert.assertEquals(builder.toMetricName(), "Root 123 and 456");
		Assert.assertEquals(builder.makePatternDto("description").toPatternKey(), "Root <varA> and <varB>");
		Assert.assertEquals(builder.makePatternDto("description").description(), "description");
	}

}
