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
package io.datarouter.bytes.blockfile.test.encoding.valueblock.sequential;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileValueBlockRows;
import io.datarouter.bytes.blockfile.encoding.valueblock.impl.BlockfileSequentialValueBlockCodec;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class BlockfileSequentialValueBlockTests{

	private static final BlockfileValueBlockCodec BLOCK_CODEC = new BlockfileSequentialValueBlockCodec();

	private record TestRow(
			String key,
			int version,
			BlockfileRowOp op,
			String value){

		static final StringCodec STRING_CODEC = StringCodec.UTF_8;
		static final Codec<TestRow,BlockfileRow> ROW_CODEC = Codec.of(
				testKv -> BlockfileRow.create(
						STRING_CODEC.encode(testKv.key),
						ComparableIntCodec.INSTANCE.encode(testKv.version),
						testKv.op,
						STRING_CODEC.encode(testKv.value)),
				binaryKv -> new TestRow(
						STRING_CODEC.decode(binaryKv.copyOfKey()),
						ComparableIntCodec.INSTANCE.decode(binaryKv.copyOfVersion()),
						binaryKv.op(),
						STRING_CODEC.decode(binaryKv.copyOfValue())));
	}

	private static final long VALUE_BLOCK_ID = 3;
	private static final long FIRST_ROW_ID = 19;
	private static final List<TestRow> TEST_ROWS = List.of(
			new TestRow("", 0, BlockfileRowOp.PUT, ""),// 0 data bytes, 1 version byte
			new TestRow("a", 1, BlockfileRowOp.DELETE, ""),// 2 data bytes, 1 version byte
			new TestRow("bb", 2, BlockfileRowOp.PUT, "bb"),// 4 data bytes, 1 version byte
			new TestRow("ccc", 135, BlockfileRowOp.PUT, "ccc"));// 6 data bytes, 2 version bytes
	private static final List<BlockfileRow> ROWS = Scanner.of(TEST_ROWS)
			.map(TestRow.ROW_CODEC::encode)
			.list();
	private static final BlockfileValueBlockRows BLOCK_ROWS = new BlockfileValueBlockRows(
			VALUE_BLOCK_ID,
			FIRST_ROW_ID,
			ROWS);

	private static final int HEADER_VALUE_BLOCK_ID_LENGTH = 1;
	private static final int HEADER_FIRST_ITEM_ID_LENGTH = 1;
	private static final int HEADER_SIZE_LENGTH = 1;

	private static final int KEY_META_LENGTH = 4;// 1 byte per item
	private static final int KEY_DATA_LENGTH = 0 + 1 + 2 + 3;
	private static final int KEY_LENGTH = KEY_META_LENGTH + KEY_DATA_LENGTH;

	private static final int VERSION_META_LENGTH = 4;// 1 byte per item
	private static final int VERSION_DATA_LENGTH = 4 * 4;// 4 bytes per item
	private static final int VERSION_LENGTH = VERSION_META_LENGTH + VERSION_DATA_LENGTH;

	private static final int OP_META_LENGTH = 0;// no overhead, fixed length per entry
	private static final int OP_DATA_LENGTH = 4;// 1 byte per item
	private static final int OP_LENGTH = OP_META_LENGTH + OP_DATA_LENGTH;

	private static final int VALUE_META_LENGTH = 4;// 1 byte per item
	private static final int VALUE_DATA_LENGTH = 0 + 0 + 2 + 3;
	private static final int VALUE_LENGTH = VALUE_META_LENGTH + VALUE_DATA_LENGTH;

	private static final int TOTAL_HEADER_LENGTH = HEADER_VALUE_BLOCK_ID_LENGTH
			+ HEADER_FIRST_ITEM_ID_LENGTH
			+ HEADER_SIZE_LENGTH;
	private static final int TOTAL_DATA_LENGTH = KEY_LENGTH + VERSION_LENGTH + OP_LENGTH + VALUE_LENGTH;
	private static final int TOTAL_LENGTH = TOTAL_HEADER_LENGTH + TOTAL_DATA_LENGTH;

	@Test
	private void testWrite(){
		byte[] bytes = BLOCK_CODEC.encode(BLOCK_ROWS).bytes();
		Assert.assertEquals(bytes.length, TOTAL_LENGTH);
	}

	@Test
	private void testRead(){
		BlockfileEncodedValueBlock encodedBlock = BLOCK_CODEC.encode(BLOCK_ROWS);
		BlockfileValueBlockRows rows = BLOCK_CODEC.decode(encodedBlock, BlockfileKeyRange.everything());
		Assert.assertEquals(rows.valueBlockId(), VALUE_BLOCK_ID);
		Assert.assertEquals(rows.firstRowIdInBlock(), FIRST_ROW_ID);
		List<TestRow> actual = Scanner.of(rows.rows())
				.map(TestRow.ROW_CODEC::decode)
				.list();
		Assert.assertEquals(actual, TEST_ROWS);
	}

}
