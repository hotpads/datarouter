package com.hotpads.datarouter.monitoring.latency;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.hotpads.util.core.Duration;

public class CheckResult{

	private final long dateMs;
	private final Optional<String> failureMessage;
	private final Optional<Duration> latency;

	private CheckResult(long dateMs, Optional<String> failureMessage, Optional<Duration> latency){
		this.dateMs = dateMs;
		this.failureMessage = failureMessage;
		this.latency = latency;
	}

	public static CheckResult newSuccess(long dateMs, Duration latency){
		return new CheckResult(dateMs, Optional.empty(), Optional.of(latency));
	}

	public static CheckResult newFailure(long dateMs, String failureMessage){
		return new CheckResult(dateMs, Optional.of(StringUtils.abbreviate(failureMessage, 200)), Optional.empty());
	}

	public String getCssClass(){
		if(failureMessage.isPresent()){
			return "red";
		}
		if(latency.get().to(TimeUnit.MILLISECONDS) < 200){
			return "green";
		}
		if(latency.get().to(TimeUnit.MILLISECONDS) < 600){
			return "orange";
		}
		return "red";

	}

	public String getLatency(){
		return latency
				.map(duration -> duration.toString(TimeUnit.MILLISECONDS))
				.orElse("");
	}

	@Override
	public String toString(){
		if(failureMessage.isPresent()){
			return "Failure " + new Duration(System.currentTimeMillis() - dateMs, TimeUnit.MILLISECONDS) + " ago: "
					+ failureMessage.get();
		}
		return getLatency() + " (" + new Duration(System.currentTimeMillis() - dateMs, TimeUnit.MILLISECONDS)
				+ " ago)";
	}

}
