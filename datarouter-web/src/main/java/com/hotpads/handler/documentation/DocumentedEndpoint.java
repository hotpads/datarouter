package com.hotpads.handler.documentation;

import java.util.List;

public class DocumentedEndpoint{

	public String url;
	public List<DocumentedParameter> parameters;
	public String description;

	public String getDescription(){
		return description;
	}

	public String getUrl(){
		return url;
	}

	public List<DocumentedParameter> getParameters(){
		return parameters;
	}
}