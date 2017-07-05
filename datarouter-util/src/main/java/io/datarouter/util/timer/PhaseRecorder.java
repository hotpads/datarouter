package io.datarouter.util.timer;

public interface PhaseRecorder<T>{
	T record(String eventName);
}
