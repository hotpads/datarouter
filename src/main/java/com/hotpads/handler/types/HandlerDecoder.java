package com.hotpads.handler.types;

import java.lang.reflect.Type;

public interface HandlerDecoder{

	<T> T deserialize(String toDeserialize, Type classOfT);
}
