package com.hotpads.util.core.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HtmlSelectOptionBean{
	private static Comparator<HtmlSelectOptionBean> beanByValueComparator = Comparator.comparing(
			HtmlSelectOptionBean::getName);

	public static List<HtmlSelectOptionBean> getOptionsFromMap(Map<String,String> map){
		List<HtmlSelectOptionBean> beans = new ArrayList<>(map.size());
		for(String key : map.keySet()){
			beans.add(new HtmlSelectOptionBean(map.get(key),key));
		}
		Collections.sort(beans, beanByValueComparator);
		return beans;
	}

	public HtmlSelectOptionBean(String name, String value){
		this.name = name;
		this.value = value;
	}

	private String name;
	private String value;
	private boolean selected = false;

	@Override
	public String toString(){
		return "<option value=\"" + value + "\"" + (selected ? " selected" : "") + ">" + name + "</option>";
	}

	private String getName(){
		return name;
	}

	public String getValue(){
		return value;
	}

}
