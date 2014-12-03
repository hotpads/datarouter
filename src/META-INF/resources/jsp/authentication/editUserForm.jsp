<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
<title>${user.username} - Edit user</title>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
<%@ include file="/WEB-INF/jsp/menu/reputation-navbar.jsp"%>
	<div class="container">
		<h2>Admin: Edit user</h2>
		<form method="POST" action="${contextPath}${authenticationConfig.editUserSubmitPath}">
		<input type="hidden" name="${authenticationConfig.userIdParam}" value="${user.id}"/>
		<table>
			<tr>
				<td>
					<label><input type="checkbox" name="${authenticationConfig.enabledParam}"
					<c:if test="${user.enabled}">checked</c:if>/> Enabled</label>
				</td>
				<td>
					<label><input type="checkbox" name="${authenticationConfig.apiEnabledParam}"
					<c:if test="${user.apiEnabled}">checked</c:if>/> API Enabled</label>
				</td>
			</tr>
			<tr>
				<td>Username:</td>
				<td><input type="text" name="${authenticationConfig.usernameParam}" value="${user.username}"
				required readonly/></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td>
					<a href="${contextPath}${authenticationConfig.resetPasswordPath}?
						${authenticationConfig.userIdParam}=${user.id}">Reset Password</a>
				</td>
			</tr>
			<tr>
				<td>Roles:</td>
				<td>
					<select multiple="multiple" name="${authenticationConfig.userRolesParam}">
						<c:forEach var="role" items="${datarouterUserRoles}">
							<option value="${role.name()}"
								<c:if test="${userRoles.contains(role)}">
								selected
								</c:if>
								>${role.name()}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>API Key:</td>
				<td>
					<input type="text" value="${user.apiKey}" readonly/><br/>
					<a href="${contextPath}${authenticationConfig.resetApiKeySubmitPath}?
						${authenticationConfig.userIdParam}=${user.id}">Reset API Key</a>
				</td>
			</tr>
			<tr>
				<td>Secret Key:</td>
				<td>
					<input type="text" value="${user.secretKey}" readonly/><br/>
					<a href="${contextPath}${authenticationConfig.resetSecretKeySubmitPath}?
						${authenticationConfig.userIdParam}=${user.id}">Reset Secret Key</a>
				</td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit"/></td>
			</tr>
		</table>

		</form>
	</div>
</body>
</html>
