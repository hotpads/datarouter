/**
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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.FieldSet;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.tuple.Range;

public class ScanSanitizer{
	private static final Logger logger = LoggerFactory.getLogger(ScanSanitizer.class);

	public static <PK extends PrimaryKey<PK>> void rejectUnexceptedFullScan(Collection<Range<PK>> ranges){
		ranges.forEach(ScanSanitizer::rejectUnexceptedFullScan);
	}

	public static <PK extends PrimaryKey<PK>> void rejectUnexceptedFullScan(Range<PK> range){
		if(range == null || range.getStart() == null && range.getEnd() == null){
			return; // expected full scan
		}
		if(range.getStart() == null && isValueOfFirstFieldNull(range.getEnd())
				|| range.getEnd() == null && isValueOfFirstFieldNull(range.getStart())
				|| isValueOfFirstFieldNull(range.getStart()) && isValueOfFirstFieldNull(range.getEnd())){
			throw new RuntimeException("unexcepted full scan detected for range=" + range);
		}
	}

	private static boolean isValueOfFirstFieldNull(FieldSet<?> key){
		return key != null && CollectionTool.getFirst(key.getFields()).getValue() == null;
	}

	public static <PK extends PrimaryKey<PK>> void logInvalidRange(Range<PK> range){
		if(!range.isValid()){
			logger.debug("invalid range={}", range, new Exception());
		}
	}

}
