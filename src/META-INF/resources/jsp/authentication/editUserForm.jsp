<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>Sign in</title>
<%@ include file="/jsp/generic/head.jsp" %>
<%@ include file="/jsp/css/css-import.jspf" %>
</head>
<body>
<%@ include file="/jsp/menu/common-navbar.jsp" %>
<%@ include file="/WEB-INF/jsp/menu/reputation-navbar.jsp"%>
	<div class="container">
		<h2>Admin: Edit user</h2>
		<form method="POST" action="${contextPath}/admin/editUserSubmit">
		<table>
			<tr>
				<td>Username:</td>
				<c:set var="userNameParam" value="${authenticationConfig.usernameParam}"/>
				<td><input name="${userNameParam}" value="${user.username}" required/></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td><a href="${contextPath}/admin/resetPassword?userId=${user.id}">Reset Password</a></td>
			</tr>
			<tr>
				<td>Roles:</td>
				<td>
					<select multiple="multiple" name="userRoles">
						<c:foreach var="role" items="${dataRouterUserRolesList}">
							<c:set var="roleName" value="${roleName}"/>
							<option value="${roleName}"
								<c:if test="${not empty userRoleMap && not empty userRoleMap.get(roleName)}">
								selected
								</c:if>
								>${roleName}</option>
						</c:foreach>
					</select>
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
