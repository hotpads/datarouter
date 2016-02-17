package com.hotpads.datarouter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import com.hotpads.datarouter.storage.field.Field;

public class PercentFieldCodec{
	private static final String FORWARD_SLASH = "/";
	private static final String CHARACTER_ENCODING = "UTF-8";

	public static String encode(List<Field<?>> fields){
		StringBuilder sb = new StringBuilder();
		boolean doneOne = false;
		for(Field<?> field : fields){
			if(doneOne){
				sb.append(FORWARD_SLASH);
			}else{
				doneOne = true;
			}
			sb.append(field.getValueString());
		}
		try{
			return URLEncoder.encode(sb.toString(), CHARACTER_ENCODING);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("fields=" + sb.toString(), e);
		}
	}

	public static String[] deCode(String pkEncoded){
		try{
			String decodedString = URLDecoder.decode(pkEncoded, CHARACTER_ENCODING);
			return decodedString.split(FORWARD_SLASH);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("pkEncoded=" + pkEncoded, e);
		}
	}
}
