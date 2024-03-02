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
package io.datarouter.bytes.blockfile.encoding.valueblock;

import java.util.List;

import io.datarouter.bytes.blockfile.encoding.valueblock.impl.BlockfileSequentialValueBlockCodec;

public class BlockfileStandardValueBlockFormats{

	public static final BlockfileValueBlockFormat
			SEQUENTIAL = new BlockfileValueBlockFormat("SEQUENTIAL", BlockfileSequentialValueBlockCodec::new);

	public static final List<BlockfileValueBlockFormat> ALL = List.of(SEQUENTIAL);

}
