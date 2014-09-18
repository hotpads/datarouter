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
		<h2>Admin: Create user</h2>
		<form method="POST" action="${contextPath}${authenticationConfig.createUserSubmitPath}">
		
		<table>
			<tr>
				<td>Username:</td>
				<td><input type="email" name="${authenticationConfig.usernameParam}" required/></td>
			</tr>
			<tr>
				<td>Password:</td>
				<td><input type="password" name="${authenticationConfig.passwordParam}" required/></td>
			</tr>
			<tr>
				<td>Roles:</td>
				<td>
					<select multiple="multiple" name="${authenticationConfig.userRolesParam}">
						<c:forEach var="role" items="${datarouterUserRoles}">
							<option value="${role}">${role}</option>
						</c:forEach>
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
