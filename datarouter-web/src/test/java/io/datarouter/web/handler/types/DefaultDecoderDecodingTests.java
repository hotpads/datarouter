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
package io.datarouter.web.handler.types;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.web.test.DatarouterWebTestNgModuleFactory;

@Guice(moduleFactory = DatarouterWebTestNgModuleFactory.class)
public class DefaultDecoderDecodingTests{

	@Inject
	private DefaultDecoder defaultDecoder;

	@Test
	public void testDecodingString(){
		Assert.assertEquals(defaultDecoder.decode("", String.class), "");
		Assert.assertEquals(defaultDecoder.decode(" ", String.class), "");
		Assert.assertEquals(defaultDecoder.decode("\"\"", String.class), "");
		Assert.assertEquals(defaultDecoder.decode("\"", String.class), "\"");
		Assert.assertEquals(defaultDecoder.decode("\" ", String.class), "\" ");
		Assert.assertEquals(defaultDecoder.decode("\" \"", String.class), " ");
		Assert.assertEquals(defaultDecoder.decode("nulls", String.class), "nulls");
		Assert.assertEquals(defaultDecoder.decode("\"correct json\"", String.class), "correct json");
	}

	@Test
	public void preventNullDecoding(){
		assertFail(() -> defaultDecoder.decode("null", String.class));
		assertFail(() -> defaultDecoder.decode(null, String.class));
		assertFail(() -> defaultDecoder.decode("", Integer.class));
		assertFail(() -> defaultDecoder.decode(" ", Integer.class));
	}

	private void assertFail(Runnable runnable){
		try{
			runnable.run();
			Assert.fail();
		}catch(Exception e){
			// expected
		}
	}

}
