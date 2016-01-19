package com.hotpads.handler.exception;

import javax.inject.Singleton;

import com.hotpads.datarouter.inject.guice.GuiceInjectorRetriever;

@Singleton
public class GuiceExceptionHandlingFilter
extends ExceptionHandlingFilter
implements GuiceInjectorRetriever{}
