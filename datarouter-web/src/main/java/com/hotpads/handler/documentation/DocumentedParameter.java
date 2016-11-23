package com.hotpads.handler.documentation;

public class DocumentedParameter{

	public String name;
	public String type;
	public Boolean required;

	public String getName(){
		return name;
	}

	public String getType(){
		return type;
	}

	public Boolean getRequired(){
		return required;
	}
}