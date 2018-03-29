<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Datarouter - Permission Request</title>
	<%--TODO possible to include a head here, instead of this stuff? --%>
	<%@ include file="/jsp/css/css-import.jspf"%>
</head>
<body>
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
			<p>We have created an account for you with no permissions. If you need to use ${appName}, submit the form below, and the administrator will follow up.</p>
		</c:otherwise>
		</c:choose>
		<p>You will need to <a href="${contextPath}/signout">Sign Out</a> and sign back in to refresh your permissions</p>
		<p>If you have any questions, you may email the administrator(s) at <a href="mailto:${email}">${email}</a>.</p>
	</div>
	<form action="${contextPath}${permissionRequestPath}?submitAction=request" method="post">
		<div class="well">
			<div class="control-group">
				<div class="controls">
					<textarea name="reason" placeholder="Why you want to access ${appName}" autofocus="autofocus" rows="4" cols = "50" required></textarea>
				</div>
				<div class="controls">
					<textarea name="specifics" placeholder="(Optional) Additional information, specific functionality you require, etc." rows="4" cols = "50"></textarea>
				</div>
				<div class="controls">
					<button type="submit" class="btn btn-primary">Submit</button>
				</div>
			</div>
		</div>
	</form>
</body>
</html>