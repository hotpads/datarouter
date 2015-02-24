package com.hotpads.util.core.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HTMLSelectOptionBean {
	public static Comparator<HTMLSelectOptionBean> beanByValueComparator = new Comparator<HTMLSelectOptionBean>(){
		@Override
		public int compare(HTMLSelectOptionBean bean1, HTMLSelectOptionBean bean2) {
			return bean1.getName().compareTo(bean2.getName());
		}
	};
	
	public static List<HTMLSelectOptionBean> getOptionsFromMap(Map<String,String> c){
		List<HTMLSelectOptionBean> beans = new ArrayList<HTMLSelectOptionBean>(c.size());
		for (String key : c.keySet()){
			beans.add(new HTMLSelectOptionBean(c.get(key),key));
		}
		Collections.sort(beans, beanByValueComparator);
		return beans;
	}
	
	public HTMLSelectOptionBean(String name, String value) {
		this.name = name;
		this.value = value;
	}

	private String name;
	private String value;
	private String format;
	private boolean selected = false;

	public String toString() {
		return "<option value=\"" + value + "\"" + (selected ? " selected" : "")
				+ ">" + name + "</option>";
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
