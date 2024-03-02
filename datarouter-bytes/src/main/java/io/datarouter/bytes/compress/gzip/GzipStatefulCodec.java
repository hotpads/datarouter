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
package io.datarouter.bytes.compress.gzip;

import java.io.ByteArrayOutputStream;

import io.datarouter.bytes.Codec;

/**
 * Currently reuses ByteArrayOutputStreams.
 * Could be enhanced to reuse the Gzip streams, but that is non-trivial.
 *
 * Not thread safe.
 */
public class GzipStatefulCodec implements Codec<byte[],byte[]>{

	private final GzipStatefulEncoder encoder;
	private final GzipStatefulDecoder decoder;

	public GzipStatefulCodec(){
		this.encoder = new GzipStatefulEncoder();
		this.decoder = new GzipStatefulDecoder();
	}

	@Override
	public byte[] encode(byte[] value){
		return encoder.encode(value);
	}

	@Override
	public byte[] decode(byte[] encodedValue){
		return decoder.decode(encodedValue);
	}

	public static class GzipStatefulEncoder{

		private ByteArrayOutputStream buffer;

		public byte[] encode(byte[] input){
			if(buffer == null){
				int initialCapacity = Math.max(16, input.length / 2);
				buffer = new ByteArrayOutputStream(initialCapacity);
			}
			return GzipTool.encode(input, buffer);
		}

	}

	public static class GzipStatefulDecoder{

		private ByteArrayOutputStream buffer;

		public byte[] decode(byte[] input){
			if(buffer == null){
				int initialCapacity = Math.max(16, input.length * 2);
				buffer = new ByteArrayOutputStream(initialCapacity);
			}
			return GzipTool.decode(input, buffer);
		}

	}

}