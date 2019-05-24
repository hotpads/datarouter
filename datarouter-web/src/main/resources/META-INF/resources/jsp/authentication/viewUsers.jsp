<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter - Users</title>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react/16.2.0/umd/react.production.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react-dom/16.2.0/umd/react-dom.production.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.26.0/babel.min.js" charset="UTF-8"></script>
	<%@ include file="/WEB-INF/jsp/generic/head.jsp"%>
	<script type="text/babel" src="${contextPath}/js/viewUsers.jsx"></script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<div id="app"></div>
</body>
</html>