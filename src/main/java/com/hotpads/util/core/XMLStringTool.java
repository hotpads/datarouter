package com.hotpads.util.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hotpads.util.core.collections.Pair;


public class XMLStringTool {
	/**
	 * make sure there are as many close brackets as open brackets.
	 */
	public static boolean evenBrackets(String s) {
		int opens = 0;
		int closes = 0;
		char last = ' ';
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '<' :
				opens++; 
				break;
			case '/': 
				if (last == '<') {
					opens--;
					closes++;
				}
				break;
			case '>':
				if (last == '/') {
					closes++;
				}
				break;
			}
			
			last = c;
		}	
		
		return opens == closes;
	}

	public static String stripBrackets(String s){
		if(s != null && (s.contains("<") || s.contains(">"))){
			s = s.replace('<', ' ');
			s = s.replace('>', ' ');
			s = s.trim();
		}
		return s;
	}

	public static String makeXmlTag(String tagName, Integer value, boolean ignoreIfNull, int indent, boolean newLine){
		if(value==null) return makeXmlTag(tagName,(String)null,ignoreIfNull, indent, newLine);
		return makeXmlTag(tagName, Integer.toString(value), ignoreIfNull, indent, newLine);
	}

	public static String makeXmlTag(String tagName, Long value, boolean ignoreIfNull, int indent, boolean newLine){
		if(value==null) return makeXmlTag(tagName,(String)null,ignoreIfNull, indent, newLine);
		return makeXmlTag(tagName, Long.toString(value), ignoreIfNull, indent, newLine);
	}
	
	public static String makeXmlTag(String tagName, Double value, boolean ignoreIfNull, int indent, boolean newLine){
		if(value==null) return makeXmlTag(tagName,(String)null,ignoreIfNull, indent, newLine);
		return makeXmlTag(tagName, Double.toString(value), ignoreIfNull, indent, newLine);
	}
	
	public static String makeXmlTag(String tagName, Boolean value, int indent, boolean newLine){
		return makeXmlTag(tagName, value.equals(true)?"true":"false", true/*will never be null*/, indent, newLine);
	}
	
	public static String makeXmlTag(String tagName, String value, boolean ignoreIfEmpty, int indent, boolean newLine){
		return makeXmlTag(tagName,value,ignoreIfEmpty,indent,newLine,(String)null);
	}
	
	public static String makeXmlTag(String tagName, String value, boolean ignoreIfEmpty, int indent, boolean newLine,
			Pair<String, String>... arguments){
		return makeXmlTag(tagName, value, ignoreIfEmpty, indent, newLine, Arrays.asList(arguments));
	}
	
	public static String makeXmlTag(String tagName, String value, boolean ignoreIfEmpty, int indent, boolean newLine,
			Collection<Pair<String, String>> arguments){
		StringBuilder argumentString = new StringBuilder();
		for(Pair<String,String> arg:arguments){
			argumentString.append(" "+arg.getLeft()+"=\""+arg.getRight()+"\"");
		}
		return makeXmlTag(tagName, value, ignoreIfEmpty, indent, newLine, argumentString.toString());
	}
	
	private static String makeXmlTag(
			String tagName,String value,boolean ignoreIfEmpty, int indent, boolean newLine, String argumentString){
		if(ignoreIfEmpty && StringTool.isNullOrEmpty(value)) return "";
		value = escapeXml(value);
		return makeUncheckedXmlTag(tagName, escapeXml(value),indent,newLine,argumentString);
	}

	public static String makeCDATATag(
			String tagName, String value, boolean ignoreIfEmpty, int indent, boolean newLine){
		if(StringTool.isNullOrEmpty(value)){ 
			if(ignoreIfEmpty) { return ""; }
			value = "";
		}
		value = "<![CDATA["+value+"]]>";
		return makeUncheckedXmlTag(tagName,value,indent,newLine,null);
	}
	
	private static String makeUncheckedXmlTag(
			String tagName, String value, int indent, boolean newLine, String argumentString){
		if(StringTool.isNull(value)) value = "";
		if(argumentString==null)argumentString="";
		return StringTool.getTabs(indent) + 
			"<"+tagName+argumentString+">"+
			value+
			"</"+tagName+">" + (newLine?"\n":"");
	}
		
	public static String escapeXml(String input){
		if(input==null){return null;}
		input = StringTool.removeNonStandardCharacters(input);
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
	
	/**
	 * inverse of escapeXml
	 * @param input
	 * @return
	 */
	public static String unescapeXml(String input) {
		if(input==null){return null;}
		
//		input = unescapeStandardPollution(input);
		input = input.replaceAll("&amp;", "&");
		input = input.replaceAll("&lt;","<");
		input = input.replaceAll("&gt;",">");
		input = input.replaceAll("&apos;","'");
		input = input.replaceAll("&quot;","\"");
		input = input.replaceAll("&nbsp;"," ");
		
		return input;	
	}
	
	/**
	 * replaces <*> with nothingness
	 * @param text
	 * @return
	 */
	public static String removeMarkup(String text){
		if(text==null){ return null; }
		text = text.replaceAll("(?s)((&amp;lt;)|(&lt;)|[\\<]).*?((&amp;gt;)|(&gt;)|[\\>])", "");
		text = text.replaceAll("(&amp;nbsp;)|\\>|\\<"," ");
		return text.trim();
	}
	
	public static String removeTagAndAttributes(String line, String tagName){
		int hTagStart = line.indexOf("<"+tagName);
		
		int hTagEnd = line.substring(hTagStart).indexOf('>');
		if(hTagEnd<0) 
			throw new IllegalArgumentException(
					tagName+" start tag broken onto multiple lines:\n"+line);
		hTagEnd += hTagStart+1;
		
		return (hTagStart>0?line.substring(0,hTagStart):"")+
				line.substring(hTagEnd);
	}
	
	public static String getFirstTagName(String line){
		Pattern p = Pattern.compile("(\\<)(\\w+)([\\s\\W]*.*\\>*)");
        Matcher m = p.matcher(line);
        m.find();
        if(m.groupCount()<2) return null;
        return m.group(2);    
	}
	
	public static Collection<String> getAllTagValues(String tagName, String xml){
		int tagStartIndex = 0;
		String beginTag = "<"+tagName+">";
		String endTag = "</"+tagName+">";
		Collection<String> values = Lists.newLinkedList();
		while((tagStartIndex = xml.indexOf(beginTag,tagStartIndex))!=-1){
			int locEnd = xml.indexOf(endTag,tagStartIndex);
			if(locEnd==-1){
				throw new RuntimeException("invalid xml, "+endTag+" doesn't exist");
			}
			values.add(xml.substring(tagStartIndex+beginTag.length(),locEnd));
			tagStartIndex = locEnd+endTag.length();
		}
		return values;
	}
	
	/** convenience methods for static import *********************************/
	public static void xml(StringBuilder xml, String tag, Number n, boolean skipEmpty, int indent){
		String num = (n==null?null:n.toString());
		xml(xml,tag,num,skipEmpty,indent);
	}
	
	public static void xml(StringBuilder xml, String tag, String value, int indent){
		xml(xml,tag,value,true,indent);
	}
	
	public static void xml(StringBuilder xml, String tag, String value, boolean skipEmpty, int indent){
		xml.append(XMLStringTool.makeXmlTag(tag,value,skipEmpty,indent,true));
	}

	public static void cdata(StringBuilder xml, String tag, String value, int tabs){
		xml.append(XMLStringTool.makeCDATATag(tag, value, false, tabs, true));
	}
	
	/** TESTS *****************************************************************/
	public static class Tests {

		@Test public void testRemoveMarkup(){
			Assert.assertEquals("b",removeMarkup("<a>b</a>"));
			Assert.assertEquals("b",removeMarkup("&lt;a&gt;b&lt;/a&gt;"));
			Assert.assertEquals("b",removeMarkup("&lt;a>b</a>"));
			Assert.assertEquals("b",removeMarkup("&amp;lt;a>b</a>"));
			Assert.assertEquals("b",removeMarkup("b"));
			Assert.assertEquals("&amp;b",removeMarkup("&amp;b"));
			Assert.assertEquals("some stuff bolded and italicized and regular",
					removeMarkup("some stuff <b>bolded</b> and &lt;i&gt;italicized&amp;lt;/i&amp;gt; and regular"));
			Assert.assertEquals("b",removeMarkup(">b<"));
			Assert.assertEquals("b",removeMarkup("b<"));
			Assert.assertEquals("b",removeMarkup("b>"));
			Assert.assertEquals("b",removeMarkup("b><"));
			Assert.assertEquals("b",removeMarkup("b><<"));
		}
		
		@Test public void testRemoveTagAndAttributes(){
			Assert.assertEquals("y",removeTagAndAttributes("<x>y", "x"));
			Assert.assertEquals("y",removeTagAndAttributes("<x >y", "x"));
			Assert.assertEquals("y",
					removeTagAndAttributes("<x oof=\"s\">y", "x"));
			Assert.assertEquals("<another>y",
					removeTagAndAttributes("<another><x oof=\"s\">y", "x"));
			
		}
		@Test public void testMakeXmlTag(){
			assertEquals("<b>donkey</b>",makeXmlTag("b","donkey",false,0,false));
			assertEquals("<b>donkey</b>\n",makeXmlTag("b","donkey",false,0,true));
			assertEquals("\t\t<b>donkey</b>",makeXmlTag("b","donkey",false,2,false));
			assertEquals("\t<b>donkey</b>\n",makeXmlTag("b","donkey",false,1,true));
			assertEquals("",makeXmlTag("b",(String)null,true,3,false));			
		}
		
		@Test public void testMakeCDATATag(){
			assertEquals("<t><![CDATA[garbage]]></t>",
					makeCDATATag("t", "garbage", false, 0, false));
			assertEquals("<t><![CDATA[>,&</>!,]]]></t>",
					makeCDATATag("t", ">,&</>!,]", false, 0, false));
		}
		
		@SuppressWarnings("unchecked")
		@Test public void testMakeXmlTagWithArguments(){
			Pair<String,String> href = Pair.create("href", "http://www.google.com");
			assertEquals("<a href=\"http://www.google.com\">Google</a>",
						makeXmlTag("a","Google",false,0,false,href));
			assertEquals("<a style=\"font-weight:bold\" " +
						"href=\"http://www.google.com\">Google</a>",
						makeXmlTag("a","Google",false,0,false,
									new Pair<String,String>("style",
													"font-weight:bold"),
									new Pair<String,String>("href",
													"http://www.google.com")));
		}
		
		@Test public void testEvenBrackets() {
			assertTrue(evenBrackets("<a>booga</a>"));
			assertTrue(evenBrackets("fjdis fds <jidff fsd  fds/>f jsdfsd kfsd"));
			assertTrue(evenBrackets("jifas<jfidsa/>fjkdsfs<a.fdsjkfs>fdsjfs</jfksd>"));
		}
		
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
		
		@Test public void testGetFirstTagName(){
			assertEquals("test",getFirstTagName("<test>"));
			assertEquals("test",getFirstTagName("<?xml><test>"));
			assertEquals("test",getFirstTagName("<?xml><!--x--><test>"));
			assertEquals("test",getFirstTagName("<?xml>\n<test>"));
			assertEquals("test",getFirstTagName("<?xml>\n<!--x-->\n<test>"));
			assertEquals("test",
					getFirstTagName("<?xml>\n<!--x-->\n" +
							"<test><another></another></test>"));
			assertEquals("test",getFirstTagName("<test x=\"y\">"));
		}
		
		@Test public void testGetAllTagValues(){
			Collection<String> allValues = getAllTagValues("c","<p><r>OK</r><j>2012</j><x>1</x><c>EX</c></p>");
			Assert.assertEquals(1, allValues.size());
			Assert.assertEquals("EX", CollectionTool.getFirst(allValues));
			
			allValues = getAllTagValues("z","<p><r>OK</r><j>2012</j><x>1</x><c>EX</c></p>");
			Assert.assertEquals(0, allValues.size());

			allValues = getAllTagValues("tag","<p><tag>OK</tag><tag>2012</tag></p>");
			Assert.assertEquals(2, allValues.size());
			String[] valuesArray = allValues.toArray(new String[]{});
			Assert.assertEquals("OK", valuesArray[0]);
			Assert.assertEquals("2012", valuesArray[1]);
		}
	}
	
}
