const FETCH_OPTIONS = {
	credentials: 'same-origin'
}

class Users extends React.Component{
	constructor(props){
		super(props);
		this.state = { 
			users: [],
			nextId: null
		}
		this.loadStartPage = this.loadStartPage.bind(this)
		this.loadNextPage = this.loadNextPage.bind(this)
		this.loadPage = this.loadPage.bind(this)
	}
	
	loadStartPage(){
		this.loadPage(null)
	}
	
	loadNextPage(){
		this.loadPage(this.state.nextId)
	}
	
	componentWillMount(){
		this.loadPage(null)
	}
	
	loadPage(startId){
		var parameters = startId != null ? "?startId=" + startId : "";
		fetch("listUsers" + parameters, FETCH_OPTIONS)
		.then(response => response.json())
		.then(json => this.setState(json))
	}
	
	render(){
		return <UserList users={this.state.users} 
						loadNextPage={this.loadNextPage} 
						loadStartPage={this.loadStartPage} />
	}
}

const UserList = props =>
	<div>
		<table className="table table-condensed">
			<thead>
				<tr>
					<th>Username</th>
					<th>ID</th>
					<th>Token</th>
					<th></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				{props.users.map(user => 
					<tr>
						<td>{user.username}</td>
						<td>{user.id}</td>
						<td>{user.token}</td>
						<td>
							{user.hasPermissionRequest && 
								<span class="bg-warning">HAS PERMISSION REQUEST</span>
							}
						</td>
						<td><a href={"editUser?userId=" + user.id} class="glyphicon glyphicon-pencil"></a></td>
					</tr>
				)}
			</tbody>
		</table>
		<nav>
			<ul class="pager">
				<li><a onClick={props.loadStartPage}>Start</a></li>
				<li><a onClick={props.loadNextPage}>Next</a></li>
			</ul>
		</nav>
	</div>

ReactDOM.render(
	<div className="container">
		<h1>Users</h1>
		<Users />
	</div>,
	document.getElementById('app')
)