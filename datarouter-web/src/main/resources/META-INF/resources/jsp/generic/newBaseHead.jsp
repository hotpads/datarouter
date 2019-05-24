<meta name="viewport" content="width=device-width, initial-scale=1">
<%@ include file="/jsp/css/new-css-import.jspf" %>
<script src="${contextPath}/requirejs/require.js"></script>
<script>
	require.config({
		baseUrl: '${contextPath}/',
		paths: {
			jquery: 'jquery/jquery',
			bootstrap: 'bootstrap/v4/js/bootstrap.bundle.min'
		},
		shim:{
			bootstrap: ['jquery']
		}
	})
	require(['bootstrap'])
</script>