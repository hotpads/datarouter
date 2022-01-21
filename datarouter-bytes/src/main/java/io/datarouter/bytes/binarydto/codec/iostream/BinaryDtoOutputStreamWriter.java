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
package io.datarouter.bytes.binarydto.codec.iostream;

import java.io.OutputStream;

import io.datarouter.bytes.OutputStreamTool;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;

public class BinaryDtoOutputStreamWriter<T extends BinaryDto> implements AutoCloseable{

	private final BinaryDtoCodec<T> codec;
	private final OutputStream outputStream;

	public BinaryDtoOutputStreamWriter(Class<T> dtoClass, OutputStream outputStream){
		this.codec = BinaryDtoCodec.of(dtoClass);
		this.outputStream = outputStream;
	}

	public int write(T dto){
		byte[] dataBytes = codec.encode(dto);
		byte[] lengthBytes = VarIntTool.encode(dataBytes.length);
		OutputStreamTool.write(outputStream, lengthBytes);
		OutputStreamTool.write(outputStream, dataBytes);
		return lengthBytes.length + dataBytes.length;
	}

	@Override
	public void close(){
		OutputStreamTool.close(outputStream);
	}

}
