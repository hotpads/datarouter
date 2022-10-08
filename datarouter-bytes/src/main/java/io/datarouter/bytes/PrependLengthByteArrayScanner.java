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
package io.datarouter.bytes;

import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class PrependLengthByteArrayScanner extends BaseScanner<byte[]>{

	private final Scanner<byte[]> inputScanner;

	public PrependLengthByteArrayScanner(Scanner<byte[]> inputScanner){
		this.inputScanner = inputScanner;
	}

	public static Scanner<byte[]> of(Scanner<byte[]> inputScanner){
		return new PrependLengthByteArrayScanner(inputScanner);
	}

	@Override
	public boolean advance(){
		if(!inputScanner.advance()){
			return false;
		}
		byte[] dataBytes = inputScanner.current();
		current = ByteTool.concat(VarIntTool.encode(dataBytes.length), dataBytes);
		return true;
	}

}