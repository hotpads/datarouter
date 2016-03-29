<%@ include file="/WEB-INF/prelude.jspf" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Joblet Threads</title>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require([
               "bootstrap/bootstrap", "plugin/sorttable"
         ], function($) {});
</script>
<script language="javascript" src="${contextPath}/js/plugin/sorttable.js" />
<%@ include file="/jsp/css/css-import.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/job.css"/>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container" id="jobletsTable">
		<h2 class="page-header">Joblet Threads</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			<p><%@ include file="/jsp/joblet/monitoringLinkBar.jsp" %></p>
			<c:forEach items="${runningJobletThreads}" var="jobletThreads">
				<h4>${jobletThreads.key} (running)</h4>
				<%@ include file="/jsp/joblet/jobletThreadTable.jspf"%>
			</c:forEach>
			<c:forEach items="${waitingJobletThreads}" var="jobletThreads">
				<h4>${jobletThreads.key} (waiting)</h4>
				<%@ include file="/jsp/joblet/jobletThreadTable.jspf"%>
			</c:forEach>
		</div>
	</div>
</body>
</html>