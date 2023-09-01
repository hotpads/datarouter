<%@ page import="io.datarouter.web.html.PageScripts" %>
<%@ page import="io.datarouter.web.navigation.DatarouterNavbarFactory" %>
<%=((DatarouterNavbarFactory)getServletContext().getAttribute("datarouterNavbarFactory")).buildNewCommonNavbar(request)%>
<%=((PageScripts)getServletContext().getAttribute("pageScripts")).toString()%>
<%@ include file="/jsp/generic/timezone-check.jsp" %>
