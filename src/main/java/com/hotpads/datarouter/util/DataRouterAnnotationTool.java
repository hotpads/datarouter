package com.hotpads.datarouter.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.java.PrivateAccessor;

/**
 * Annotations must be retained at runtime! (hibernate's are)
 * @author david
 *
 */
public class DataRouterAnnotationTool {
	static Logger logger = LoggerFactory.getLogger(DataRouterAnnotationTool.class);

	private Class<?> clazz=null;
	
	public DataRouterAnnotationTool(Class<?> clazz){
		this.clazz=clazz;
	}

	public Integer getColumnLengthAnnotation(String fieldName){
		Field f=null;
		Class<?> currentClass = clazz;
		while(f == null && currentClass!=null && !currentClass.equals(Object.class)){
			f = PrivateAccessor.getPrivateField(currentClass,fieldName);
			if(f==null){
				 currentClass = currentClass.getSuperclass();
			}
		}
		return getColumnLengthAnnotation(f);
	}
	public Integer getColumnLengthMethodAnnotation(String methodName){
		Method m = PrivateAccessor.getPrivateMethod(clazz,methodName);
		return getColumnLengthAnnotation(m);
	}

	private Integer getColumnLengthAnnotation(AccessibleObject annotated){
		try{
			Column colAn = (Column)annotated.getAnnotation(Column.class);
			if(colAn == null) return null;
			return colAn.length();
		}catch(Exception e){
			return null;
		}
	}
	
	public String limitLength(String column, String content){
		Integer maxLength = getMaxLengthIfExceeded(column, content);
		if(maxLength==null) return content;
		return content.substring(0, maxLength);
	}
	
	/**
	 * Returns the max column length if a column's content exceeds the length 
	 * annotation.  Useful for error checking methods.
	 * @param column
	 * @param content
	 * @return
	 */
	public Integer getMaxLengthIfExceeded(String column, String content){
		if(content == null) return null;
		Integer maxLength = getColumnLengthAnnotation(column);
		if(maxLength==null) maxLength=255;
		if(content.length()>maxLength) return maxLength;
		return null;
	}
	
	private static class AnnotationToolTestBean{
		public static final String COL_name = "name", COL_feedId = "feedId", COL_label = "label";
		public static final int LENGTH_name = 50;
		public static final int LENGTH_feedId = 27;
		public static final int LENGTH_COL_LABEL = 50;
		
		@Column(length=LENGTH_name)
		private String name;

		@Column(length=LENGTH_feedId, nullable=false)
		protected String feedId;
		
		@Column(length=LENGTH_COL_LABEL)
		protected String label = null;
		
		public final String getName(){
			return name;
		}

		public final void setName(String name){
			this.name = name;
		}
		
	}
	
	private static class AnnotationToolTestExtendedBean extends AnnotationToolTestBean{
		
	}
	
	public static class Tests{

		@Test
		public void testGetLengthAnnotation() throws Exception{
			DataRouterAnnotationTool at = new DataRouterAnnotationTool(AnnotationToolTestBean.class);
			Assert.assertEquals(new Integer(50), at.getColumnLengthAnnotation(AnnotationToolTestBean.COL_name));
			
			//test field declared on supertype
			at = new DataRouterAnnotationTool(AnnotationToolTestExtendedBean.class);
			Assert.assertNotNull(at.getColumnLengthAnnotation(AnnotationToolTestExtendedBean.COL_feedId));
			Assert.assertNull(at.getColumnLengthAnnotation("nonExistantField"));
		}
		@Test
		public void testLimitLength() throws Exception{
			DataRouterAnnotationTool at = new DataRouterAnnotationTool(AnnotationToolTestBean.class);
			AnnotationToolTestBean bean = new AnnotationToolTestBean();
			String shortName = "somethingshort";
			String longName = 
				"somethingMuchLongerThanSomethingShortWithMoreLetters";
			bean.setName(shortName);
			bean.setName(at.limitLength(AnnotationToolTestBean.COL_name, bean.getName()));
			Assert.assertEquals(shortName,bean.getName());
			bean.setName(longName);
			bean.setName(at.limitLength(AnnotationToolTestBean.COL_name, bean.getName()));
			Assert.assertEquals(
					longName.substring(0,
							at.getColumnLengthAnnotation(AnnotationToolTestBean.COL_name)),
					bean.getName());
		}

		@Test
		public void testGetAnnotationLengthConstant(){
			DataRouterAnnotationTool at = new DataRouterAnnotationTool(AnnotationToolTestBean.class);
			Assert.assertEquals(new Integer(AnnotationToolTestBean.LENGTH_COL_LABEL), at.getColumnLengthAnnotation(AnnotationToolTestBean.COL_label));

		}
	}
	
}
