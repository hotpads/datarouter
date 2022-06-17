<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>File appender</title>
</head>
<body class="input-no-margin">
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container py-5">
		<h2>File appender</h2>
		<a class="d-block" href="http://logging.apache.org/log4j/2.x/manual/appenders.html#FileAppender">log4j2 FileAppender documentation</a>
		<form class="form py-3" action="${contextPath}/datarouter/logging/editFileAppender">
			<c:set var="disabledProp" value="${not empty name ? 'disabled' : ''}"/>
			<div class="form-group">
				<label>Name</label>
				<input type="text" name="name" value="${name}" class="form-control" ${disabledProp}>
			</div>
			<div class="form-group">
				<label>Layout</label>
				<input type="text" name="layout" value="${layout}" class="form-control text-monospace" ${disabledProp}>
			</div>
			<div class="form-group">
				<label>FileName</label>
				<input type="text" name="fileName" value="${fileName}" class="form-control text-monospace" ${disabledProp}>
			</div>
			<c:if test="${empty name}">
				<div class="form-group">
					<input type="hidden" name="action" value="Create">
					<input type="submit" class="btn btn-success" value="Create">
				</div>
			</c:if>
		</form>
	</div>
</body>
</html>
