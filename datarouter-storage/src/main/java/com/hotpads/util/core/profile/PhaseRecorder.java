package com.hotpads.util.core.profile;

public interface PhaseRecorder<T> {
	T record( String eventName );
}
