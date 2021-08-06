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
package io.datarouter.model.key.primary;

import java.util.Objects;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.util.tuple.Range;

public class EntityPrimaryKeyTool{

	public static <
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>>
	boolean isSingleEntity(Range<PK> pkRange){
		Range<EK> ekRange = getEkRange(pkRange);
		return ekRange.hasStart() && ekRange.equalsStartEnd() && isEntityFullyDefined(ekRange.getStart());
	}

	private static <
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>>
	Range<EK> getEkRange(Range<PK> pkRange){
		EK start = pkRange.hasStart() ? pkRange.getStart().getEntityKey() : null;
		EK end = pkRange.hasEnd() ? pkRange.getEnd().getEntityKey() : null;
		return new Range<>(start, true, end, true);
	}

	public static <EK extends EntityKey<EK>> boolean isEntityFullyDefined(EK ek){
		return ek.getFields().stream()
				.map(Field::getValue)
				.noneMatch(Objects::isNull);
	}

}
