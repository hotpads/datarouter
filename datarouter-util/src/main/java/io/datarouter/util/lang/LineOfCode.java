/*
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

import java.util.Objects;

import io.datarouter.util.ComparableTool;

public class LineOfCode implements Comparable<LineOfCode>{

	private static final int OFFSET_FROM_TOP_OF_STACK = 1;// top of stack is our constructor

	private final String packageName;
	private final String className;
	private final String methodName;
	private final Integer lineNumber;

	public LineOfCode(){
		this(OFFSET_FROM_TOP_OF_STACK);
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
		return Objects.hash(className, lineNumber, methodName, packageName);
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
		return Objects.equals(className, other.className)
				&& Objects.equals(lineNumber, other.lineNumber)
				&& Objects.equals(methodName, other.methodName)
				&& Objects.equals(packageName, other.packageName);
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
