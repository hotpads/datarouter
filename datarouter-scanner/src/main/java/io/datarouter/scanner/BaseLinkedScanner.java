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

/**
 * Subclasses do not have to worry about closing the input scanner.
 */
public abstract class BaseLinkedScanner<T,R>
extends BaseScanner<R>{
	private static final Logger logger = LoggerFactory.getLogger(BaseLinkedScanner.class);

	protected final Scanner<T> input;
	protected boolean closed;

	public BaseLinkedScanner(Scanner<T> input){
		this.input = input;
		this.closed = false;
	}

	protected abstract boolean advanceInternal();

	@Override
	public final boolean advance(){
		if(closed){
			return false;
		}
		try{
			if(advanceInternal()){
				return true;
			}
		}catch(Exception e){
			logger.info("scanner exception on advanceInternal", e);
			close();
			throw e;
		}
		close();
		return false;
	}

	/**
	 * Extend to close internal resources before the input scanner is closed.
	 */
	protected void closeInternal(){
	}

	@Override
	public final void close(){
		if(closed){
			return;
		}
		try{
			closeInternal();
		}catch(Exception e){
			logger.warn("scanner exception on closeInternal", e);
		}
		try{
			input.close();
		}catch(Exception e){
			logger.warn("scanner exception on input.close", e);
		}
		closed = true;
	}

}
