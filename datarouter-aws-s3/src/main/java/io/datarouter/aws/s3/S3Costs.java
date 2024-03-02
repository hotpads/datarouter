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

import io.datarouter.bytes.ByteLength;
import io.datarouter.util.number.NumberFormatter;

public class S3Costs{

	/*--------- monthly -----------*/

	// As of 2023-04-05: $0.023/GB (but assuming they mean GiB)
	public static final double MONTHLY_DOLLARS_PER_GiB = .023;

	public static double monthlyStorageDollars(ByteLength bytes){
		double gibibytesDouble = bytes.toBytesDouble() / ByteLength.ofGiB(1).toBytesDouble();
		return gibibytesDouble * MONTHLY_DOLLARS_PER_GiB;
	}

	public static long monthlyStorageCents(ByteLength bytes){
		if(bytes.toBytes() == 0){
			return 0;
		}
		double centsDouble = monthlyStorageDollars(bytes) * 100;
		// Assume they will charge something for even 1 byte
		return Math.max(1, (long)centsDouble);
	}

	public static String monthlyStorageCostString(ByteLength bytes){
		return dollarsAndCentsDisplayString(monthlyStorageDollars(bytes));
	}

	/*--------- yearly -----------*/

	public static double yearlyStorageDollars(ByteLength bytes){
		return 12 * monthlyStorageDollars(bytes);
	}

	public static String yearlyStorageCostString(ByteLength bytes){
		return dollarsAndCentsDisplayString(yearlyStorageDollars(bytes));
	}

	/*--------- format -----------*/

	public static String dollarsAndCentsDisplayString(double value){
		return "$" + NumberFormatter.format(value, 2);
	}

}
