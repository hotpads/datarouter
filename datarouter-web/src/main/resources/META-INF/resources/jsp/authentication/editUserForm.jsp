<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/jsp/generic/head.jsp" %>
	<title>${user.username} - Edit user</title>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/menu/navbar.jsp"%>
	<div class="container">
		<h2>User details</h2>
		<div class="row">
			<div class="col-sm-6">
				<dl>
					<dt>Username</dt>
					<dd>${user.username}</dd>
					<dt>Password</dt>
					<dd>
						*** (<a href="${contextPath}${authenticationConfig.resetPasswordPath}?
								${authenticationConfig.userIdParam}=${user.id}">Reset</a>)
					</dd>
				</dl>
			</div>
			<div class="col-sm-6 panel panel-default">
				<form class="panel-body" method="POST" action="${contextPath}${authenticationConfig.editUserSubmitPath}">
					<input type="hidden" name="${authenticationConfig.userIdParam}" value="${user.id}"/>
					<div class="form-group">
						<label>Roles:</label>
						<c:forEach var="role" items="${datarouterUserRoles}">
							<div>
								<input type="checkbox" value="${role}" id="${role}" name="${authenticationConfig.userRolesParam}"
										<c:if test="${userRoles.contains(role)}">checked</c:if>/>
								<label for="${role}">${role}</label>
							</div>
						</c:forEach>
					</div>
					<div class="form-group">
						<label>Accounts:</label>
						<c:forEach var="account" items="${datarouterAccounts}">
							<div>
								<input type="checkbox" value="${account.key.accountName}" id="${account.key.accountName}" name="accounts"
										<c:if test="${userAccounts.contains(account.key.accountName)}">checked</c:if>/>
								<label for="${account.key.accountName}">${account.key.accountName}</label>
							</div>
						</c:forEach>
					</div>
					<div class="form-group">
						<label>User Enabled:</label>
						<div>
							<input type="checkbox" id="enabled" name="${authenticationConfig.enabledParam}"<c:if test="${user.enabled}">checked</c:if>/>
							<label for="enabled">Enabled</label>
						</div>
					</div>
					<input type="submit" class="btn btn-default"/>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<h3>Permission Requests</h3>
				<a class="btn btn-primary" role="button" href="${permissionRequestPage}">Create Permission Request</a>
				<h4>Current Requests</h4>
				<c:choose>
					<c:when test="${!currentRequests.isEmpty()}">
					<table class="table table-bordered">
						<tr><th>Request time</th><th>Request Text</th>
						<c:forEach var="request" items="${currentRequests}">
							<tr><td>${request.key.requestTime}</td><td>${request.requestText}</td></tr>
						</c:forEach>
					</table>
					</c:when>
					<c:otherwise>
						<p>None</p>
					</c:otherwise>
				</c:choose>
				<h4>Resolved Requests</h4>
				<c:choose>
					<c:when test="${!resolvedRequests.isEmpty()}">
					<table class="table table-bordered">
						<tr><th>Request time</th><th>Request Text</th><th>Resolution</th><th>Resolution Time</th></tr>
						<c:forEach var="resolvedRequest" items="${resolvedRequests.keySet()}">
							<tr><td>${resolvedRequest.key.requestTime}</td><td>${resolvedRequest.requestText}</td><td>${resolvedRequests.get(resolvedRequest)}</td><td>${resolvedRequest.resolutionTime}</td></tr>
						</c:forEach>
					</table>
					</c:when>
					<c:otherwise>
						<p>None</p>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>
</body>
</html>
