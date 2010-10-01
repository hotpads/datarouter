package com.hotpads.datarouter.meta;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.key.primary.BasePrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;

public class DatabeanClassGenerator {

	private String name;
	private List<FieldDefinition<?>> fieldDefinitions;
	private List<FieldDefinition<?>> keyFieldDefinitions;
	
	private static class FieldDefinition<T>{
		Class<T> type;
		String name;
		T initialValue = null;
		public FieldDefinition(Class<T> type, String name, T initialValue){
			this.type = type;
			this.name = name;
			this.initialValue = initialValue;
		}
		public String toString(){
			return "private "+type.getSimpleName()+" "+name+
					(initialValue==null?"":(" = "+initialValue))+";\n";
		}
	}
	
	public DatabeanClassGenerator(String name){
		this.name = name;
		this.fieldDefinitions = Lists.newLinkedList();
		this.keyFieldDefinitions = Lists.newLinkedList();
	}
	
	
	private <T> void addField(Class<T> type, String name, T initialValue, 
			List<FieldDefinition<?>> fieldList){
		fieldList.add(new FieldDefinition<T>(type,name,initialValue));
	}
	public <T> void addKeyField(Class<T> type, String name, T initialValue){
		addField(type,name,initialValue,keyFieldDefinitions);
	}
	public <T> void addField(Class<T> type, String name, T initialValue){
		addField(type,name,initialValue,fieldDefinitions);
	}
	
	public String toJavaDatabean(){
		StringBuilder sb = new StringBuilder();
		/** Imports ***********************************************************/
		sb.append(generateImports(fieldDefinitions, keyFieldDefinitions, 
				Column.class,Entity.class,Id.class,AccessType.class,
				BaseDatabean.class));
		sb.append("\n");
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		sb.append("@SuppressWarnings(\"serial\")\n");
		sb.append("@"+Entity.class.getSimpleName()+"\n");
		sb.append("@"+AccessType.class.getSimpleName()+"(\"field\")\n");
		sb.append("public class "+name+" extends "
				+BaseDatabean.class.getSimpleName()+"<"+keyClassName+">{\n");
		sb.append("\n");
		
		/** Fields ************************************************************/
		sb.append("\t@"+Id.class.getSimpleName()+"\n");
		sb.append("\t@"+Column.class.getSimpleName()+"(nullable=false)\n");
		sb.append("\tprivate "+keyClassName+" key;\n\n");
		for(FieldDefinition<?> f : fieldDefinitions){
			sb.append("\t"+f.toString());
		}
		sb.append("\n");
		
		/** Column Names ******************************************************/
		sb.append("\tpublic static final String\n");
		sb.append("\t\tKEY_NAME = \"key\",\n");
		List<String> colDefs = Lists.newLinkedList();
		for(FieldDefinition<?> f : Iterables.concat(keyFieldDefinitions,
													fieldDefinitions)){
			colDefs.add("\t\tCOL_"+f.name+" = \""+f.name+"\"");
		}
		sb.append(Joiner.on(",\n").join(colDefs)+";\n");
		sb.append("\n");
		
		/** Constructors ******************************************************/
		sb.append("\tpublic "+name+"(){}\n");
		sb.append("\tpublic "+name+"(");
		sb.append(makeMethodArguments(keyFieldDefinitions));
		sb.append("){\n");
		List<String> keyArgsList = Lists.newLinkedList();
		for(FieldDefinition<?> kf : keyFieldDefinitions){ 
			keyArgsList.add(kf.name);
		}
		String keyArgs = Joiner.on(", ").join(keyArgsList);
		sb.append("\t\tthis.key = new "+keyClassName+"("+keyArgs+");\n\t}\n\n");
		
		/** Keyclass methods **************************************************/
		sb.append("\t@Override\n");
		sb.append("\tpublic Class<"+keyClassName+"> getKeyClass() {\n");
		sb.append("\t\treturn "+keyClassName+".class;\n\t}\n\n");
		sb.append("\t@Override\n");
		sb.append("\tpublic "+keyClassName+" getKey() {\n");
		sb.append("\t\treturn this.key;\n\t}\n\n");
		sb.append("\tpublic void setKey("+keyClassName+" key) {\n");
		sb.append("\t\tthis.key = key;\n\t}\n\n");
		
		/** Getters and Setters ***********************************************/
		for(FieldDefinition<?> f : fieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(f.name);
			String type = f.type.getSimpleName();
			//get
			sb.append("\tpublic "+type+" get"+capField+"(){\n");
			sb.append("\t\treturn this."+f.name+";\n\t}\n");
			//set
			sb.append("\tpublic void set"+capField+"("+type+" "+f.name+"){\n");
			sb.append("\t\tthis."+f.name+" = "+f.name+";\n\t}\n\n");			
		}
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(kf.name);
			String type = kf.type.getSimpleName();
			//get
			sb.append("\tpublic "+type+" get"+capField+"(){\n");
			sb.append("\t\treturn this.key.get"+capField+"();\n\t}\n");
			//set
			sb.append("\tpublic void set"+capField+"("+type+" "+kf.name+"){\n");
			sb.append("\t\tthis.key.set"+capField+"("+kf.name+");\n\t}\n\n");			
		}
		
