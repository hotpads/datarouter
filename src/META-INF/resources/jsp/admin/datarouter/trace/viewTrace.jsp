<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>View Trace</title>
<%@ include file="/jsp/generic/head.jsp" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
            "plugin/sorttable", "bootstrap/bootstrap"
    ], function($) {});
</script>
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp" %>
	
	<div class="container">
		<h2>Trace</h2>
		<br/>
		<table class="table table-striped table-bordered table-collapse sortable">
			<tr>
				<td class="right">Trace id:</td>
				<td>${trace.id}</td>
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
				<td>${threadGroup.numThreads}ms</td>
			</tr>
		</table>
	</div>
	
	<br/>
	<div class="container">
		${threadGroup.html}
	</div>

</body>
</html>