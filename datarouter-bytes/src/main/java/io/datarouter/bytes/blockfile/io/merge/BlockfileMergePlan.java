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
package io.datarouter.bytes.blockfile.io.merge;

import java.util.List;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.row.BlockfileRowCollator.BlockfileRowCollatorStrategy;

public record BlockfileMergePlan(
		int numCompactorFiles,
		ByteLength numCompactorBytes,
		List<Integer> levels,
		List<BlockfileNameAndSize> files,
		BlockfileRowCollatorStrategy collatorStrategy){

	/* Note that total output size could be less or more based on metadata and compression effects.
	 * It could also be significantly less based on pruning versions and deletes.
	 */
	public ByteLength totalInputSize(){
		return BlockfileNameAndSize.totalSize(files);
	}

}
