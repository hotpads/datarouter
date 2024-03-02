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
package io.datarouter.bytes.blockfile.row;

import java.io.InputStream;
import java.util.Comparator;

import io.datarouter.bytes.io.InputStreamTool;

/**
 * Either PUT or DELETE.
 *
 * PUTS sort after DELETES so we favor keeping the PUT in cases of a PUT and DELETE with the same key+version.
 */
public enum BlockfileRowOp{

	DELETE((byte)0),
	PUT((byte)1);

	public static final int NUM_PERSISTENT_BYTES = 1;
	public static final Comparator<BlockfileRowOp> COMPARATOR = Comparator.comparing(op -> op.persistentValue);

	private static final BlockfileRowOp[] PERSISTENT_VALUE_LOOKUP_TABLE = new BlockfileRowOp[]{DELETE, PUT};

	public final byte persistentValue;
	public final byte[] persistentValueArray;

	BlockfileRowOp(byte persistentValue){
		this.persistentValue = persistentValue;
		persistentValueArray = new byte[]{persistentValue};
	}

	public static BlockfileRowOp fromByte(byte value){
		return PERSISTENT_VALUE_LOOKUP_TABLE[value];
	}

	public static BlockfileRowOp fromInputStream(InputStream inputStream){
		byte value = InputStreamTool.readRequiredByte(inputStream);
		return PERSISTENT_VALUE_LOOKUP_TABLE[value];
	}

}
