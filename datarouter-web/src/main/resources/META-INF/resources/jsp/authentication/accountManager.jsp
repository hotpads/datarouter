<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter Account Manager</title>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react-dom.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react-router/3.0.2/ReactRouter.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.24.0/babel.min.js" charset="UTF-8"></script>
	<%@ include file="/WEB-INF/jsp/generic/head.jsp"%>
	<script>
		const CONTEXT_PATH = "${contextPath}";
	</script>
	<script type="text/babel" src="${contextPath}/js/accountManager.js"></script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar.jsp" %>
	<%@ include file="/WEB-INF/jsp/menu/navbar.jsp"%>
	<div id="app"></div>
</body>
</html>