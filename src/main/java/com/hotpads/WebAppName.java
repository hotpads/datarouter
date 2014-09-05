package com.hotpads;

import javax.inject.Singleton;

@Singleton
public class WebAppName{

	private String name;

	public void init(String name) {
		if (this.name != null) {
			throw new IllegalStateException("WebAppName is already initialized");
		}
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
