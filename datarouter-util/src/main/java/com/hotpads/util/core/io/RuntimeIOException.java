package com.hotpads.util.core.io;

import java.io.IOException;

@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException{

	private IOException checkedCause;
	
	public RuntimeIOException(IOException checkedCause){
		this.checkedCause = checkedCause;
	}

	public IOException getCheckedCause(){
		return checkedCause;
	}
	
}
