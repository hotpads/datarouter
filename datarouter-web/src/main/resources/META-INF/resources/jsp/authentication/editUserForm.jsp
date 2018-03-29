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
						<label>Roles</label>
						<select multiple="multiple" class="form-control" name="${authenticationConfig.userRolesParam}">
							<c:forEach var="role" items="${datarouterUserRoles}">
								<option value="${role.name()}"
										<c:if test="${userRoles.contains(role)}">
										selected
										</c:if>
								>
									${role.name()}
								</option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group">
						<label>Accounts</label>
						<select multiple="multiple" class="form-control" name="accounts">
							<c:forEach var="account" items="${datarouterAccounts}">
								<option value="${account.key.accountName}"
										<c:if test="${userAccounts.contains(account.key.accountName)}">
										selected
										</c:if>
								>
									${account.key.accountName}
								</option>
							</c:forEach>
						</select>
					</div>
					<div class="checkbox">
						<label>
							<input type="checkbox" name="${authenticationConfig.enabledParam}"
									<c:if test="${user.enabled}">checked</c:if>/> Enabled
						</label>
					</div>
					<input type="submit" class="btn btn-default"/>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<h3>Permission Requests</h3>
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
