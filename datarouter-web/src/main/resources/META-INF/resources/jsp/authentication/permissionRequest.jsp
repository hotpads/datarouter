<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Datarouter - Permission Request</title>
	<%@ include file="/WEB-INF/jsp/generic/head.jsp"%>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp"%>
<%@ include file="/WEB-INF/jsp/menu/navbar.jsp"%>
<div class="container-fluid">
	<div>
		<p>Welcome to ${appName}. <a href="${contextPath}/signout">Sign out</a></p>
		<c:choose>
		<c:when test="${currentRequest != null}">
			<p>You already have an open permission request for ${appName}. You may submit another request to replace it.</p>
			<p>
				Time Requested: ${currentRequest.key.requestTime}
				<br>
				Request Text: ${currentRequest.requestText}
			</p>
		</c:when>
		<c:otherwise>
			<p>If you need (additional) permissions to use ${appName}, submit the form below, and the administrator will follow up.</p>
		</c:otherwise>
		</c:choose>
		<p>You will need to <a href="${contextPath}/signout">Sign Out</a> and sign back in to refresh your permissions</p>
		<p>If you have any questions, you may email the administrator(s) at <a href="mailto:${email}">${email}</a>.</p>
	</div>
	<form action="${contextPath}${permissionRequestPath}?submitAction=request" method="post">
		<div class="well">
			<div class="control-group">
				<div class="controls">
					<label for="reason">Why you want to access ${appName}:</label><br>
					<textarea name="reason" autofocus="autofocus" rows="4" cols = "50" required></textarea>
				</div>
				<div class="controls">
					<label for="specifics">(Optional) Additional information, specific functionality you require - roles, accounts, etc...:</label><br>
					<textarea name="specifics" rows="4" cols = "50">${defaultSpecifics.orElse("")}</textarea>
				</div>
				<div class="controls">
					<button type="submit" class="btn btn-primary">Submit</button>
				</div>
			</div>
		</div>
	</form>
</div>
</body>
</html>