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
package io.datarouter.storage.node.adapter.sanitization.sanitizer;

import io.datarouter.model.field.FieldSet;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class ScanSanitizer{

	public static <PK extends PrimaryKey<PK>> void rejectUnexpectedFullScan(Range<PK> range){
		if(range.hasStart() && isMissingFirstField(range.getStart())){
			String message = String.format(
					"Unexpected full scan error: startKey exists but with null first field.  range=%s",
					range);
			throw new RuntimeException(message);
		}
		if(range.hasEnd() && isMissingFirstField(range.getEnd())){
			String message = String.format(
					"Unexpected full scan error: endKey exists but with null first field.  range=%s",
					range);
			throw new RuntimeException(message);
		}
	}

	// TODO: what if there are gaps after the first field?
	private static boolean isMissingFirstField(FieldSet<?> key){
		return key.getFields().iterator().next().getValue() == null;
	}

}
