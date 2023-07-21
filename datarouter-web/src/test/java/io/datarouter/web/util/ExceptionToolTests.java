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
package io.datarouter.web.util;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ExceptionToolTests{

	@SuppressWarnings("serial")
	private static class SpecialInterruptedException extends InterruptedException{
	}

	@Test
	public void isFromInstanceOfTest(){
		Assert.assertTrue(ExceptionTool.isFromInstanceOf(new InterruptedException(), InterruptedException.class));
		Assert.assertTrue(ExceptionTool.isFromInstanceOf(new SpecialInterruptedException(),
				InterruptedException.class));
		Assert.assertTrue(ExceptionTool.isFromInstanceOf(new Exception(new InterruptedException()),
				InterruptedException.class));
		Assert.assertTrue(ExceptionTool.isFromInstanceOf(new Exception(new SpecialInterruptedException()),
				InterruptedException.class));
		Assert.assertFalse(ExceptionTool.isFromInstanceOf(new Exception(), InterruptedException.class));

		Assert.assertTrue(ExceptionTool.isFromInstanceOf(new SpecialInterruptedException(), RuntimeException.class,
				InterruptedException.class));
		Assert.assertFalse(ExceptionTool.isFromInstanceOf(new Exception(), RuntimeException.class,
				InterruptedException.class));
	}

	@Test
	private void testGetFirstMatchingExceptionIfApplicableNoMatch(){
		var nestedExceptions = new RuntimeException(new IOException(new IllegalArgumentException()));
		Optional<Throwable> firstMatchingExceptionOptional = ExceptionTool.getFirstMatchingExceptionIfApplicable(
				nestedExceptions, ConcurrentModificationException.class);
		Assert.assertTrue(firstMatchingExceptionOptional.isEmpty());
	}

	@Test
	private void testGetFirstMatchingExceptionIfApplicableMatch(){
		var nestedExceptions = new RuntimeException(new IOException(new IllegalArgumentException()));
		Optional<Throwable> firstMatchingExceptionOptional = ExceptionTool.getFirstMatchingExceptionIfApplicable(
				nestedExceptions, ConcurrentModificationException.class,
						IllegalArgumentException.class);
		Assert.assertFalse(firstMatchingExceptionOptional.isEmpty());
		Assert.assertTrue(firstMatchingExceptionOptional.get() instanceof IllegalArgumentException);
	}

	@Test
	private void testGetFirstMatchingExceptionIfApplicableFirstMatch(){
		var nestedExceptions = new RuntimeException(new IOException(new IllegalArgumentException()));
		Optional<Throwable> firstMatchingExceptionOptional = ExceptionTool.getFirstMatchingExceptionIfApplicable(
				nestedExceptions, IOException.class, IllegalArgumentException.class);
		Assert.assertFalse(firstMatchingExceptionOptional.isEmpty());
		Assert.assertTrue(firstMatchingExceptionOptional.get() instanceof IOException);
	}

}
