package com.hotpads.profile.count.collection.archive;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.util.core.lang.ClassTool;

public abstract class BaseCountArchive implements CountArchive{

	protected final String webApp;
	protected final String source;
	protected final Long periodMs;
	
	
	
	public BaseCountArchive(String sourceType, String source, Long periodMs){
		this.webApp = sourceType;
		this.source = source;
		this.periodMs = periodMs;
	}

	@Override
	public int compareTo(CountArchive that){
		if(ClassTool.differentClass(this, that)){ 
			return DrComparableTool.nullFirstCompareTo(this.getClass().getName(), that.getClass().getName()); 
		}
		return (int)(this.getPeriodMs() - that.getPeriodMs());
	}

	@Override
	public String getSourceType(){
		return webApp;
	}

	@Override
	public String getSource(){
		return source;
	}

	@Override
	public long getPeriodMs(){
		return this.periodMs;
	}
	
}
