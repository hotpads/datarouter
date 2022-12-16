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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class CountingInputStream extends FilterInputStream{

	private final long countInterval;
	private final Consumer<Long> countCallback;
	private long count;
	private long countSinceLastPublished;

	public CountingInputStream(InputStream inputStream){
		super(inputStream);
		this.countInterval = 0;
		this.countCallback = null;
	}

	public CountingInputStream(InputStream inputStream, int countInterval, Consumer<Long> countCallback){
		super(inputStream);
		this.countSinceLastPublished = 0;
		this.countInterval = countInterval;
		this.countCallback = countCallback;
	}

	@Override
	public int read() throws IOException{
		int data = super.read();
		if(data != -1){
			count++;
			updateCountSinceLastPublished(1);
		}
		return data;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException{
		int byteCount = super.read(buffer, offset, length);
		if(byteCount != -1){
			count += byteCount;
			updateCountSinceLastPublished(byteCount);
		}
		return byteCount;
	}

	@Override
	public long skip(long skipBytes) throws IOException{
		long skipCount = super.skip(skipBytes);
		count += skipCount;
		updateCountSinceLastPublished(skipCount);
		return skipCount;
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