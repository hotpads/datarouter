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
package io.datarouter.pathnode;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.inject.Singleton;

public class PathNodeTests{

	@Singleton
	public static class TestPaths extends PathNode{

		public final BPaths aa = branch(BPaths::new, "aa");

		public static class BPaths extends PathNode{
			public final CPaths bb = branch(CPaths::new, "bb");
		}

		public static class CPaths extends PathNode{
			public final PathNode cc = leaf("cc");
		}

	}

	@Test
	public void testJoin(){
		TestPaths paths = new TestPaths();

		Assert.assertEquals(paths.aa.join("/"), "aa");
		Assert.assertEquals(paths.aa.bb.join("/"), "aa/bb");
		Assert.assertEquals(paths.aa.bb.cc.join("/"), "aa/bb/cc");
		Assert.assertEquals(paths.aa.bb.cc.join(""), "aabbcc");
		Assert.assertEquals(paths.aa.bb.cc.join(" dog "), "aa dog bb dog cc");

		Assert.assertEquals(paths.aa.bb.cc.join("/", "/", "/"), "/aa/bb/cc/");
		Assert.assertEquals(paths.aa.bb.cc.join("/", "/", ""), "/aa/bb/cc");
		Assert.assertEquals(paths.aa.bb.cc.join("", "/", "/"), "aa/bb/cc/");
		Assert.assertEquals(paths.aa.bb.cc.join("", "/", ""), "aa/bb/cc");
	}

	@Test
	public void testToStringArray(){
		TestPaths paths = new TestPaths();
		Assert.assertEquals(paths.aa.toStringArray(), new String[]{"aa"});
		Assert.assertEquals(paths.aa.bb.toStringArray(), new String[]{"aa", "bb"});
		Assert.assertEquals(paths.aa.bb.cc.toStringArray(), new String[]{"aa", "bb", "cc"});
	}

	@Test
	public void testToSlashedString(){
		TestPaths paths = new TestPaths();
		Assert.assertEquals(paths.aa.toSlashedString(), "/aa");
		Assert.assertEquals(paths.aa.bb.toSlashedString(), "/aa/bb");
		Assert.assertEquals(paths.aa.bb.cc.toSlashedString(), "/aa/bb/cc");
	}

	@Test
	public void testToSlashedStringWithEndingSlash(){
		TestPaths paths = new TestPaths();
		Assert.assertEquals(paths.aa.toSlashedStringWithTrailingSlash(), "/aa/");
		Assert.assertEquals(paths.aa.bb.toSlashedStringWithTrailingSlash(), "/aa/bb/");
		Assert.assertEquals(paths.aa.bb.cc.toSlashedStringWithTrailingSlash(), "/aa/bb/cc/");
	}

	@Test
	public void testNodesAfter(){
		TestPaths paths = new TestPaths();
		List<PathNode> nodesAfter = PathNode.nodesAfter(paths.aa, paths.aa.bb.cc);
		Assert.assertEquals(PathNode.toSlashedString(nodesAfter, true), "/bb/cc");
	}

	@Test
	public void testToSlashedStringAfter(){
		TestPaths paths = new TestPaths();
		PathNode cc = paths.aa.bb.cc;
		Assert.assertEquals(cc.toSlashedStringAfter(null, true), "/aa/bb/cc");
		Assert.assertEquals(cc.toSlashedStringAfter(paths.aa, true), "/bb/cc");
		Assert.assertEquals(cc.toSlashedStringAfter(paths.aa.bb, true), "/cc");
	}

	@Test
	public void testEquals(){
		TestPaths paths1 = new TestPaths();
		TestPaths paths2 = new TestPaths();
		Assert.assertNotSame(paths1.aa, paths2.aa);
		Assert.assertEquals(paths1.aa, paths2.aa);
	}

	@Test
	public void testParse(){
		TestPaths paths = new TestPaths();
		PathNode cc = paths.aa.bb.cc;
		Assert.assertEquals(PathNode.parse(cc.toSlashedString()), cc);
		Assert.assertEquals(PathNode.parse(cc.join("/")), cc);
		Assert.assertEquals(PathNode.parse(cc.toSlashedStringWithTrailingSlash()), cc);
		PathNode aa = paths.aa;
		Assert.assertEquals(PathNode.parse(aa.toSlashedString()), aa);
		Assert.assertEquals(PathNode.parse(paths.toSlashedString()), paths);
	}

}
