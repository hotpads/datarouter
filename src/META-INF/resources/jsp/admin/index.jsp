<%@ include file="/WEB-INF/prelude.jspf"%>
<html>
<head>
<title>${title}</title>
<%@ include file="/jsp/generic/head.jsp"%>
<script type="text/javascript" data-main="${contextPath}/js/core-common" src="${contextPath}/js/require-jquery.js"></script>
<script type="text/javascript">
	require([
           "jquery/jquery-ui", "bootstrap/bootstrap"
    ], function($) {});
</script>
<%@ include file="/jsp/css/css-import.jsp"%>

</head>
<body>
	<%@ include file="/jsp/menu/dr-navbar.jsp"%>
	<div class="container">
		<h4>Welcome to Datarouter module !</h4>
	</div>
	</form>
</body>