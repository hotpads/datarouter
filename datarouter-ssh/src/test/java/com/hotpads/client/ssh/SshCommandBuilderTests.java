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
package com.hotpads.client.ssh;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.client.ssh.request.SshExecSessionRequest.SshExecCommandBuilder;

public class SshCommandBuilderTests{

	@Test
	public void testCommandBuilder(){
		var actual = new SshExecCommandBuilder()
				.append("echo")
				.append("\"Hello World!\"")
				.build();

		Assert.assertEquals(actual, new String[]{"echo", "\"Hello World!\""});

		actual = new SshExecCommandBuilder()
				.asLoginShell()
				.append("echo")
				.append("\"Hello World!\"")
				.build();

		Assert.assertEquals(actual, new String[]{"bash", "-l", "-c", "\"echo \\\"Hello World!\\\"\""});
	}

	@Test
	public void testObfuscate(){
		var actual = new SshExecCommandBuilder()
				.append("one", "one.half", "two", "three")
				.obfuscate("one")
				.obfuscate("three")
				.display();

		Assert.assertEquals(actual, "xxx xxx.half two xxxxx");
	}

}
