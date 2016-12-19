package com.hotpads.datarouter.config;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.hotpads.handler.encoder.HandlerEncoder;
import com.hotpads.handler.port.CompoundPortIdentifier;
import com.hotpads.handler.port.PortIdentifier;
import com.hotpads.util.http.json.GsonJsonSerializer;
import com.hotpads.util.http.json.JsonSerializer;

public class DatarouterWebGuiceModule extends ServletModule{

	@Override
	protected void configureServlets(){
		install(new DatarouterStorageGuiceModule());
		bind(ServletContextProvider.class).toInstance(new ServletContextProvider(getServletContext()));
		bind(JsonSerializer.class).annotatedWith(Names.named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)).to(
				GsonJsonSerializer.class);
		bind(PortIdentifier.class).annotatedWith(Names.named(CompoundPortIdentifier.COMPOUND_PORT_IDENTIFIER))
				.to(CompoundPortIdentifier.class);
	}

}
