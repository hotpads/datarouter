<%@ page import="io.datarouter.web.navigation.DatarouterNavbarFactory" %>
<%=((DatarouterNavbarFactory)getServletContext().getAttribute("datarouterNavbarFactory")).buildNewCommonNavbar(request)%>
<%@ include file="/jsp/generic/timezone-check.jsp" %>
