package com.hotpads.datarouter.client.imp.hibernate.util;

import java.util.List;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;

public class CriteriaTool {

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Conjunction makeRangeConjonction(Range<PK> range,
			DatabeanFieldInfo<PK,D,?> fieldInfo){
		return makeRangeConjonction(range.getStart(), range.getStartInclusive(), range.getEnd(), range
				.getEndInclusive(), fieldInfo);
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> Conjunction makeRangeConjonction(
			final PK start, final boolean startInclusive,
			final PK end, final boolean endInclusive,
			DatabeanFieldInfo<PK,D,?> fieldInfo){

		Conjunction conjunction = Restrictions.conjunction();

		if(start != null && DrCollectionTool.notEmpty(start.getFields())){
			List<Field<?>> startFields = DrListTool.createArrayList(
					FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), start.getFields()));
			int numNonNullStartFields = FieldTool.countNonNullLeadingFields(startFields);
			Disjunction d = Restrictions.disjunction();
			for(int i=numNonNullStartFields; i > 0; --i){
				Conjunction c = Restrictions.conjunction();
				for(int j=0; j < i; ++j){
					Field<?> startField = startFields.get(j);
					if(j < i-1){
						c.add(Restrictions.eq(startField.getPrefixedName(), startField.getValue()));
					}else{
						if(startInclusive && i==numNonNullStartFields){
							c.add(Restrictions.ge(startField.getPrefixedName(), startField.getValue()));
						}else{
							c.add(Restrictions.gt(startField.getPrefixedName(), startField.getValue()));
						}
					}
				}
				d.add(c);
			}
			conjunction.add(d);
		}

		if(end != null && DrCollectionTool.notEmpty(end.getFields())){
			List<Field<?>> endFields = DrListTool.createArrayList(
					FieldTool.prependPrefixes(fieldInfo.getKeyFieldName(), end.getFields()));
			int numNonNullEndFields = FieldTool.countNonNullLeadingFields(endFields);
			Disjunction d = Restrictions.disjunction();
			for(int i=0; i < numNonNullEndFields; ++i){
				Conjunction c = Restrictions.conjunction();
				for(int j=0; j <= i; ++j){
					Field<?> endField = endFields.get(j);
					if(j==i){
						if(endInclusive && i==numNonNullEndFields-1){
							c.add(Restrictions.le(endField.getPrefixedName(), endField.getValue()));
						}else{
							c.add(Restrictions.lt(endField.getPrefixedName(), endField.getValue()));
						}
					}else{
						c.add(Restrictions.eq(endField.getPrefixedName(), endField.getValue()));
					}
				}
				d.add(c);
			}
			conjunction.add(d);
		}

		return conjunction;
	}

}
