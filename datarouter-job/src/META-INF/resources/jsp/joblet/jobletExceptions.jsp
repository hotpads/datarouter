<%@ include file="/WEB-INF/prelude.jspf" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Joblet Exception Records</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<%@ include file="/WEB-INF/jsp/generic/jobHead.jsp" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require([
               "bootstrap/bootstrap", "plugin/sorttable"
         ], function($) {});
</script>
<link rel="stylesheet" href="${contextPath}/css/job.css"/>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container" id="exceptionsTable">
		<h2 class="page-header">Joblet Exception Records</h2>
		<div class="page-content-container page-content-thicktop page-single-column">
			<table class="sortable table table-bordered table-condensed">
				<tr>
					<th>type</th>
					<th>queueId</th>
					<th>created</th>
					<th>numItems</th>
					<th>numTasks</th>
					<th>exceptionRecordId</th>
				</tr>
				<c:forEach items="${failedJoblets}" var="joblet">
					<tr>
						<td>${joblet.key.type}</td>
						<td>${joblet.queueId}</td>
						<td>${joblet.createdAgo}</td>
						<td>${joblet.numItems}</td>
						<td>${joblet.numTasks}</td>
						<td><a href="/analytics/exception/details?exceptionRecord=${joblet.exceptionRecordId}">${joblet.exceptionRecordId}</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</body>
</html>