<!DOCTYPE html>
<%@ include file="/jsp/generic/prelude.jspf"%>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>View Trace</title>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<nav class="navbar navbar-light bg-light justify-content-start">
		<div class="container">
			<form class="form-inline">
				<input class="form-control mr-sm-2" type="search" placeholder="Trace ID" name="id">
				<input type="submit" class="btn btn-primary" value="Search">
			</form>
		</div>
	</nav>
	<div class="container my-4">
		<h2>Trace</h2>
		<table class="table table-striped table-bordered table-collapse">
			<tr>
				<td class="right">Trace id:</td>
				<td>${trace.traceId}</td>
			</tr>
			<tr>
				<td class="right">request:</td>
				<td>${trace.requestString}</td>
			</tr>
			<tr>
				<td class="right">start time:</td>
				<td>${trace.time}</td>
			</tr>
			<tr>
				<td class="right">duration:</td>
				<td>${trace.duration}ms</td>
			</tr>
			<tr>
				<td class="right">total numThreads:</td>
				<td>${threadGroup.numThreads}</td>
			</tr>
		</table>
	</div>
	<br/>
	<div class="container">
		${threadGroupHtml}
	</div>
</body>
</html>