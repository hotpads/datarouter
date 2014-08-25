<%@ include file="/WEB-INF/prelude.jspf"%>
<%@ include file="../../../generic/prelude-datarouter.jspf"%>
<!DOCTYPE html>
<html>
<head>
<title>Logging settings</title>
<%@ include file="/jsp/css/css-import.jspf"%>
<link rel="stylesheet" href="${contextPath}/css/multiple-select.css">
<script data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script>
	require(["bootstrap/bootstrap", "jquery.multiple.select"], function(){
		if(document.readyState != 'loading'){
			$('select[multiple]').multipleSelect();
		} else {
			document.addEventListener("DOMContentLoaded", function(){
				$('select[multiple]').multipleSelect();
			});
		}
	});
</script>
</head>
<body class="input-no-margin">
	<%@ include file="/jsp/menu/common-navbar.jsp"%>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<h2>Log4j2 configuration</h2>
	<h3>Tester (will log the message for each level)</h3>
	<input type="text" id="loggerName" class="input-xlarge" value="com.hotpads.handler.logging">
	<input type="text" id="loggerMessage" class="input-medium" placeholder="Message">
	<button class="btn btn-primary" onclick="jQuery.ajax('${contextPath}/datarouter/logging/testLog?loggerName='+$('#loggerName').val()+'&loggerMessage='+$('#loggerMessage').val())">Test</button>
	<div>
		<h3>Logger config</h3>
		<a href="http://logging.apache.org/log4j/2.x/manual/architecture.html#LoggerConfig">About LoggerConfig</a>
		<a href="http://logging.apache.org/log4j/2.x/manual/configuration.html#Additivity">About Additivity</a>
		<table>
			<tr>
				<th>Name</th>
				<th>Level</th>
				<th>Additive</th>
				<th>Appenders</th>
				<th>Action</th>
			</tr>
			<c:forEach items="${configs}" var="config">
				<tr>
					<form method="post" action="${contextPath}/datarouter/logging/updateLoggerConfig">
						<input type="hidden" name="name" value="${config.value.name}">
						<td>
							<c:if test="${config.value != rootLogger}">
								${config.value.name}
							</c:if>
							<c:if test="${config.value == rootLogger}">
								<h4 style="margin: 0;text-align: center;">Root Logger</h4>
							</c:if>
						</td>
						<td>
							<select name="level" class="input-medium">
								<c:forEach items="${levels}" var="level">
									<option
										<c:if test="${level == config.value.level}">
										selected
									</c:if>>${level}</option>
								</c:forEach>
							</select>
						</td>
						<td>
							<select name="additive" class="input-small">
								<c:forEach items="${booleans}" var="bool">
									<option
										<c:if test="${bool == config.value.additive}">
										selected
									</c:if>>${bool}</option>
								</c:forEach>
							</select>
						</td>
						<td>
							<c:set var="configAppender" value="${appenderMap.get(config.value)}"/>
							<select name="appenders" multiple>
								<c:forEach items="${appenders}" var="appender">
									<option
										<c:forEach items="${configAppender}" var="element">
											<c:if test="${element == appender.value.name}">
												selected
											</c:if>
										</c:forEach>
									>${appender.value.name}</option>
								</c:forEach>
							</select>
						</td>
						<td>
							<input type="submit" class="btn btn-warning" value="Update">
							<c:if test="${config.value != rootLogger}">
								<a class="btn btn-danger" href="${contextPath}/datarouter/logging/deleteLoggerConfig?name=${config.key}">Delete</a>
							</c:if>
						</td>
					</form>
				</tr>
			</c:forEach>
			<tr>
				<form method="post" action="${contextPath}/datarouter/logging/createLoggerConfig">
					<td>
						<input type="text" name="name" placeholder="Name" required>
					</td>
					<td>
						<select name="level" class="input-medium" required>
							<option disabled selected>Level</option>
							<c:forEach items="${levels}" var="level">
								<option>${level}</option>
							</c:forEach>
						</select>
					</td>
					<td>
						<select name="additive" class="input-small" required>
							<option disabled selected>Additive</option>
							<c:forEach items="${booleans}" var="bool">
								<option>${bool}</option>
							</c:forEach>
						</select>
					</td>
					<td>
						<select name="appenders" multiple required>
							<c:forEach items="${appenders}" var="appender">
								<option>${appender.value.name}</option>
							</c:forEach>
						</select>
					</td>
					<td style="text-align: center;">
						<input type="submit" class="btn btn-primary" value="Add">
					</td>
				</form>
			</tr>
		</table>
	</div>
	<div>
		<h3>Appenders</h3>
		<table class="http-param">
			<tr>
				<th>Name</th>
				<th>Type</th>
				<th>Layout</th>
				<th>Action</th>
			</tr>
			<c:forEach items="${appenders}" var="appender">
				<tr>
					<td>${appender.value.name}</td>
					<td>${appender.value.getClass().simpleName}</td>
					<td>${appender.value.layout}</td>
					<td>
						<a class="btn btn-primary" href="${contextPath}/datarouter/logging/edit${appender.value.getClass().simpleName}?name=${appender.key}">Details</a>
						<a class="btn btn-danger" href="${contextPath}/datarouter/logging/deleteAppender?name=${appender.key}">Delete</a>
					</td>
				</tr>
			</c:forEach>
		</table>
		<a class="btn btn-success" href="${contextPath}/datarouter/logging/editConsoleAppender">Create console appender</a>
		<a class="btn btn-success" href="${contextPath}/datarouter/logging/editFileAppender">Create file appender</a>
	</div>
</body>
</html>