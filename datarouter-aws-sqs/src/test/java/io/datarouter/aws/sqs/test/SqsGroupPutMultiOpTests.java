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
package io.datarouter.aws.sqs.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.aws.sqs.group.op.SqsGroupPutMultiOp;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.collection.SetTool;

public class SqsGroupPutMultiOpTests{

	private static final int MAX_BOUNDED_BYTES = 10;
	private static final byte[] SEPARATOR = ",".getBytes();

	@Test
	public void testConcatGroups(){
		List<byte[]> group = Stream.of("foo", "bar", "baz")
				.map(StringByteTool::getUtf8Bytes)
				.collect(Collectors.toList());
		Assert.assertEquals(SqsGroupPutMultiOp.concatGroup(group, "{".getBytes(), "}".getBytes(), SEPARATOR),
				"{foo,bar,baz}");
		Assert.assertEquals(SqsGroupPutMultiOp.concatGroup(Arrays.asList("foo".getBytes()), "{".getBytes(), "}"
				.getBytes(), SEPARATOR), "{foo}");
	}

	@Test
	public void testMakeGroups(){
		byte[] fakeDatabean1 = new byte[MAX_BOUNDED_BYTES];
		List<byte[]> fakeDatabeans = new ArrayList<>(Arrays.asList(fakeDatabean1));
		Set<List<byte[]>> groups = SqsGroupPutMultiOp.makeGroups(fakeDatabeans, SEPARATOR, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, SetTool.of(fakeDatabeans));

		byte[] fakeDatabean2 = new byte[MAX_BOUNDED_BYTES];
		fakeDatabeans.add(fakeDatabean2);
		groups = SqsGroupPutMultiOp.makeGroups(fakeDatabeans, SEPARATOR, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, SetTool.of(
				Arrays.asList(fakeDatabean1),
				Arrays.asList(fakeDatabean2)));

		byte[] fakeDatabean3 = new byte[MAX_BOUNDED_BYTES / 2];
		fakeDatabeans.add(fakeDatabean3);
		groups = SqsGroupPutMultiOp.makeGroups(fakeDatabeans, SEPARATOR, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, SetTool.of(
				Arrays.asList(fakeDatabean1),
				Arrays.asList(fakeDatabean2),
				Arrays.asList(fakeDatabean3)));

		byte[] fakeDatabean4 = new byte[MAX_BOUNDED_BYTES / 2];
		fakeDatabeans.add(fakeDatabean4);
		groups = SqsGroupPutMultiOp.makeGroups(fakeDatabeans, SEPARATOR, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, SetTool.of(
				Arrays.asList(fakeDatabean1),
				Arrays.asList(fakeDatabean2),
				Arrays.asList(fakeDatabean3),
				Arrays.asList(fakeDatabean4)));

		byte[] fakeDatabean5 = new byte[MAX_BOUNDED_BYTES / 2 - SEPARATOR.length];
		fakeDatabeans.add(fakeDatabean5);
		groups = SqsGroupPutMultiOp.makeGroups(fakeDatabeans, SEPARATOR, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, SetTool.of(
				Arrays.asList(fakeDatabean1),
				Arrays.asList(fakeDatabean2),
				Arrays.asList(fakeDatabean3),
				Arrays.asList(fakeDatabean4, fakeDatabean5)));
	}

}
