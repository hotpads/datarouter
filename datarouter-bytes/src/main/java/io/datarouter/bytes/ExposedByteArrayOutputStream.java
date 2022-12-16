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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * For converting from a ByteArrayOutputStream to a ByteArrayInputStream without making a copy of the data.
 *
 * Should be used carefully for performance-critical situations.
 *
 * Expected behavior:
 * - populate the OutputStream
 * - convert to InputStream using toInputStream()
 * - drain the InputStream
 * - optionally repeat by calling reset() on the OutputStream to avoid allocating
 */
public class ExposedByteArrayOutputStream
extends ByteArrayOutputStream{

	public ExposedByteArrayOutputStream(){
	}

	public ExposedByteArrayOutputStream(int initialCapacity){
		super(initialCapacity);
	}

	public InputStream toInputStream(){
		return new ByteArrayInputStream(buf, 0, count);
	}

}
