<%@ page import="io.datarouter.web.html.PageScripts" %>
<%@ page import="io.datarouter.web.navigation.DatarouterNavbarFactory" %>
<%=((DatarouterNavbarFactory)getServletContext().getAttribute("datarouterNavbarFactory")).buildCommonNavbar(request)%>
<%=((PageScripts)getServletContext().getAttribute("pageScripts")).toString()%>

