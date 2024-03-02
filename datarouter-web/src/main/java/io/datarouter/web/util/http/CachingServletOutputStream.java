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
package io.datarouter.web.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import io.datarouter.bytes.ByteLength;

public class CachingServletOutputStream extends ServletOutputStream{

	private static final ByteLength FOUR_MiB = ByteLength.ofMiB(4);

	private OutputStream outputStream;
	private ByteArrayOutputStream copiedOutputStream;
	private int currentSize;

	public CachingServletOutputStream(OutputStream outputStream){
		this.outputStream = outputStream;
		this.copiedOutputStream = new ByteArrayOutputStream();
		this.currentSize = 0;
	}

	@Override
	public boolean isReady(){
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener){
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(int data) throws IOException{
		outputStream.write(data);
		if(isOutputStreamWithinLimit()){
			copiedOutputStream.write(data);
			currentSize++;
		}
	}

	public byte[] getCopy(){
		return copiedOutputStream.toByteArray();
	}

	public boolean isOutputStreamWithinLimit(){
		return currentSize < FOUR_MiB.toBytes();
	}

}
