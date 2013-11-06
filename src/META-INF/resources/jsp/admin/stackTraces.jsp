<%@ include file="../generic/prelude.jspf"%>
<html>
<head>
<title>${title}</title>
<c:import url="/jsp/generic/head.jsp" />
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
	    "bootstrap/bootstrap"
    ], function($) {});
</script>
<c:import url="/jsp/css/css-import.jsp" />
</head>
<body>
	<c:import url="/jsp/menu/dr-navbar.jsp" />
	<div class="container">${contentJSP}</div>
</body>