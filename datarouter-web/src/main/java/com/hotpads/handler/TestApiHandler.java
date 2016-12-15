package com.hotpads.handler;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.types.DefaultDecoder;
import com.hotpads.handler.types.P;
import com.hotpads.handler.types.RequestBody;
import com.hotpads.handler.types.TypeProvider;
import com.hotpads.util.http.json.GsonJsonSerializer;

public class TestApiHandler extends BaseHandler{

	@Override
	@Handler(encoder=JsonEncoder.class)
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
	public Mav hello(@P("_first_name") String firstname){
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

	/*
	 * These examples show the possibility to return another type than Mav.
	 */
	@Handler(encoder=JsonEncoder.class)
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

	@Handler(encoder=JsonEncoder.class)
	public FooBar banana(){
		FooBar dto = new FooBar("hello", 42, Calendar.getInstance().getTime());
		return dto;
	}

	@Handler(encoder=JsonEncoder.class)
	public Collection<FooBar> bananas(){
		FooBar dto = new FooBar("hello", 42, Calendar.getInstance().getTime());
		FooBar otd = new FooBar("world", 24, Calendar.getInstance().getTime());
		return DrListTool.create(dto, otd);
	}

	/*
	 * COMBO
	 */
	@Handler(encoder=JsonEncoder.class)
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
	public Mav count(@P(typeProvider = FooBarCollectionTypeProvider.class) Collection<FooBar> fooBars){
		return new MessageMav("There are/is " + fooBars.size() + " element(s) in this list.");
	}

	/*
	 * MEGA COMBO
	 */
	@Handler(encoder=JsonEncoder.class)
	public int size(@P(typeProvider = FooBarCollectionTypeProvider.class) Collection<FooBar> fooBars){
		return fooBars.size();
	}

	@Handler(encoder=JsonEncoder.class)
	public int size(@RequestBody List<Object> list){
		return list.size();
	}

	@Handler(encoder=JsonEncoder.class)
	public int length(@RequestBody String string){
		return string.length();
	}

	@Singleton
	public static class TestApiHandlerDecoder extends DefaultDecoder{

		public TestApiHandlerDecoder(){
			super(new GsonJsonSerializer(new GsonBuilder().setDateFormat("yyyyMMdd").create()));
		}
	}

	@Handler(encoder=JsonEncoder.class, decoder=TestApiHandlerDecoder.class)
	public int year(Date date){
		return DrDateTool.getYearInteger(date);
	}

}
