package com.hotpads.datarouter.util.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class DrXMLStringTool {
	
	public static String escapeXml(String input){
		if(input==null){return null;}
		input = DrStringTool.removeNonStandardCharacters(input);
		input = input.replaceAll("&[Aa][Mm][Pp];", "&");
		input = input.replaceAll("&[Ll][Tt];","<");
		input = input.replaceAll("&[Gg][Tt];",">");
		input = input.replaceAll("&[Aa][Pp][Oo][Ss];","'");
		input = input.replaceAll("&[Qq][Uu][Oo][Tt];","\"");
		input = input.replaceAll("&[Nn][Bb][Ss][Pp];"," ");
		input = input.replaceAll("&", "&amp;");  //must do the ampersand first
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");
		input = input.replaceAll("'", "&apos;");
		input = input.replaceAll("\"", "&quot;");
		
		return input;
	}

	
	/** TESTS *****************************************************************/
	
	public static class Tests {
		
		@Test public void testEscapeXml(){
			assertEquals("test &amp; for &amp;",
					"Baseboard &amp; Crown Moldings Throughout; ",
					escapeXml("Baseboard &amp; Crown Moldings Throughout; "));
			assertEquals("test &amp; for &Amp;",
					"Baseboard &amp; Crown Moldings Throughout; ",
					escapeXml("Baseboard &Amp; Crown Moldings Throughout; "));
			assertEquals("test &amp; for &",
					"Baseboard &amp; Crown Moldings Throughout; ",
					escapeXml("Baseboard & Crown Moldings Throughout; "));
			assertEquals("test <>\"'&",
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley " +
					"&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
					escapeXml("<wee>steve \"steve-o\" o'malley & " +
							"fred \"the dagger\" dirkowitz</wee>"));
			assertEquals("test &lt;&gt;&quot;&apos;&amp;",
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley " +
					"&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
					escapeXml("&lt;wee&gt;steve &quot;steve-o&quot; o&apos;" +
							"malley &amp; fred &quot;the dagger&quot; " +
							"dirkowitz&lt;/wee&gt;"));
			assertEquals("test &lt;&gt;&quot;&apos;&amp; with various cases",
					"&lt;wee&gt;steve &quot;steve-o&quot; o&apos;malley " +
					"&amp; fred &quot;the dagger&quot; dirkowitz&lt;/wee&gt;",
					escapeXml("&lT;wee&Gt;steve &QUOT;steve-o&qUoT; o&aPoS;" +
							"malley &aMP; fred &Quot;the dagger&Quot; " +
							"dirkowitz&LT;/wee&gt;"));
		}
		
	}
	
}
