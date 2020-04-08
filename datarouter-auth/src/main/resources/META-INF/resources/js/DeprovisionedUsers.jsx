const FETCH_OPTIONS = {
	credentials: 'same-origin',
	headers: {'Content-Type': 'application/json'}
}

const PAGE_SIZE = 20

const DEFAULT_STATE = {
	users: [],
	filteredUsers: [],
	numChecked: 0,
	index: 0,
	emailFilter: '',
	optionalMessage: null
}

class Users extends React.Component{
	constructor(props){
		super(props)
		this.state = DEFAULT_STATE
		this.loadStartPage = this.loadStartPage.bind(this)
		this.loadPrevPage = this.loadPrevPage.bind(this)
		this.loadNextPage = this.loadNextPage.bind(this)
		this.loadData = this.loadData.bind(this)
		this.setAllChecked = this.setAllChecked.bind(this)
		this.handleToggleChecked = this.handleToggleChecked.bind(this)
		this.handleEmailFilter = this.handleEmailFilter.bind(this)
		this.restoreUsers = this.restoreUsers.bind(this)
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

	loadData(optionalMessage){
		fetch(PATH + '/fetchDeprovisionedUsers', FETCH_OPTIONS)
				.then(response => response.json())
				.then(json => {
					const users = json.deprovisionedUsers.map(user => {
						return {...user, isChecked: false}
					})
					this.setState({
						...DEFAULT_STATE,
						users: users,
						filteredUsers: users,
						optionalMessage: optionalMessage ? optionalMessage : null
					})
				})
	}

	setAllChecked(trueOrFalse){
		this.setState((state, props) => {
			const newUsers = state.users.map(user => {
				return {...user, isChecked: trueOrFalse}
			})
			return {
				numChecked: trueOrFalse ? newUsers.length : 0,
				users: newUsers,
				filteredUsers: this.applyFilters(newUsers, state.emailFilter)
			}
		})
	}

	handleToggleChecked(targetUsername){
		this.setState((state, props) => {
			const isAdd = !state.users.find(user => user.username === targetUsername).isChecked
			const newUsers = state.users.map(user => user.username === targetUsername ? {...user, isChecked: isAdd} : user)
			return {
				numChecked: state.numChecked + (isAdd ? 1 : -1),
				users: newUsers ,
				filteredUsers: this.applyFilters(newUsers, state.emailFilter)
			}
		})
	}

	handleEmailFilter(event){
		const newValue = event.target.value
		this.setState((state, props) => {
			const newFilteredUsers = this.applyFilters(state.users, newValue)
			return {
				emailFilter: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}

	restoreUsers(){
		const usernamesToRestore = this.state.users.filter(user => user.isChecked)
				.map(user => user.username)
		fetch(PATH + '/restoreUsers', {
					...FETCH_OPTIONS, method: 'POST', body: JSON.stringify({usernamesToRestore: usernamesToRestore})
				}).then(response => response.json())
				.then(json => {
					const restoredUsernames = json.restoredUsernames.length ? json.restoredUsernames.toString() : 'None'
					this.loadData('Restored usernames: ' + restoredUsernames)
				})
	}

	applyFilters(users, emailFilter){
		return users.filter(user => {
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
				<Restore numChecked={this.state.numChecked}
						optionalMessage={this.state.optionalMessage}
						setAllChecked={this.setAllChecked}
						restoreUsers={this.restoreUsers} />
				<Filters emailFilter={this.state.emailFilter}
						handleEmailFilter={this.handleEmailFilter} />
				<UserList users={this.state.filteredUsers}
						index={this.state.index}
						handleToggleChecked={this.handleToggleChecked}
						loadStartPage={this.loadStartPage}
						loadPrevPage={this.loadPrevPage}
						loadNextPage={this.loadNextPage} />
			</div>
		)
	}
}

const Restore = props => {
	const buttonLabel = 'Restore ' + props.numChecked + (props.numChecked === 1 ? ' User' : ' Users')
	return (
		<div>
			{props.optionalMessage &&
				<div>
					<p>{props.optionalMessage}</p>
				</div>
			}
			<div>
				<button type="button" class="btn btn-danger" disabled={props.numChecked === 0} onClick={props.restoreUsers}>{buttonLabel}</button>
				<button type="button" class="btn btn-primary" onClick={() => props.setAllChecked(true)}>Select All</button>
				<button type="button" class="btn btn-primary" onClick={() => props.setAllChecked(false)}>Deselect All</button>
			</div>
		</div>
	)
}

const Filters = props =>
	<div class="form-group">
		<div class="form-group">
			<label for="emailFilter"> Email Filter: </label>
			<input type="text" name="emailFilter" class="form-control" value={props.emailFilter} onChange={props.handleEmailFilter} id="emailFilter"/>
		</div>
	</div>

const UserList = props =>
	<div>
		<table className="table table-condensed">
			<thead>
				<tr>
					<th>Username</th>
					<th>Deleted Roles</th>
					<th>Restore</th>
				</tr>
			</thead>
			<tbody>
				{props.users.slice(props.index, props.index + PAGE_SIZE)
						.map(user =>
							<tr>
								<td>{user.username}</td>
								<td>{user.roles.toString()}</td>
								<td>
									<input type="checkbox" checked={user.isChecked} onChange={() => props.handleToggleChecked(user.username)}/>
								</td>
							</tr>
						)
				}
			</tbody>
		</table>
		<nav>
			<ul class="pagination">
				<li><a class="page-link" onClick={props.loadPrevPage}>Previous</a></li>
				<li><a class="page-link" onClick={props.loadStartPage}>Start</a></li>
				<li><a class="page-link" onClick={props.loadNextPage}>Next</a></li>
			</ul>
		</nav>
	</div>

ReactDOM.render(
	<div className="container">
		<h1>Deprovisioned Users</h1>
		<Users />
	</div>,
	document.getElementById('app')
)