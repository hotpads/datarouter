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
package io.datarouter.exception.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.exception.filter.ExceptionHandlingFilter;
import io.datarouter.exception.utils.ExceptionDetailsDetector.ExceptionRecorderDetails;

public class ExceptionRecorderDetailsTests{

	@Test
	public void parseDefaultExceptionNameTest(){
		String defualtNameWithoutCallOrigin = ExceptionRecorderDetails.getDefaultName(NullPointerException.class
				.getName(), ExceptionDetailsDetector.class.getName(), null);
		Assert.assertEquals(defualtNameWithoutCallOrigin,
				"NullPointerException at ExceptionDetailsDetector in ExceptionDetailsDetector");

		String defualtNameWithCallOriginAndMethodName = ExceptionRecorderDetails.getDefaultName(
				NullPointerException.class.getName(), ExceptionDetailsDetector.class.getName(),
				"io.datarouter.exception.filter.ExceptionHandlingFilter.tryRecordExceptionAndRequestNotification");
		Assert.assertEquals(defualtNameWithCallOriginAndMethodName,
				"NullPointerException at ExceptionDetailsDetector "
						+ "in ExceptionHandlingFilter.tryRecordExceptionAndRequestNotification");

		String defualtNameWithCallOrigin = ExceptionRecorderDetails.getDefaultName(NullPointerException.class
				.getName(), ExceptionDetailsDetector.class.getName(), ExceptionHandlingFilter.class.getName());
		Assert.assertEquals(defualtNameWithCallOrigin,
				"NullPointerException at ExceptionDetailsDetector in ExceptionHandlingFilter");

		String defualtNameWithSimpleNameCallOrigin = ExceptionRecorderDetails.getDefaultName(NullPointerException.class
				.getName(), ExceptionDetailsDetector.class.getName(), ExceptionHandlingFilter.class.getSimpleName());
		Assert.assertEquals(defualtNameWithSimpleNameCallOrigin,
				"NullPointerException at ExceptionDetailsDetector in ExceptionHandlingFilter");
	}

}
