<%@ include file="/jsp/generic/prelude-datarouter.jspf"%>
<html>
<head>
<title>${title}</title>
<%@ include file="/jsp/generic/head.jsp" %>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
	    "bootstrap/bootstrap"
    ], function($) {});
</script>
<%@ include file="/jsp/css/css-import.jspf"%>
</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">${contentJSP}</div>
</body>