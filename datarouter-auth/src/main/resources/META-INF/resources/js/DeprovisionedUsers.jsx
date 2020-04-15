const FETCH_OPTIONS = {
	credentials: 'same-origin',
	headers: {'Content-Type': 'application/json'}
}

const PAGE_SIZE = 20

const DEFAULT_STATE = {
	users: [],
	filteredUsers: [],
	numCheckedToRestore: 0,
	numCheckedToDeprovision: 0,
	index: 0,
	emailFilter: '',
	isFlaggedOnlyChecked: false,
	isDeprovisionedOnlyChecked: false,
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
		this.selectAllFiltered = this.selectAllFiltered.bind(this)
		this.deselectAll = this.deselectAll.bind(this)
		this.handleToggleChecked = this.handleToggleChecked.bind(this)
		this.handleEmailFilter = this.handleEmailFilter.bind(this)
		this.handleToggleFlaggedOnly = this.handleToggleFlaggedOnly.bind(this)
		this.handleToggleDeprovisionedOnly = this.handleToggleDeprovisionedOnly.bind(this)
		this.restoreUsers = this.restoreUsers.bind(this)
		this.deprovisionUsers = this.deprovisionUsers.bind(this)
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

	selectAllFiltered(){
		this.setState((state, props) => {
			const currentFilteredUsernames = state.filteredUsers.reduce((acc, user) => {
				acc[user.username] = user.username
				return acc
			},{})
			let numAddedToRestore = 0, numAddedToDeprovision = 0
			const newUsers = state.users
					.map(user => {
						if(currentFilteredUsernames[user.username] === user.username && !user.isChecked){
							numAddedToRestore += user.status === 'DEPROVISIONED' ? 1 : 0
							numAddedToDeprovision += user.status === 'FLAGGED' ? 1 : 0
							return {...user, isChecked: true}
						}
						return user
					})
			return {
				numCheckedToRestore: state.numCheckedToRestore + numAddedToRestore,
				numCheckedToDeprovision: state.numCheckedToDeprovision + numAddedToDeprovision,
				users: newUsers,
				filteredUsers: this.applyFilters(newUsers, state.emailFilter, state.isFlaggedOnlyChecked, state.isDeprovisionedOnlyChecked)
			}
		})
	}

	deselectAll(){
		this.setState((state, props) => {
			const newUsers = state.users.map(user => {
				return {...user, isChecked: false}
			})
			return {
				numCheckedToRestore: 0,
				numCheckedToDeprovision: 0,
				users: newUsers,
				filteredUsers: this.applyFilters(newUsers, state.emailFilter, state.isFlaggedOnlyChecked, state.isDeprovisionedOnlyChecked)
			}
		})
	}

	handleToggleChecked(targetUsername){
		this.setState((state, props) => {
			const user = state.users.find(user => user.username === targetUsername)
			const isAdd = !user.isChecked
			const numCheckedToIncrement = user.status === 'FLAGGED' ? 'numCheckedToDeprovision' : user.status === 'DEPROVISIONED' ? 'numCheckedToRestore' : ''
			const newUsers = state.users.map(user => user.username === targetUsername ? {...user, isChecked: isAdd} : user)
			return {
				[numCheckedToIncrement]: state[numCheckedToIncrement] + (isAdd ? 1 : -1),
				users: newUsers ,
				filteredUsers: this.applyFilters(newUsers, state.emailFilter, state.isFlaggedOnlyChecked, state.isDeprovisionedOnlyChecked)
			}
		})
	}

	handleEmailFilter(event){
		const newValue = event.target.value
		this.setState((state, props) => {
			const newFilteredUsers = this.applyFilters(state.users, newValue, state.isFlaggedOnlyChecked, state.isDeprovisionedOnlyChecked)
			return {
				emailFilter: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}
	
	handleToggleFlaggedOnly(event){
		const newValue = event.target.checked
		this.setState((state, props) => {
			const newFilteredUsers = this.applyFilters(state.users, state.emailFilter, newValue, state.isDeprovisionedOnlyChecked)
			return {
				isFlaggedOnlyChecked: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}
	
	handleToggleDeprovisionedOnly(event){
		const newValue = event.target.checked
		this.setState((state, props) => {
			const newFilteredUsers = this.applyFilters(state.users, state.emailFilter, state.isFlaggedOnlyChecked, newValue)
			return {
				isDeprovisionedOnlyChecked: newValue,
				filteredUsers: newFilteredUsers,
				index: 0
			}
		})
	}

	restoreUsers(){
		const message = `Are you sure you want to restore ${this.state.numCheckedToRestore} user${this.state.numCheckedToRestore > 1 ? 's' : ''}?`
		if(!window.confirm(message)){
			return
		}
		const usernamesToRestore = this.state.users.filter(user => user.isChecked && user.status === 'DEPROVISIONED')
				.map(user => user.username)
		fetch(PATH + '/restoreUsers', {
					...FETCH_OPTIONS, method: 'POST', body: JSON.stringify({usernamesToRestore: usernamesToRestore})
				}).then(response => response.json())
				.then(json => {
					const restoredUsernames = json.restoredUsernames.length ? json.restoredUsernames.toString() : 'None'
					this.loadData('Restored usernames: ' + restoredUsernames)
				})
	}

	deprovisionUsers(){
		const message = `Are you sure you want to deprovision ${this.state.numCheckedToDeprovision} user${this.state.numCheckedToDeprovision > 1 ? 's' : ''}?`
		if(!window.confirm(message)){
			return
		}
		const usernamesToDeprovision = this.state.users.filter(user => user.isChecked && user.status === 'FLAGGED')
				.map(user => user.username)
		fetch(PATH + '/deprovisionUsers', {
					...FETCH_OPTIONS, method: 'POST', body: JSON.stringify({usernamesToDeprovision: usernamesToDeprovision})
				}).then(response => response.json())
				.then(json => {
					const deprovisionedUsernames = json.deprovisionedUsernames.length ? json.deprovisionedUsernames.toString() : 'None'
					this.loadData('Deprovisioned usernames: ' + deprovisionedUsernames)
				})
	}

	applyFilters(users, emailFilter, isFlaggedOnlyChecked, isDeprovisionedOnlyChecked){
		return users.filter(user => {
			if(emailFilter.length > 0 && user.username.toLowerCase().indexOf(emailFilter.toLowerCase()) < 0){
				return false
			}
			if(isFlaggedOnlyChecked && user.status !== 'FLAGGED'){
				return false
			}
			if(isDeprovisionedOnlyChecked && user.status !== 'DEPROVISIONED'){
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
				<Actions numCheckedToRestore={this.state.numCheckedToRestore}
						numCheckedToDeprovision={this.state.numCheckedToDeprovision}
						optionalMessage={this.state.optionalMessage}
						selectAllFiltered={this.selectAllFiltered}
						deselectAll={this.deselectAll}
						restoreUsers={this.restoreUsers}
						deprovisionUsers={this.deprovisionUsers} />
				<Filters emailFilter={this.state.emailFilter}
						handleEmailFilter={this.handleEmailFilter}
						isFlaggedOnlyChecked={this.state.isFlaggedOnlyChecked}
						handleToggleFlaggedOnly={this.handleToggleFlaggedOnly}
						isDeprovisionedOnlyChecked={this.state.isDeprovisionedOnlyChecked}
						handleToggleDeprovisionedOnly={this.handleToggleDeprovisionedOnly} />
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

const Actions = props => {
	const restoreButtonLabel = 'Restore ' + props.numCheckedToRestore + (props.numCheckedToRestore === 1 ? ' User' : ' Users')
	const deprovisionButtonLabel = 'Deprovision ' + props.numCheckedToDeprovision + (props.numCheckedToDeprovision === 1 ? ' User' : ' Users')
	return (
		<div>
			{props.optionalMessage &&
				<div>
					<p>{props.optionalMessage}</p>
				</div>
			}
			<div class="btn-toolbar">
				<button type="button" class="btn btn-danger" disabled={props.numCheckedToRestore === 0} onClick={props.restoreUsers}>{restoreButtonLabel}</button>
				<button type="button" class="btn btn-danger mx-2" disabled={props.numCheckedToDeprovision === 0} onClick={props.deprovisionUsers}>{deprovisionButtonLabel}</button>
				<button type="button" class="btn btn-primary" onClick={props.selectAllFiltered}>Select All Filtered</button>
				<button type="button" class="btn btn-primary mx-2" onClick={props.deselectAll}>Deselect All</button>
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
		<div class="form-inline">
			<div class="form-group">
				<input type="checkbox" class="form-check-input" name="flaggedOnly" class="form-control" checked={props.isFlaggedOnlyChecked} onChange={props.handleToggleFlaggedOnly} id="flaggedOnly"/>
				<label for="flaggedOnly" class="form-check-label">FLAGGED Only</label>
			</div>
			<div class="form-group mx-2">
				<input type="checkbox" name="deprovisionedOnly" class="form-control" checked={props.isDeprovisionedOnlyChecked} onChange={props.handleToggleDeprovisionedOnly} id="deprovisionedOnly"/>
				<label for="deprovisionedOnly">DEPROVISIONED Only</label>
			</div>
		</div>
	</div>

const UserList = props =>
	<div>
		<table className="table table-condensed">
			<thead>
				<tr>
					<th>Username</th>
					<th>Deleted Roles</th>
					<th>Status</th>
					<th>Restore</th>
					<th>Deprovision</th>
				</tr>
			</thead>
			<tbody>
				{props.users.slice(props.index, props.index + PAGE_SIZE)
						.map(user =>
							<tr>
								<td>{user.username}</td>
								<td>{user.roles.toString()}</td>
								<td>{user.status}</td>
								<td>
									<input type="checkbox" checked={user.status === 'DEPROVISIONED' && user.isChecked} disabled={user.status !== 'DEPROVISIONED'} onChange={() => props.handleToggleChecked(user.username)}/>
								</td>
								<td>
									<input type="checkbox" checked={user.status === 'FLAGGED' && user.isChecked} disabled={user.status !== 'FLAGGED'} onChange={() => props.handleToggleChecked(user.username)}/>
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
		<h1>User Deprovisioning</h1>
		<Users />
	</div>,
	document.getElementById('app')
)