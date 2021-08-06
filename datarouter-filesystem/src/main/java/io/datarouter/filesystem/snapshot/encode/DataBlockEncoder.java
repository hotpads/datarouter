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
package io.datarouter.filesystem.snapshot.encode;

public interface DataBlockEncoder extends BlockEncoder{

	/**
	 * The number of items in the block.  Zero indicates we don't need to flush it.
	 */
	int numRecords();

	/**
	 * Used to determine when to flush a block.  We won't always know the exact block size until we actually encode
	 * it.
	 */
	int numBytes();

}
