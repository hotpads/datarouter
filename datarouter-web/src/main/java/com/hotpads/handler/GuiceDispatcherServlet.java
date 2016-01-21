package com.hotpads.handler;

import com.hotpads.datarouter.inject.guice.GuiceInjectorRetriever;

@SuppressWarnings("serial")
public abstract class GuiceDispatcherServlet extends DispatcherServlet implements GuiceInjectorRetriever{}
