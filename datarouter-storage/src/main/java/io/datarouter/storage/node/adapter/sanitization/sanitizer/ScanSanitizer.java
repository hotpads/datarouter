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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSet;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.field.compare.FieldSetComparator;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class ScanSanitizer{
	private static final Logger logger = LoggerFactory.getLogger(ScanSanitizer.class);

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

	public static <PK extends PrimaryKey<PK>> void logInvalidRange(Range<PK> range){
		if(!range.hasStart() || !range.hasEnd()){
			return;
		}
		if(range.equalsStartEnd()){
			if(!range.getStartInclusive()){
				logger.warn("range start=end with startInclusive=false, range={}", range, new Exception());
			}
		}else{
			List<Field<?>> startFields = range.getStart().getFields();
			List<Field<?>> endFields = range.getEnd().getFields();
			int numFieldsToCompare = Math.min(
					FieldTool.countNonNullLeadingFields(startFields),
					FieldTool.countNonNullLeadingFields(endFields));
			for(int i = 0; i < numFieldsToCompare; ++i){
				int diff = FieldSetComparator.compareFields(startFields.get(i), endFields.get(i));
				if(diff < 0){
					return;
				}
				if(diff > 0){
					logger.warn("range start after end, range={}", range, new Exception());
					return;
				}
			}
		}
	}

}