		sb.append("}");
		return sb.toString();
	}
	

	public String toJavaDatabeanKey(){
		StringBuilder sb = new StringBuilder();
		/** Imports ***********************************************************/
		sb.append(generateImports(null,keyFieldDefinitions,
				Column.class, List.class, Embeddable.class,
				Field.class, FieldTool.class, BasePrimaryKey.class));
		sb.append("\n");		
		
		/** Class Definition **************************************************/
		String keyClassName = name + "Key";
		sb.append("@SuppressWarnings(\"serial\")\n");
		sb.append("@"+Embeddable.class.getSimpleName()+"\n");
		sb.append("public class "+keyClassName+" extends "
				+BasePrimaryKey.class.getSimpleName()+"<"+keyClassName+">{\n");
		sb.append("\n");
		
		/** Fields ************************************************************/
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			sb.append("\t@"+Column.class.getSimpleName()+"(nullable=false)\n");
			sb.append("\t"+kf.toString());
		}
		sb.append("\n");
		
		/** Constructors ******************************************************/
		sb.append("\t"+keyClassName+"(){}\n");
		sb.append("\tpublic "+keyClassName+"(");
		sb.append(makeMethodArguments(keyFieldDefinitions));
		sb.append("){\n");
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			sb.append("\t\tthis."+kf.name+" = "+kf.name+";\n");
		}
		sb.append("\t}\n\n");

		
		/** BasePrimaryKey methods ********************************************/
		List<String> keyFieldList = Lists.newLinkedList();
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			keyFieldList.add("new "+kf.type.getSimpleName()+"Field(keyName, "
						+name+".COL_"+kf.name+", this."+kf.name+")");
		}
		sb.append("\t@Override\n");
		sb.append("\tpublic List<"+Field.class.getSimpleName()
				+"<?>> getFields() {\n");
		sb.append("\t\tString keyName = "+name+".KEY_NAME;\n");
		sb.append("\t\treturn "+FieldTool.class.getSimpleName()+".createList(\n");
		sb.append("\t\t\t"+Joiner.on(",\n\t\t\t").join(keyFieldList)
					+");\n\t}\n\n");
		
		
		/** Getters and Setters ***********************************************/
		for(FieldDefinition<?> kf : keyFieldDefinitions){
			String capField = StringTool.capitalizeFirstLetter(kf.name);
			String type = kf.type.getSimpleName();
			//get
			sb.append("\tpublic "+type+" get"+capField+"(){\n");
			sb.append("\t\treturn this."+kf.name+";\n\t}\n");
			//set
			sb.append("\tpublic void set"+capField+"("+type+" "+kf.name+"){\n");
			sb.append("\t\tthis."+kf.name+" = "+kf.name+";\n\t}\n\n");			
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	private static String makeMethodArguments(Collection<FieldDefinition<?>> fs){
		List<String> args = Lists.newLinkedList();
		for(FieldDefinition<?> f : fs){
			args.add(f.type.getSimpleName()+" "+f.name);
		}
		return Joiner.on(", ").join(args);
	}
	
	private static String generateImports(
			Collection<FieldDefinition<?>> fs, 
			Collection<FieldDefinition<?>> keyFs, Class<?>... classes){
		
		SortedSet<String> cannonicalClassNames = Sets.newTreeSet();
		for(Class<?> c : classes){
			cannonicalClassNames.add(c.getCanonicalName());
		}
		
		String fieldImpPackage = StringField.class.getPackage().getName();
		
		for(FieldDefinition<?> f 
				: Iterables.concat(CollectionTool.nullSafe(fs),
									CollectionTool.nullSafe(keyFs))){
			if(fs == null){
				cannonicalClassNames.add(
						fieldImpPackage+"."+f.type.getSimpleName()+"Field");
			}
			
			String canonical = f.type.getCanonicalName();
			if(canonical.startsWith(String.class.getPackage().getName()))
				continue;
			cannonicalClassNames.add(canonical);
		}
		
		StringBuilder sb = new StringBuilder();
		String lastPackagePrefix = null;
		for(String canonical : cannonicalClassNames){
			int firstDot = canonical.indexOf('.');
			int secondDot = canonical.indexOf('.', firstDot+1);
			String packagePrefix = 
				canonical.substring(0,secondDot>0?secondDot:firstDot);
			if(lastPackagePrefix!=null 
					&& ! lastPackagePrefix.equals(packagePrefix)){
				sb.append("\n");
			}
			lastPackagePrefix = packagePrefix;
			sb.append("import "+canonical+";\n");
		}
		
		return sb.toString();
	}
	
	public static void main(String... args){
		
		DatabeanClassGenerator g = 
			new DatabeanClassGenerator("ListingCounter");

		g.addField(Boolean.class, "active", true);
		g.addField(Date.class, "created", null);
		
		g.addKeyField(String.class, "name", null);

		System.err.println(g.toJavaDatabean());
		System.err.println("\n");
		System.err.println(g.toJavaDatabeanKey());
	}
	
}
