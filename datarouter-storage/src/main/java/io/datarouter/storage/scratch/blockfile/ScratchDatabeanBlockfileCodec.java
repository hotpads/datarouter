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
package io.datarouter.storage.scratch.blockfile;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.scratch.ScratchDatabeanCodec.ScratchDatabeanBytes;

public class ScratchDatabeanBlockfileCodec<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Codec<D,BlockfileRow>{

	private final Codec<D,ScratchDatabeanBytes> databeanBytesCodec;

	public ScratchDatabeanBlockfileCodec(Codec<D,ScratchDatabeanBytes> databeanBytesCodec){
		this.databeanBytesCodec = databeanBytesCodec;
	}

	@Override
	public BlockfileRow encode(D databean){
		ScratchDatabeanBytes scratchBytes = databeanBytesCodec.encode(databean);
		return BlockfileRow.putWithoutVersion(scratchBytes.key(), scratchBytes.value());
	}

	@Override
	public D decode(BlockfileRow blockfileRow){
		var scratchBytes = new ScratchDatabeanBytes(blockfileRow.copyOfKey(), blockfileRow.copyOfValue());
		return databeanBytesCodec.decode(scratchBytes);
	}

}
