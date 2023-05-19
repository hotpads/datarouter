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
package io.datarouter.aws.s3;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteLength;

public class S3CostsTests{

	@Test
	public void testStorageCost(){
		var tebibyte = ByteLength.ofTiB(1);
		double dollarsFor1TiB = S3Costs.monthlyStorageDollars(tebibyte);
		Assert.assertEquals(dollarsFor1TiB, 23.5, 1.0);//Add a little tolerance
		long centsFor1TiB = S3Costs.monthlyStorageCents(tebibyte);
		Assert.assertEquals(centsFor1TiB, 2_355, 1.0);//Add a little tolerance
	}

}