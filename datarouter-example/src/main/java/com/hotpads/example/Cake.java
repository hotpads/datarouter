package com.hotpads.example;

import java.util.Arrays;
import java.util.List;

import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayField;
import com.hotpads.datarouter.storage.field.imp.array.DelimitedStringArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;

public class Cake extends BaseDatabean<CakeKey,Cake>{

	public static class FieldKeys{
		public static final DelimitedStringArrayFieldKey INGREDIENTS = new DelimitedStringArrayFieldKey("ingredients",
				",");
		public static final IntegerFieldKey PREPARATION_TIME_MIN = new IntegerFieldKey("preparationTimeMin");
		public static final IntegerFieldKey COOKING_TIME_MIN = new IntegerFieldKey("cookingTimeMin");
		public static final IntegerFieldKey CALORIE = new IntegerFieldKey("calorie");
	}

	private CakeKey key;

	private List<String> ingredients;
	private Integer preparationTimeMin;
	private Integer cookingTimeMin;
	private Integer calorie;

	public static class CakeFielder extends BaseDatabeanFielder<CakeKey,Cake>{

		@Override
		public Class<? extends Fielder<CakeKey>> getKeyFielderClass(){
			return CakeKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(Cake cake){
			return Arrays.asList(
					new DelimitedStringArrayField(FieldKeys.INGREDIENTS, cake.ingredients),
					new IntegerField(FieldKeys.PREPARATION_TIME_MIN, cake.preparationTimeMin),
					new IntegerField(FieldKeys.COOKING_TIME_MIN, cake.cookingTimeMin),
					new IntegerField(FieldKeys.CALORIE, cake.calorie));
		}

	}

	@SuppressWarnings("unused") // used by reflection
	private Cake(){
		this.key = new CakeKey();
	}

	public Cake(String name, List<String> ingredients, Integer preparationTimeMin, Integer cookingTimeMin,
			Integer calorie){
		this.key = new CakeKey(name);
		this.ingredients = ingredients;
		this.preparationTimeMin = preparationTimeMin;
		this.cookingTimeMin = cookingTimeMin;
		this.calorie = calorie;
	}

	@Override
	public Class<CakeKey> getKeyClass(){
		return CakeKey.class;
	}

	@Override
	public CakeKey getKey(){
		return key;
	}

	public Integer getCalorie(){
		return calorie;
	}

}
