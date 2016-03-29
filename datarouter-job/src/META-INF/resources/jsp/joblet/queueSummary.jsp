<%@ include file="/WEB-INF/prelude.jspf" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Jobs</title>
<%@ include file="/jsp/css/css-import.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/job.css"/>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
 		require([
               "bootstrap/bootstrap", "plugin/sorttable"
         ], function($) {});
</script>
<style>
table{
	border-collapse:collapse;
}
th{
	background-color:#aaeeaa;
}
th,td{
	border:1px solid gray;
	padding:2px;	
}
 </style>
 
 
<script type="text/javascript">
</script>	
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/generic/navBar.jsp" %>
	<div class="container">
		<h2 class="page-title">Joblet Queues</h2>
		<div class="clearfix"></div>
		<div
			class="page-content-container page-content-thicktop page-single-column">

			<%@ include file="/jsp/joblet/monitoringLinkBar.jsp" %>
			<br /> <br /> <br /> total tickets: ${totalTickets}<br /> <br /> <br />

			<table class="sortable table table-striped">
				<tr>
					<th>id</th>
					<th>numTickets</th>
					<th>maxTickets</th>
				</tr>
				<c:forEach items="${queues}" var="queue">
					<tr>
						<td>${queue.id}</td>
						<td>${queue.numTickets}&nbsp;&nbsp;&nbsp; <a
							href="?submitAction=alterQueueNumTickets&queueId=${queue.id}&diff=1">+</a>&nbsp;
							<a
							href="?submitAction=alterQueueNumTickets&queueId=${queue.id}&diff=-1">-</a>
						</td>
						<td>${queue.maxTickets}&nbsp;&nbsp;&nbsp; <a
							href="/admin/feeds/edit/${queue.id}">edit</a>
						</td>
					</tr>
				</c:forEach>
			</table>

		</div>
	</div>
</body>
</html>