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
package io.datarouter.bytes.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class CountingOutputStream extends FilterOutputStream{

	private final long countInterval;
	private final Consumer<Long> countCallback;
	private long count;
	private long countSinceLastPublished;

	public CountingOutputStream(OutputStream outputStream){
		super(outputStream);
		this.countInterval = 0;
		this.countCallback = null;
	}

	public CountingOutputStream(OutputStream outputStream, int countInterval, Consumer<Long> countCallback){
		super(outputStream);
		this.countInterval = countInterval;
		this.countCallback = countCallback;
	}

	@Override
	public void write(int byteData) throws IOException{
		out.write(byteData);
		count++;
		updateCountSinceLastPublished(1);
	}

	@Override
	public void write(byte[] byteData, int offset, int length) throws IOException{
		out.write(byteData, offset, length);
		count += length;
		updateCountSinceLastPublished(length);
	}

	@Override
	public void close() throws IOException{
		super.close();
		if(countCallback != null){
			countCallback.accept(countSinceLastPublished);
			countSinceLastPublished = 0;
		}
	}

	private void updateCountSinceLastPublished(long byteCount){
		countSinceLastPublished += byteCount;
		if(countCallback != null){
			if(countSinceLastPublished >= countInterval){
				countCallback.accept(countSinceLastPublished);
				countSinceLastPublished = 0;
			}
		}
	}

	public long getCount(){
		return count;
	}

}
