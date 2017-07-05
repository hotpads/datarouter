package io.datarouter.util;

import java.util.Optional;
import java.util.function.Function;

import io.datarouter.util.lang.ObjectTool;

public class OptionalTool{
	public static <I,O> O mapOrElse(Optional<I> in, Function<I,O> mapper, O orElse){
		in = ObjectTool.nullSafe(in, Optional.empty());
		return in.map(mapper).orElse(orElse);
	}
}
