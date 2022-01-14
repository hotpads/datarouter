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
package io.datarouter.aws.sqs.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.serialize.StringDatabeanCodec;
import io.datarouter.model.serialize.codec.JsonDatabeanCodec;

public class SqsGroupPutMultiOpTests{

	private static final int MAX_BOUNDED_BYTES = 10;
	private static final StringDatabeanCodec CODEC = new JsonDatabeanCodec();

	@Test
	public void testConcatGroups(){
		List<byte[]> group = Stream.of("foo", "bar", "baz")
				.map(StringCodec.UTF_8::encode)
				.collect(Collectors.toList());
		Assert.assertEquals(CODEC.concatGroup(group), "[foo,bar,baz]");
		Assert.assertEquals(CODEC.concatGroup(List.of("foo".getBytes())), "[foo]");
	}

	@Test
	public void testMakeGroups(){
		byte[] fakeDatabean1 = new byte[MAX_BOUNDED_BYTES];
		List<byte[]> fakeDatabeans = new ArrayList<>();
		fakeDatabeans.add(fakeDatabean1);
		List<List<byte[]>> groups = CODEC.makeGroups(fakeDatabeans, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, List.of(fakeDatabeans));

		byte[] fakeDatabean2 = new byte[MAX_BOUNDED_BYTES];
		fakeDatabeans.add(fakeDatabean2);
		groups = CODEC.makeGroups(fakeDatabeans, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, List.of(
				List.of(fakeDatabean1),
				List.of(fakeDatabean2)));

		byte[] fakeDatabean3 = new byte[MAX_BOUNDED_BYTES / 2];
		fakeDatabeans.add(fakeDatabean3);
		groups = CODEC.makeGroups(fakeDatabeans, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, List.of(
				List.of(fakeDatabean1),
				List.of(fakeDatabean2),
				List.of(fakeDatabean3)));

		byte[] fakeDatabean4 = new byte[MAX_BOUNDED_BYTES / 2];
		fakeDatabeans.add(fakeDatabean4);
		groups = CODEC.makeGroups(fakeDatabeans, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, List.of(
				List.of(fakeDatabean1),
				List.of(fakeDatabean2),
				List.of(fakeDatabean3),
				List.of(fakeDatabean4)));

		byte[] fakeDatabean5 = new byte[MAX_BOUNDED_BYTES / 2 - CODEC.getCollectionSeparatorBytes().length];
		fakeDatabeans.add(fakeDatabean5);
		groups = CODEC.makeGroups(fakeDatabeans, MAX_BOUNDED_BYTES);
		Assert.assertEquals(groups, List.of(
				List.of(fakeDatabean1),
				List.of(fakeDatabean2),
				List.of(fakeDatabean3),
				List.of(fakeDatabean4, fakeDatabean5)));
	}

}
