<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>Console appender</title>
<%@ include file="/jsp/css/css-import.jspf"%>
<link rel="stylesheet" href="${contextPath}/css/multiple-select.css">
<script data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script>
	require(["bootstrap/bootstrap"]);
</script>
</head>
<body class="input-no-margin">
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<h2>Console appender</h2>
	<a href="http://logging.apache.org/log4j/2.x/manual/appenders.html#ConsoleAppender">log4j2 ConsoleAppender documentation</a>
	<form method="post" action="${contextPath}/datarouter/logging/editConsoleAppender">
		Name:
		<input type="text" name="name" value="${name}">
		<br>
		Layout:
		<input type="text" name="layout" value="${layout}" class="input-xxlarge">
		<br>
		Target:
		<select name="target">
			<option>SYSTEM_OUT</option>
			<option>SYSTEM_ERR</option>
		</select>
		<br>
		<c:if test="${empty name}">
			<input type="hidden" name="action" value="Create">
			<input type="submit" class="btn btn-success" value="Create">
		</c:if>
<%-- 		<c:if test="${not empty name}">
	 			<input type="hidden" name="action" value="Edit">
	 			<input type="submit" class="btn btn-warning" value="Edit">
	 		</c:if> --%>
	</form>
</body>
</html>