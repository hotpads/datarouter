<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<title>Joblet Threads</title>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
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