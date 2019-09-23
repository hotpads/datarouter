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
package io.datarouter.util.lang;

import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * these can break easily if you modify this class.  just update the test
 */
public class LineOfCodeTests{

	@Test
	public void testSimple(){
		LineOfCode lineOfCode = new LineOfCode();
		Assert.assertEquals(lineOfCode.getPackageName(), "io.datarouter.util.lang");
		Assert.assertEquals(lineOfCode.getClassName(), "LineOfCodeTests");
		Assert.assertEquals(lineOfCode.getMethodName(), "testSimple");
		Assert.assertEquals(lineOfCode.getLineNumber(), Integer.valueOf(28));
	}

	@Test
	public void testCompareTo(){
		LineOfCode first = new LineOfCode();
		LineOfCode second = new LineOfCode();
		int diff = first.compareTo(second);
		Assert.assertEquals(diff, -1);
	}

}
