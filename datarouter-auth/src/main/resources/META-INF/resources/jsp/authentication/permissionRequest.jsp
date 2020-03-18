<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Datarouter - Permission Request</title>
	<%@ include file="/jsp/generic/baseHead-b4.jsp"%>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar-b4.jsp"%>
<div class="container-fluid">
	<div>
		<p>Welcome to ${serviceName}. <a href="${contextPath}/signout">Sign out</a></p>
		<c:choose>
		<c:when test="${currentRequest != null}">
			<p>You already have an open permission request for ${serviceName}. You may submit another request to replace it.</p>
			<p>
				Time Requested: ${currentRequest.key.requestTime}
				<br>
				Request Text: ${currentRequest.requestText}
				<br>
				Click <a href="${declinePath}">here</a> to decline it.
			</p>
		</c:when>
		<c:otherwise>
			<p>If you need (additional) permissions to use ${serviceName}, submit the form below, and the administrator will follow up.</p>
		</c:otherwise>
		</c:choose>
		<p>You will need to <a href="${contextPath}/signout">Sign Out</a> and sign back in to refresh your permissions</p>
		<p>If you have any questions, you may email the administrator(s) at <a href="mailto:${email}">${email}</a>.</p>
	</div>
	<form action="${submitPath}" method="post">
		<div class="card card-body bg-light">
			<div class="control-group">
				<div class="controls">
					<label for="reason">Why you want to access ${serviceName}:</label><br>
					<textarea name="reason" autofocus="autofocus" rows="4" cols = "50" required></textarea>
				</div>
				<div class="controls">
					<label for="specifics">Additional information we have detected:</label><br>
					<textarea name="specifics" rows="4" cols ="50" readonly>${defaultSpecifics.orElse("")}</textarea>
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