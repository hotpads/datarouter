package com.hotpads.datarouter.databeangenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.http.RequestTool;

public class DatabeanGeneratorHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatabeanGeneratorHandler.class);

	public static final String PARAM_DATABEAN_NAME = "databeanName";
	public static final String PARAM_DATABEAN_PACKAGE = "databeanPackage";
	public static final String PARAM_KEYFIELD_ENUM_TYPE = "keyField_enumType_";
	public static final String PARAM_KEYFIELD_NAME = "keyField_name_";
	public static final String PARAM_KEYFIELD_TYPE = "keyField_type_";
	public static final String PARAM_FIELD_ENUM_TYPE = "field_enumType_";
	public static final String PARAM_FIELD_NAME = "field_name_";
	public static final String PARAM_FIELD_TYPE = "field_type_";
	public static final String PARAM_CREATE_SCRIPT = "script";

	public static final int MAX_KEYFIELDS = 100;
	public static final int MAX_FIELDS = 200;

	private static List<Class<?>> sortedFieldTypes = new ArrayList<>(
			new TreeMap<>(JavapoetDatabeanGenerator.fieldTypeClassLookup).values());

	@Override
	@Handler
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/admin/datarouter/generateJavaClasses.jsp");
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder("FIELD TYPES:\n------------\n");

		for(Class<?> clazz : sortedFieldTypes){
			sb.append("<option value=\"" + clazz.getSimpleName() + "\">" + clazz.getSimpleName() + "</option>");
			sb1.append(clazz.getSimpleName() + "\n");
		}
		mav.put("fieldTypes", sb.toString());
		mav.put("fieldTypesAsString", sb1.toString());
		return mav;
	}

	@Handler(encoder = JsonEncoder.class)
	protected String generateJavaCode(){
		try{
			JavapoetDatabeanGenerator generator = collectParams();

			StringBuilder javaCode = new StringBuilder();
			javaCode.append(generator.getName() + "~~##~~");
			javaCode.append(generator.toJavaDatabean());
			javaCode.append("\n/****************************************************/");
			javaCode.append(generator.toJavaDatabeanKey());

			return javaCode.toString();
			//logger.warn(javaCode);
		}catch(Exception e){
			logger.error("",e);
			return "failed";
		}
	}

	private JavapoetDatabeanGenerator collectParams(){
		String createScript = StringEscapeUtils.unescapeHtml4(RequestTool.get(request, PARAM_CREATE_SCRIPT, null));
		if(DrStringTool.notEmpty(createScript)){
			return new JavapoetDatabeanGenerator(createScript);
		}
		String name = DrStringTool.capitalizeFirstLetter(RequestTool.get(request, PARAM_DATABEAN_NAME, null));
		String packageName = RequestTool.get(request, PARAM_DATABEAN_PACKAGE, null);

		JavapoetDatabeanGenerator generator = new JavapoetDatabeanGenerator(name, packageName);

		for(int i = 0; i < MAX_KEYFIELDS; i++){
			String fieldType = RequestTool.get(request, PARAM_KEYFIELD_TYPE + i, null);
			String fieldName = RequestTool.get(request, PARAM_KEYFIELD_NAME + i, null);
			String fieldEnumType = RequestTool.get(request, PARAM_KEYFIELD_ENUM_TYPE + i, null);
			generator.addKeyField(fieldType, fieldName, fieldEnumType);
		}

		for(int i = 0; i < MAX_FIELDS; i++){
			String fieldType = RequestTool.get(request, PARAM_FIELD_TYPE + i, null);
			String fieldName = RequestTool.get(request, PARAM_FIELD_NAME + i, null);
			String fieldEnumType = RequestTool.get(request, PARAM_FIELD_ENUM_TYPE + i, null);
			generator.addField(fieldType, fieldName, fieldEnumType);
		}

		return generator;
	}
}
