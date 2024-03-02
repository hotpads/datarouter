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
package io.datarouter.bytes.blockfile.index;

import java.util.List;

public record BlockfileIndexBlockInput(
		long globalBlockId,
		long indexBlockId,
		int level,
		List<BlockfileIndexEntry> children){

	public BlockfileValueBlockIdRange toParentValueBlockIdRange(){
		return new BlockfileValueBlockIdRange(
				children.getFirst().valueBlockIdRange().first(),
				children.getLast().valueBlockIdRange().last());
	}

	public BlockfileRowIdRange toParentRowIdRange(){
		return new BlockfileRowIdRange(
				children.getFirst().rowIdRange().first(),
				children.getLast().rowIdRange().last());
	}

	public BlockfileRowRange toParentRowRange(){
		return new BlockfileRowRange(
				children.getFirst().rowRange().first(),
				children.getLast().rowRange().last());
	}
}