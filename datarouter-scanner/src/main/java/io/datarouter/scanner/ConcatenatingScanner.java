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
package io.datarouter.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcatenatingScanner<T> extends BaseLinkedScanner<Scanner<T>,T>{
	private static final Logger logger = LoggerFactory.getLogger(ConcatenatingScanner.class);

	private Scanner<T> currentInputScanner;

	public ConcatenatingScanner(Scanner<Scanner<T>> input){
		super(input);
	}

	@Override
	public boolean advanceInternal(){
		if(currentInputScanner != null && currentInputScanner.advance()){
			current = currentInputScanner.current();
			return true;
		}
		while(input.advance()){
			currentInputScanner = input.current();
			if(currentInputScanner.advance()){
				current = currentInputScanner.current();
				return true;
			}
		}
		return false;
	}

	@Override
	public void closeInternal(){
		if(closed){
			return;
		}
		if(currentInputScanner != null){
			try{
				currentInputScanner.close();
			}catch(Exception e){
				logger.warn("scanner exception on currentInputScanner.close", e);
			}
		}
	}
}
