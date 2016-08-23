package com.hotpads.util.core.io;

import java.io.IOException;

@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException{

	public RuntimeIOException(IOException checkedCause){
		super(checkedCause);
	}

}
