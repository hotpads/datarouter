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
package io.datarouter.model.field;

import java.util.List;

import io.datarouter.model.field.compare.FieldSetComparator;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.scanner.Scanner;

public abstract class BaseFieldSet<F extends FieldSet<F>>
implements FieldSet<F>{

	/*----------------------------- standard --------------------------------*/

	@Override
	public boolean equals(Object that){
		if(that == null){
			return false;
		}
		if(!getClass().equals(that.getClass())){
			return false;
		}
		return 0 == FieldSetComparator.compareStatic(this, (FieldSet<?>)that);
	}

	@Override
	public int hashCode(){
		int result = 0;
		for(Field<?> field : getFields()){
			result = 31 * result + field.getValueHashCode();
		}
		return result;
	}

	/*
	 * WARNING - FieldSets are compared based only on their key fields.  Content is not compared by default
	 */
	@Override
	public int compareTo(FieldSet<F> that){
		return FieldSetComparator.compareStatic(this, that);
	}

	@Override
	public String toString(){
		return getClass().getSimpleName() + "." + PercentFieldCodec.encodeFields(getFields());
	}

	/*------------------------------ fields ---------------------------------*/

	@Override
	public List<String> getFieldNames(){
		return Scanner.of(getFields())
				.map(Field::getKey)
				.map(FieldKey::getName)
				.list();
	}

	@Override
	public List<?> getFieldValues(){
		return Scanner.of(getFields())
				.map(Field::getValue)
				.list();
	}

	@Override
	public Object getFieldValue(String fieldName){
		return Scanner.of(getFields())
				.include(field -> field.getKey().getName().equals(fieldName))
				.findFirst()
				.map(Field::getValue)
				.orElse(null);
	}

	//allows us to use the databean itself as the default Fielder
	//  - eliminates the less user-friendly nested class
	@Override
	public List<Field<?>> getFields(F fieldset){
		return fieldset.getFields();
	}

}
