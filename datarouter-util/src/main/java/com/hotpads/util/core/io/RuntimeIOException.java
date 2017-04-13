package com.hotpads.util.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @deprecated use {@link UncheckedIOException}
 */
@Deprecated
@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException{

	public RuntimeIOException(IOException checkedCause){
		super(checkedCause);
	}

}
