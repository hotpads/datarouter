<%@ include file="/WEB-INF/prelude.jspf" %>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>Joblet Queues</title>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	<%@ include file="/jsp/joblet/jobletsNavbar.jspf" %>
	<div class="container">
		<h2 class="page-title">Joblet Queues</h2>
		<div class="clearfix"></div>
		<div
			class="page-content-container page-content-thicktop page-single-column">

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