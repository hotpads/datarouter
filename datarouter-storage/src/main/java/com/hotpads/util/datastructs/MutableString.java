package com.hotpads.util.datastructs;

/**
 * An object that contains a string, but remains the same object when the string changes.
 */
public class MutableString implements Comparable<MutableString>{

	private String string;

	public MutableString(String string){
		this.string = string;
	}

	@Override
	public boolean equals(Object object){
		if(object instanceof MutableString){
			return string.equals(((MutableString)object).getString());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return string.hashCode();
	}

	@Override
	public String toString(){
		return getString();
	}

	@Override
	public int compareTo(MutableString otherString){
		return string.compareTo(otherString.string);
	}

	public String get(){
		return string;
	}

	public void set(String string){
		this.string = string;
	}

	public String getString(){
		return string;
	}

	public void setString(String string){
		this.string = string;
	}
}