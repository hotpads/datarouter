<%@ include file="/jsp/generic/prelude.jspf"%>
<!DOCTYPE html>
<html>
<head>
	<title>Datarouter</title>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react-dom.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/react-router/3.0.2/ReactRouter.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.24.0/babel.min.js" charset="UTF-8"></script>
	<%@ include file="/jsp/generic/datarouterHead.jsp" %>
	<style>
		.status{
			display: inline-block;
			width: 10px;
			height: 10px;
			border: solid 1px;
			border-color: transparent;
			border-radius: 50%;
		}
		.green{
			background-color: #55DD44;
			border-color: #337722;
		}
		.orange{
			background-color: #FFAA00;
			border-color: #CC8800;
		}
		.red{
			background-color: #DD2222;
			border-color: #882222;
		}
		td{
			word-break: break-all;
		}
	</style>
	<script type="text/babel">
	const CONTEXT_PATH = "${contextPath}";
	const Form = ({ onChange, keyword }) => (
		<form onSubmit={(event) => event.preventDefault()}>
			<div className="form-group">
				<input type="text" 
						className="form-control" 
						placeholder="Search for a node" 
						onChange={onChange} 
						value={keyword} />
			</div>
		</form>
	);
	const Nodes = ({ nodes }) => (
		<table className="table table-striped table-bordered table-hover table-sm">
			<thead>
				<tr>
					<th>Node Name</th>
					<th>Count Keys</th>
					<th ${showTableCountLink ? '' : "hidden"}>Table Size</th>
				</tr>
			</thead>
			<tbody>
				{ nodes.map(node => 
					<tr key={node.name}>
						<td>
							{Array(4 * node.levelsNested).fill().map((x, i) => 
								<span key={i}>{'\u00A0'}</span>
							)}
							<a href={CONTEXT_PATH + "${viewNodeDataPath}" + encodeURIComponent(node.name)}>
								{node.name}
							</a>
						</td>
						<td>
							{node.sorted &&
								<a href={CONTEXT_PATH + "${countKeysPath}" + encodeURIComponent(node.name)}>
									count keys
								</a>
							}
						</td>
						<td ${showTableCountLink ? '' : "hidden"}>
							<a href={CONTEXT_PATH + "${tableCountLink}" + encodeURIComponent(node.name)}>
								<i className="fas fa-signal"></i>
							</a>
						</td>
					</tr>
				)}
			</tbody>
		</table>
	);
	class Search extends React.Component{
		constructor(props){
			super(props);
			var initialKeyword = new URLSearchParams(window.location.search).get("nodeSearchKeyword") || '';
			this.state = {keyword: initialKeyword, nodes: []};
		}

		componentDidMount(){
			this.loadResults(this.state.keyword);
		}

		loadResults = (keyword) => {
			if(keyword !== ""){
				fetch(CONTEXT_PATH + '/datarouter/nodes/search?keyword=' + keyword)
						.then(response => response.json())
						.then(nodes => this.setState({nodes}));
			}else{
				this.setState({nodes: []});
			}
		}

		onChange = (event) => {
			this.setState({keyword: event.target.value});
			event.preventDefault();
			this.loadResults(event.target.value);
			window.history.replaceState('', '', '?nodeSearchKeyword=' + event.target.value);
		}

		render(){
			return (
				<div>
					<Form onChange={this.onChange} keyword={this.state.keyword} />
					<Nodes nodes={this.state.nodes} />
				</div>
			);
		}
	}
	if(!!${showStorage}){
		ReactDOM.render(
			<div>
				<h3>Nodes</h3>
				<Search />
			</div>,
			document.getElementById('nodeSearch')
		)
	}
	</script>
</head>
<body>
	<%@ include file="/jsp/menu/common-navbar-b4.jsp" %>
	<div class="container-fluid my-3">
		<div class="row">
			${serverPropertiesTable}
			<c:if test="${showStorage}">
				${clientsTable}
			</c:if>
		</div>
		<div class="row mt-3">
			<div id="nodeSearch" class="col-12"></div>
		</div>
	</div>
</body>
</html>
