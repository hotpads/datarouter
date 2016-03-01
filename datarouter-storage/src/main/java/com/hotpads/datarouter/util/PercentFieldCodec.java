package com.hotpads.datarouter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.datarouter.storage.field.Field;

public class PercentFieldCodec{
	private static final String FORWARD_SLASH = "/";
	private static final String CHARACTER_ENCODING = "UTF-8";

	public static String encode(List<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		fields.stream().map(Field::getValueString).collect(Collectors.joining(FORWARD_SLASH));
		try{
			return URLEncoder.encode(sb.toString(), CHARACTER_ENCODING);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("fields=" + sb.toString(), e);
		}
	}

	public static String[] decode(String pkEncoded){
		try{
			String decodedString = URLDecoder.decode(pkEncoded, CHARACTER_ENCODING);
			return decodedString.split(FORWARD_SLASH);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("pkEncoded=" + pkEncoded, e);
		}
	}
}
