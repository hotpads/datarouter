<meta name="viewport" content="width=device-width, initial-scale=1">
<%@ include file="/jsp/css/new-css-import.jspf" %>
<script src="${contextPath}/requirejs/require.js"></script>
<script>
	require.config({
		baseUrl: '${contextPath}/',
		paths: {
			'jquery': 'jquery/jquery',
			'jquery-ui': 'jquery/jquery-ui',
			'bootstrap': 'bootstrap/v4/js/bootstrap.bundle.min',
			'sorttable': 'sorttable/sorttable',
			'multiple-select': 'jee-assets/multiple-select/multiple-select',
			'dygraph': 'dygraph/dygraph-combined',
			'dygraph-extra': 'dygraph/dygraph-extra',
			'goog' : 'requirejs/plugins/goog',
			'async' : 'requirejs/plugins/async',
			'propertyParser' : 'requirejs/plugins/propertyParser'
		},
		shim:{
			'bootstrap': ['jquery'],
			'multiple-select' : ['jquery'],
			'dygraph-extra': ['dygraph']
		}
	})
	require(['jquery', 'bootstrap'], function($){
		// Add caption to sortable tables if they don't already have one
		$('table.sortable')
			.filter((i, el) => $(el).find('caption').length === 0)
			.append('<caption>Sortable table</caption>')
	})
</script>
