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
package io.datarouter.scanner;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamScanner<T> extends IteratorScanner<T>{
	private static final Logger logger = LoggerFactory.getLogger(StreamScanner.class);

	private final Stream<T> stream;
	private boolean closed;

	public StreamScanner(Stream<T> stream){
		super(stream.iterator());
		this.stream = stream;
		this.closed = false;
	}

	public static <T> Scanner<T> of(Stream<T> stream){
		return new StreamScanner<>(stream);
	}

	@Override
	public void close(){
		if(closed){
			return;
		}
		try{
			stream.close();
		}catch(Exception e){
			logger.warn("exception on stream.close", e);
		}
		closed = true;
	}

}
