package com.hotpads.util.core.io;

import java.io.IOException;

/**
 * @deprecated use {@link RuntimeException}
 */
@Deprecated
@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException{

	public RuntimeIOException(IOException checkedCause){
		super(checkedCause);
	}

}
