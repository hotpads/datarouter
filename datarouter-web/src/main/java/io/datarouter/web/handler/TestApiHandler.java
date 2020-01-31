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
package io.datarouter.web.handler;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.datarouter.httpclient.json.GsonJsonSerializer;
import io.datarouter.util.DateTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.DefaultDecoder;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.RequestBody;
import io.datarouter.web.handler.types.RequestBodyString;
import io.datarouter.web.handler.types.TypeProvider;

public class TestApiHandler extends BaseHandler{

	@Handler(defaultHandler = true)
	protected String handleDefault(){
		return "This is not a mav";
	}

	@Handler
	public Mav before(){
		return new MessageMav("Before !");
	}

	/*
	 * These examples show the possibility to pass the query parameters directly as method parameters
	 * - Note the use of the @P annotation to specify a different parameter name than the java one.
	 * - You can overload a @Handler function : the handler will call the function that has the largest number of
	 * parameters matching.
	 */

	@Handler
	public Mav hello(@Param("_first_name") String firstname){
		return new MessageMav("Degemer mat " + firstname + "!");
	}

	@Handler
	public Mav hi(String firstname, String lastname){
		return new MessageMav("Degemer mat " + firstname + " " + lastname + "!");
	}

	@Handler
	public Mav hi(String firstname, String lastname, boolean english){
		if(english){
			return new MessageMav("Hello " + firstname + " " + lastname + "!");
		}
		return new MessageMav("Degemer mat " + firstname + " " + lastname + "!");
	}

	@Handler
	public Mav hi(String firstname, String lastname, String greeting){
		return new MessageMav(greeting + " " + firstname + " " + lastname + "!");
	}

	private static enum Honorific{
		Mr,
		Miss,
		Mrs,
		Ms,
		;
	}

	// deserialize an enum
	@Handler
	public Mav hi(Honorific honorific, String name){
		return new MessageMav("Degemer mat " + honorific + " " + name + "!");
	}

	/*
	 * These examples show the possibility to return another type than Mav.
	 */
	@Handler
	public Date now(){
		return Calendar.getInstance().getTime();
	}

	public static class FooBar{
		public final String firstField;
		public final int intField;
		public final Date created;

		public FooBar(String firstField, int intField, Date created){
			this.firstField = firstField;
			this.intField = intField;
			this.created = created;
		}

		public String getHello(String name){
			return "Hello " + name;
		}
	}

	@Handler
	public FooBar banana(){
		FooBar dto = new FooBar("hello", 42, Calendar.getInstance().getTime());
		return dto;
	}

	@Handler
	public Collection<FooBar> bananas(){
		FooBar dto = new FooBar("hello", 42, Calendar.getInstance().getTime());
		FooBar otd = new FooBar("world", 24, Calendar.getInstance().getTime());
		return ListTool.create(dto, otd);
	}

	/*
	 * COMBO
	 */
	@Handler
	public FooBar first(FooBar[] fooBars){
		if(fooBars.length == 0){
			return null;
		}
		return fooBars[0];
	}

	// Decoding

	@Handler
	public Mav describe(FooBar fooBar){
		return new MessageMav("I'm " + fooBar.firstField + ", on " + fooBar.created + "my int is " + fooBar.intField);
	}

	@Handler
	public Mav sumInBase(@RequestBody int[] numbers, int base){
		int sum = 0;
		for(int num : numbers){
			while(num > 0){
				sum = sum + num % base;
				num = num / base;
			}
		}
		return new MessageMav(Integer.toString(sum));
	}

	@Handler
	public Mav printPrimitiveIntArray(@Param("numbers[]") int[] numbers){
		return new MessageMav(Arrays.toString(numbers));
	}

	@Handler
	public Mav printIntegerObjectArray(@Param("numbers[]") Integer[] numbers){
		return new MessageMav(Arrays.toString(numbers));
	}

	@Handler
	public Mav printPrimitiveIntArrayNoParamName(int[] numbers){
		return new MessageMav(Arrays.toString(numbers));
	}

	@Handler
	public Mav printComplicatedArrayParams(int[] foo, Integer[] bar, int[] baz){
		return new MessageMav(String.format("%s, %s, %s", Arrays.toString(foo), Arrays.toString(bar),
				Arrays.toString(baz)));
	}

	@Handler
	public Mav timeContains(@RequestBody Set<String> haystack, String needle, @RequestBodyString String strHaystack){
		PhaseTimer timer = new PhaseTimer();
		strHaystack.contains(needle);
		timer.add("String.contains");
		haystack.contains(needle);
		timer.add("Set.contains");
		return new MessageMav(timer.toString());
	}

	/*
	 * When you want generic types like Collection as parameters, you have to define a static type provider.
	 */
	public static class FooBarCollectionTypeProvider implements TypeProvider{

		@Override
		public Type get(){
			return new TypeToken<Collection<FooBar>>(){}.getType();
		}

	}

	@Handler
	public Mav count(@Param(typeProvider = FooBarCollectionTypeProvider.class) Collection<FooBar> fooBars){
		return new MessageMav("There are/is " + fooBars.size() + " element(s) in this list.");
	}

	/*
	 * MEGA COMBO
	 */
	@Handler
	public int size(@Param(typeProvider = FooBarCollectionTypeProvider.class) Collection<FooBar> fooBars){
		return fooBars.size();
	}

	@Handler
	public int size(@RequestBody List<Object> list){
		return list.size();
	}

	@Handler
	public int length(@RequestBody String string){
		return string.length();
	}

	@Singleton
	public static class TestApiHandlerDecoder extends DefaultDecoder{

		public TestApiHandlerDecoder(){
			super(new GsonJsonSerializer(new GsonBuilder().setDateFormat("yyyyMMdd").create()));
		}
	}

	@Handler(decoder = TestApiHandlerDecoder.class)
	public int year(Date date){
		return DateTool.getYearInteger(date);
	}

}
