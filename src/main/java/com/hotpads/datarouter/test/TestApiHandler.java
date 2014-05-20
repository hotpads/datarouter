package com.hotpads.datarouter.test;


import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;

import com.google.gson.reflect.TypeToken;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.mav.imp.MessageMav;
import com.hotpads.handler.types.DefaultDecoder;
import com.hotpads.handler.types.HandlerDecoder;
import com.hotpads.handler.types.P;
import com.hotpads.util.core.ListTool;
import com.ibm.icu.util.Calendar;

public class TestApiHandler extends BaseHandler{
	
	/*
	 * These examples show the possibility to pass the query parameters directly as method parameters
	 * - Note the use of the @P annotation to specify the name of the parameter.
	 * - If a @Handler method has non-@P-annotated parameters, it can't be called.
	 * - You can overload a @Handler function : the handler will call the function that has the largest number of
	 * parameters matching.
	 */
	@Handler
	public Mav hello(@P("firstname") String firstname){
		return new MessageMav("Degemer mat " + firstname + "!");
	}
	
	// WON'T WORK
	@Handler
	public Mav goodbye(@P("firstname") String firstname, String lastname){
		return new MessageMav("Kenavo " + firstname + "!");
	}
	
	@Handler
	public Mav hi(@P("firstname") String firstname, @P("lastname") String lastname){
		return new MessageMav("Degemer mat " + firstname + " " + lastname + "!");
	}
	
	@Handler
	public Mav hi(@P("firstname") String firstname, @P("lastname") String lastname, @P("english") boolean english){
		if(english){
			return new MessageMav("Hello " + firstname + " " + lastname + "!");
		}else{
			return new MessageMav("Degemer mat " + firstname + " " + lastname + "!");
		}
	}
	
	@Handler
	public Mav hi(@P("firstname") String firstname, @P("lastname") String lastname, @P("greeting") String greeting){
		return new MessageMav(greeting + " " + firstname + " " + lastname + "!");
	}
	
	//WON'T WORK
	@Handler
	public Mav bye(String firstname){
		return new MessageMav("Kenavo " + firstname + "!");
	}

	/*
	 * These examples show the possibility to return another type than Mav.
	 * - Just specify the encoder with @Handler
	 * - If you are not pleased with the default JSON serializer, you are welcome to build one and activate it
	 * with the setJsonSerializer() method.
	 */
	@Handler(encoder=JsonEncoder.class)
	public Date now(){
		return Calendar.getInstance().getTime();
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
		return ListTool.create(dto, otd);
	}
	
	/*
	 * COMBO
	 */
	@Handler(encoder=JsonEncoder.class)
	public FooBar first(@P("fooBars") FooBar[] fooBars){
		if(fooBars.length == 0){
			return null;
		}
		return fooBars[0];
	}
	
	/*
	 * When you want generic types like Collection as parameters, you have to define a static no-arg decoder.
	 */
	public static class FooBarCollectionDecoder extends DefaultDecoder{
		
		public FooBarCollectionDecoder(){
			
		}

		@Override
		public <T> T deserialize(String toDeserialize, Type classOfT){
			return super.deserialize(toDeserialize, new TypeToken<Collection<FooBar>>(){}.getType());
		}
		
	}
	
	@Handler
	public Mav count(@P(value = "fooBars", decoder = FooBarCollectionDecoder.class) Collection<FooBar> fooBars){
		return new MessageMav("There are/is " + fooBars.size() + " element(s) in this list.");
	}
	
	/*
	 * MEGA COMBO
	 */
	@Handler(encoder=JsonEncoder.class)
	public int size(@P(value = "fooBars", decoder = FooBarCollectionDecoder.class) Collection<FooBar> fooBars){
		return fooBars.size();
	}
	
	public static class XmlFooBarDecoder implements HandlerDecoder{

		@Override
		public <T> T deserialize(String toDeserialize, Type classOfT){
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	@Handler(encoder=JsonEncoder.class, decoder=XmlFooBarDecoder.class)
	public int length(String string){
		return string.length();
	}
	
	public class FooBar{
		private String firstField;
		private int intField;
		private Date created;
		
		public FooBar(String firstField, int intField, Date created){
			super();
			this.firstField = firstField;
			this.intField = intField;
			this.created = created;
		}
		
		public String getFirstField(){
			return firstField;
		}
		public int getIntField(){
			return intField;
		}
		public Date getCreated(){
			return created;
		}
		public String getHello(String name){
			return "Hello " + name;
		}
		
	}

	@Override
	public <T> T deserialize(String toDeserialize, Type classOfT){
		// TODO Auto-generated method stub
		return null;
	}
	
}
