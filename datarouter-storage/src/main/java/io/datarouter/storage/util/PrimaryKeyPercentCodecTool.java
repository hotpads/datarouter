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
package io.datarouter.storage.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.util.Require;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.string.StringTool;

public class PrimaryKeyPercentCodecTool{

	public static String encode(PrimaryKey<?> pk){
		return PercentFieldCodec.encodeFields(pk.getFields());
	}

	public static <PK extends PrimaryKey<PK>> String encodeMulti(Collection<PK> pks, char delimiter){
		Require.isTrue(PercentFieldCodec.isValidExternalSeparator(delimiter), "invalid delimiter:" + delimiter);
		return pks.stream()
				.map(PrimaryKeyPercentCodecTool::encode)
				.collect(Collectors.joining(Character.toString(delimiter)));
	}

	public static <PK extends PrimaryKey<PK>> PK decode(Class<PK> pkClass, String encodedPk){
		return decode(ReflectionTool.supplier(pkClass), encodedPk);
	}

	public static <PK extends PrimaryKey<PK>> PK decode(Supplier<PK> pkSupplier, String encodedPk){
		if(encodedPk == null){
			return null;
		}
		PK pk = pkSupplier.get();
		List<String> tokens = PercentFieldCodec.decode(encodedPk);
		int index = 0;
		for(Field<?> field : pk.getFields(pk)){
			if(index > tokens.size() - 1){
				break;
			}
			field.fromString(tokens.get(index));
			field.setUsingReflection(pk, field.getValue());
			field.setValue(null);// to be safe until Field logic is cleaned up
			++index;
		}
		return pk;
	}


	public static <PK extends PrimaryKey<PK>> List<PK> decodeMulti(Class<PK> pkClass, char delimiter,
			String encodedPks){
		List<String> eachEncodedPk = StringTool.splitOnCharNoRegex(encodedPks, delimiter, false);
		return eachEncodedPk.stream()
				.map(encodedPk -> decode(pkClass, encodedPk))
				.collect(Collectors.toList());
	}

}
