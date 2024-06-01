<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>

<head>
	<meta charset=utf-8 />
	<meta name="viewport" content="user-scalable=no, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, minimal-ui">
	<title>GraphQL Playground</title>
	<link rel="stylesheet" href="../../jsp/graphql/build/static/css/index.css" />
	<link rel="shortcut icon" href="../../jsp/graphql/build/favicon.png" />
	<script src='../../jsp/graphql/build/static/js/middleware.js' type='text/javascript'></script>
	<script src="https://unpkg.com/prettier@2.0.5/standalone.js"></script>
	<script src="https://unpkg.com/prettier@2.0.5/parser-graphql.js"></script>
</head>

<body>
	<div id="root">
		<style>
body {
	background-color: rgb(23, 42, 58);
	font-family: Open Sans, sans-serif;
	height: 90vh;
}

#root {
	height: 100%;
	width: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.loading {
	font-size: 32px;
	font-weight: 200;
	color: rgba(255, 255, 255, .6);
	margin-left: 20px;
}

img {
	width: 78px;
	height: 78px;
}

.title {
	font-weight: 400;
}
</style>
		<img src='../../jsp/graphql/build/logo.png' alt=''>
		<div class="loading">
			Loading <span class="title">Rental Growth GraphQL Playground</span>
		</div>
	</div>
	<script type="text/javascript">
		function buildDefaultTab(){
			tabs = [];
			var defaultTab = ${defaultTab};
			var sampleQuery = defaultTab.sampleQueries[0];
			tabs[0] = {
				endpoint: getHostNameAndContextPath() + defaultTab.rootQueryUrl,
				name: defaultTab.rootName + '/' + sampleQuery.name, // tab name
				rootName: defaultTab.rootName, // for query params
				queryName: sampleQuery.name, // for query params
				query: prettifyQuery(sampleQuery.query),
				queryTypes: sampleQuery.queryTypes,
				headers: defaultTab.headers
			}
			return tabs;
		}

		function buildSideNavs(){
			sideNavs = [];
			var jspDtoList = ${playgroundJspDtos};
			for(var i = 0; i < jspDtoList.length; i++){
				sideNavs[i] = {
					endpoint: getHostNameAndContextPath() + jspDtoList[i].rootQueryUrl,
					name: jspDtoList[i].rootName,
					sampleQueries: prettifyQueryByName(jspDtoList[i].sampleQueries),
					headers: jspDtoList[i].headers
				}
			}
			return sideNavs;
		}

		function prettifyQueryByName(sampleQueries){
			for(var i = 0; i < sampleQueries.length; i++){
				sampleQueries[i].query = prettifyQuery(sampleQueries[i].query);
			}
			return sampleQueries;
		}

		function prettifyQuery(query){
			return prettier.format(query, {parser: "graphql", plugins: prettierPlugins});
		}

		function getHostNameAndContextPath(){
			return '${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}';
		}

		window.addEventListener('load', function (event) {
		GraphQLPlayground.init(document.getElementById('root'), {
			endpoint: getHostNameAndContextPath() + '${defaultQueryEndpoint}',
			settings: {
				'request.credentials': 'same-origin',
				'schema.polling.enable' : false,
				'prettier.useTabs': true,
				'schema.disableComments': false,
				'tracing.hideTracingResponse': false,
				'tracing.tracingSupported': true
			},
			tabs: buildDefaultTab(),
			sideNavs: buildSideNavs()
			});
		});
	</script>
</body>
</html>
