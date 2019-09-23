/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.lang;

import io.datarouter.util.ComparableTool;

public class LineOfCode implements Comparable<LineOfCode>{

	private static final int OFFSET_FROM_TOP_OF_STACK = 1;// top of stack is our constructor

	private String packageName;
	private String className;
	private String methodName;
	private Integer lineNumber;

	public LineOfCode(){
		this(1);// add one for the chained constructor
	}

	public LineOfCode(int additionalOffsetFromTop){
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		StackTraceElement callsite = stackTrace[OFFSET_FROM_TOP_OF_STACK + additionalOffsetFromTop];
		this.packageName = StackTraceElementTool.getPackageName(callsite);
		this.className = StackTraceElementTool.getSimpleClassName(callsite);
		this.methodName = callsite.getMethodName();
		this.lineNumber = callsite.getLineNumber();
	}

	public String getPersistentString(){
		return packageName + "." + className + ":" + methodName + ":" + lineNumber;
	}

	@Override
	public String toString(){
		return getPersistentString();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (className == null ? 0 : className.hashCode());
		result = prime * result + (lineNumber == null ? 0 : lineNumber.hashCode());
		result = prime * result + (methodName == null ? 0 : methodName.hashCode());
		result = prime * result + (packageName == null ? 0 : packageName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		LineOfCode other = (LineOfCode) obj;
		if(className == null){
			if(other.className != null){
				return false;
			}
		}else if(!className.equals(other.className)){
			return false;
		}
		if(lineNumber == null){
			if(other.lineNumber != null){
				return false;
			}
		}else if(!lineNumber.equals(other.lineNumber)){
			return false;
		}
		if(methodName == null){
			if(other.methodName != null){
				return false;
			}
		}else if(!methodName.equals(other.methodName)){
			return false;
		}
		if(packageName == null){
			if(other.packageName != null){
				return false;
			}
		}else if(!packageName.equals(other.packageName)){
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(LineOfCode that){
		if(that == null){
			return 1;
		}// null first
		int diff = ComparableTool.nullFirstCompareTo(this.packageName, that.packageName);
		if(diff != 0){
			return diff;
		}
		diff = ComparableTool.nullFirstCompareTo(this.className, that.className);
		if(diff != 0){
			return diff;
		}
		diff = ComparableTool.nullFirstCompareTo(this.methodName, that.methodName);
		if(diff != 0){
			return diff;
		}
		return ComparableTool.nullFirstCompareTo(this.lineNumber, that.lineNumber);
	}

	public String getPackageName(){
		return packageName;
	}

	public String getClassName(){
		return className;
	}

	public String getMethodName(){
		return methodName;
	}

	public Integer getLineNumber(){
		return lineNumber;
	}

}
