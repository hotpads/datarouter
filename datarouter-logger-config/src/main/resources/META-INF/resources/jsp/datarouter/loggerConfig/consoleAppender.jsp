<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<title>Console appender</title>
</head>
<body class="input-no-margin">
	<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
	<div class="container py-5">
		<h2>Console appender</h2>
		<a class="d-block" href="http://logging.apache.org/log4j/2.x/manual/appenders.html#ConsoleAppender">log4j2 ConsoleAppender documentation</a>
		<form class="form py-3">
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
				<label>Target</label>
				<select name="target" ${disabledProp} class="form-control">
					<option ${target == 'SYSTEM_OUT' ? 'selected' : ''}>SYSTEM_OUT</option>
					<option ${target == 'SYSTEM_ERR' ? 'selected' : ''}>SYSTEM_ERR</option>
				</select>
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
