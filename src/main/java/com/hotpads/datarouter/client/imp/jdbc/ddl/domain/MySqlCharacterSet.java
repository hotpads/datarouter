package com.hotpads.datarouter.client.imp.jdbc.ddl.domain;

public enum MySqlCharacterSet{

	  armscii8          ,
	  ascii             ,
	  binary            ,
	  cp1250            ,
	  cp1251            ,
	  cp1256            ,
	  cp1257            ,
	  cp850             ,
	  cp852             ,
	  cp866             ,
	  dec8              ,
	  filename          ,
	  geostd8           ,
	  greek             ,
	  hebrew            ,
	  hp8               ,
	  keybcs2           ,
	  koi8r             ,
	  koi8u             ,
	  latin1            ,
	  latin2            ,
	  latin5            ,
	  latin7            ,
	  macce             ,
	  macroman          ,
	  swe7              ,
	  utf8              ;

	public static MySqlCharacterSet parse(String s){
		String lowerCase = s.toLowerCase();
		for(MySqlCharacterSet charset : values()){
			if(charset.toString().equals(lowerCase)){
				return charset;
			}
		}
		return null;
	}

}