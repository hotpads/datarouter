<%@ page import="io.datarouter.web.navigation.DatarouterNavbarFactory" %>
<%=((DatarouterNavbarFactory)getServletContext().getAttribute("datarouterNavbarFactory")).buildCommonNavbar(request)%>
