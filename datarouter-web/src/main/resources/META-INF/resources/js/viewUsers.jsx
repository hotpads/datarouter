const FETCH_OPTIONS = {
	credentials: 'same-origin'
}

const PAGE_SIZE = 20

class Users extends React.Component{
	constructor(props){
		super(props)
		this.state = { 
			users: [],
			filteredUsers: [],
			index: 0,
			requestorsOnly: false,
			emailFilter: ""
		}
		this.loadStartPage = this.loadStartPage.bind(this)
		this.loadPrevPage = this.loadPrevPage.bind(this)
		this.loadNextPage = this.loadNextPage.bind(this)
		this.loadData = this.loadData.bind(this)
		this.handleRequestorToggle = this.handleRequestorToggle.bind(this)
		this.handleEmailFilter = this.handleEmailFilter.bind(this)
	}
	
	loadStartPage(){
		this.setState({
			index: 0
		})
	}

	loadPrevPage(){
		this.setState((state, props) => ({
			index: state.index < PAGE_SIZE ? 0 : state.index - PAGE_SIZE
		}))
	}

	loadNextPage(){
		this.setState((state, props) => ({
			index: state.index + PAGE_SIZE < state.filteredUsers.length ? state.index + PAGE_SIZE : state.index
		}))
	}

	loadData(){
		fetch("listUsers", FETCH_OPTIONS)
				.then(response => response.json())
				.then(json => this.setState({
					users: json,
					filteredUsers: json
				}))
	}

	handleRequestorToggle(){
		this.setState((state, props) => {
			const newValue = !state.requestorsOnly
			const newFilteredUsers = this.applyFilters(state.users, newValue, state.emailFilter)
			return {
				requestorsOnly: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}

	handleEmailFilter(event){
		const newValue = event.target.value
		this.setState((state, props) => {
			const newFilteredUsers = this.applyFilters(state.users, state.requestorsOnly, newValue)
			return {
				emailFilter: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}

	applyFilters(users, requestorsOnly, emailFilter){
		return users.filter(user => {
			if(requestorsOnly && !user.hasPermissionRequest){
				return false
			}
			if(emailFilter.length > 0 && user.username.toLowerCase().indexOf(emailFilter.toLowerCase()) < 0){
				return false
			}
			return true
		})
	}

	componentWillMount(){
		this.loadData()
	}

	render(){
		return(
			<div>
				<Filters requestorsOnly={this.state.requestorsOnly}
						handleRequestorToggle={this.handleRequestorToggle}
						emailFilter={this.state.emailFilter}
						handleEmailFilter={this.handleEmailFilter} />
				<UserList users={this.state.filteredUsers}
						index={this.state.index}
						loadStartPage={this.loadStartPage}
						loadPrevPage={this.loadPrevPage}
						loadNextPage={this.loadNextPage} />
			</div>
		)
	}
}

const Filters = props =>
	<div>
		<label>Show requestors only: </label>
		<input type="checkbox" checked={props.requestorsOnly} onChange={props.handleRequestorToggle}/>
		<br/>
		<label>Email filter: </label>
		<input type="text" name="emailFilter" value={props.emailFilter} onChange={props.handleEmailFilter}/>
	</div>

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
				{props.users.slice(props.index, props.index + PAGE_SIZE)
						.map(user =>
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
						)
				}
			</tbody>
		</table>
		<nav>
			<ul class="pager">
				<li><a onClick={props.loadPrevPage}>Previous</a></li>
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