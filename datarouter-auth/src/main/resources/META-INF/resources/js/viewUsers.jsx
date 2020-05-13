const FETCH_OPTIONS = {
	credentials: 'same-origin'
}

const FETCH_POST_OPTIONS = {
	...FETCH_OPTIONS,
	method: 'POST',
	headers: {'Content-Type': 'application/json'}
}

const PAGE_SIZE = 20

const doFetch = (path, extraOptions, bodyObject, onSuccess, onError) => {
	extraOptions = extraOptions || {}
	const allOptions = bodyObject ? {...FETCH_POST_OPTIONS, ...extraOptions, body: JSON.stringify(bodyObject)} : {...FETCH_OPTIONS, ...extraOptions}
	fetch(path, allOptions)
			.then(response => {
				if(!response.ok){
					throw new Error(`HTTP ${response.status}`)
				}
				try{
					return response.json()
				}catch(e){
					throw new Error('Fetch request succeeded but JSON parsing failed.')
				}
			}).then(json => {
				try{
					//TODO DATAROUTER-2788 determine success or failure based on standard fields and call onSuccess or onError
					onSuccess && onSuccess(json)
				}catch(e){
					throw new Error('Fetch request succeeded but response processing failed.')
				}
			}).catch(error => {
				onError && onError(error)
			})
}

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
		this.setState((state) => ({
			index: state.index < PAGE_SIZE ? 0 : state.index - PAGE_SIZE
		}))
	}

	loadNextPage(){
		this.setState((state) => ({
			index: state.index + PAGE_SIZE < state.filteredUsers.length ? state.index + PAGE_SIZE : state.index
		}))
	}

	loadData(){
		fetch("listUsers", FETCH_OPTIONS)//TODO DATAROUTER-2788 (and path)
				.then(response => response.json())
				.then(json => this.setState({
					users: json,
					filteredUsers: json
				}))
	}

	handleRequestorToggle(){
		this.setState((state) => {
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
		this.setState((state) => {
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
		if(!this.props.display){
			return null
		}
		return(
			<div>
				<h1>Users</h1>
				<Filters requestorsOnly={this.state.requestorsOnly}
						handleRequestorToggle={this.handleRequestorToggle}
						emailFilter={this.state.emailFilter}
						handleEmailFilter={this.handleEmailFilter} />
				<UserList users={this.state.filteredUsers}
						index={this.state.index}
						loadStartPage={this.loadStartPage}
						loadPrevPage={this.loadPrevPage}
						loadNextPage={this.loadNextPage}
						openEditUser={this.props.openEditUser} />
			</div>
		)
	}
	
}

const Filters = props =>
	<div class="form-group">
		<div class="form-check">
			<label class="form-check-label">
				<input class="form-check-input" type="checkbox" checked={props.requestorsOnly} onChange={props.handleRequestorToggle}/>
				Show requestors only
			</label>
		</div>
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
								<td><Badges badges={user.hasPermissionRequest ? ['Permission Request'] : []} /></td>
								<td><button type="button" class="btn btn-primary" name={user.username} onClick={props.openEditUser}>Edit</button></td>
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

const Badges = props => {
	const badges = props.badges || []
	return <h4>
		{badges.map((content, index) => <span class={'badge badge-danger' + (index < badges.length - 1 ? ' mr-3' : '')}>{content}</span>)}
	</h4>
}

class ViewUser extends React.Component{

	constructor(props){
		super(props)
		this.state = {
			loaded: false,
			error: '',
			username: props.defaultUsername,
			id: null,
			token: null,
			requests: [],
			deprovisionedUserDto: {},
			availableRoles: [],
			currentRoles: [],
			availableAccounts: [],
			currentAccounts: [],
			newPassword: ''
		}
		this.updateUserDetails = this.updateUserDetails.bind(this)
		this.refresh = this.refresh.bind(this)
		this.handleToggleRole = this.handleToggleRole.bind(this)
		this.handleToggleAccount = this.handleToggleAccount.bind(this)
	}

	updateUserDetails(userDetails, callback = () => {}){
		if(userDetails.success){
			this.setState({
				loaded: true,
				error: '',
				username: userDetails.username,
				id: userDetails.id,
				token: userDetails.token,
				requests: userDetails.requests,
				deprovisionedUserDto: userDetails.deprovisionedUserDto,
				availableRoles: userDetails.availableRoles,
				currentRoles: userDetails.currentRoles,
				availableAccounts: userDetails.availableAccounts,
				currentAccounts: userDetails.currentAccounts
			}, callback)
		}else{
			this.setState({error: userDetails.message, loaded:false})
		}
	}

	refresh(callback){
		const queryParam = '?username=' + encodeURIComponent(this.state.username)
		doFetch(PATHS.getUserDetails + queryParam, {}, null, userDetails => this.updateUserDetails(userDetails, callback),
				error => {
					this.setState({error: error, loaded: false})
				})
	}

	handleToggleRole(event){
		this.handleToggle('currentRoles', event.target.id)
	}

	handleToggleAccount(event){
		this.handleToggle('currentAccounts', event.target.id)
	}

	handleToggle(object, key){
		this.setState((state) => {
			return {[object]: {...state[object], [key]: !state[object][key]}}
		})
	}

	componentDidMount(){
		if(this.state.username){
			this.refresh()
		}
	}

	render(){
		if(!this.props.display){
			return null
		}

		const header = <h1>Edit User <button type="button" class="btn btn-primary" onClick={this.props.closeEditUser}>Back to User List</button></h1>
		if(this.state.error && !this.state.loaded){
			return <div>
				{header}
				<div class="alert-danger">
					<h3>{'Failed to load user. ' + this.state.error}</h3>
				</div>
			</div>
		}

		const deprovisioned = this.state.loaded && !this.state.deprovisionedUserDto.status.isUserEditable
		return this.state.loaded && <div>
			{header}
			<UserInformationTable username={this.state.username}
				id={this.state.id}
				token={this.state.token}
				deprovisioned={deprovisioned}
				hasOpenPermissionRequest={this.state.requests.length > 0 && !this.state.requests[0].resolution} />
			<ProvisioningStatusCard deprovisionedUserDto={this.state.deprovisionedUserDto}
				refresh={this.refresh} />
			<EditRolesAndAccountsCard id={this.state.id}
				username={this.state.username}
				token={this.state.token}
				availableRoles={this.state.availableRoles}
				currentRoles={this.state.currentRoles}
				availableAccounts={this.state.availableAccounts}
				currentAccounts={this.state.currentAccounts}
				disabled={deprovisioned}
				handleToggleRole={this.handleToggleRole}
				handleToggleAccount={this.handleToggleAccount}
				updateUserDetails={this.updateUserDetails} />
			<EditPasswordCard username={this.state.username}
				disabled={deprovisioned}
				updateUserDetails={this.updateUserDetails} />
			<PermissionRequestsCard id={this.state.id}
				requests={this.state.requests}
				refresh={this.refresh}  />
		</div>
	}

}

const withAlertCardContainer = (WrappedComponent, headerText) => {
	return class extends React.Component{

		constructor(props){
			super(props)
			this.handle = this.handle.bind(this)
			this.handleDanger = this.handleDanger.bind(this)
			this.handleSuccess = this.handleSuccess.bind(this)
			this.dismiss = this.dismiss.bind(this)
			
			this.state = {
				display: false,
				bootstrapClass: '',
				message: ''
			}
		}

		handle(bootstrapClass, message){
			this.setState({display: true, bootstrapClass: bootstrapClass, message: message})
		}

		handleDanger(message){
			this.handle('alert-danger', message)
		}

		handleSuccess(message){
			this.handle('alert-success', message)
		}

		dismiss(){
			this.setState({display: false})
		}

		render(){
			return <div class="card mb-3">
				<div class="card-header">{headerText}</div>
				<div class="card-body">
					{this.state.display
						&& <div class={'alert ' + this.state.bootstrapClass}>
							<p>{this.state.message}</p>
							<span class="btn btn-link font-weight-light font-italic" onClick={this.dismiss}>
							<small>dismiss</small>
							</span>
						</div>}
					<WrappedComponent
						handleDanger={this.handleDanger}
						handleSuccess={this.handleSuccess}
						{...this.props} />
				</div>
			</div>
		}

	}
}

const UserInformationTable = props => {
	const deprovisionedBadge = props.deprovisioned ? ['Deprovisioned'] : []
	const permissionRequestBadge = props.hasOpenPermissionRequest ? ['Permission Request'] : []
	return <div>
		<table className="table table-condensed">
			<thead>
				<tr>
					<th>Username</th>
					<th>ID</th>
					<th>Token</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>{props.username}</td>
					<td>{props.id}</td>
					<td>{props.token}</td>
					<td><Badges badges={[...deprovisionedBadge, ...permissionRequestBadge]} /></td>
				</tr>
			</tbody>
		</table>
	</div>
}

class ProvisioningStatusForm extends React.Component{

	constructor(props){
		super(props)
		this.handleDeprovision = this.handleDeprovision.bind(this)
		this.handleRestore = this.handleRestore.bind(this)
	}

	handleDeprovision(event){
		event.preventDefault()
		const username = this.props.deprovisionedUserDto.username
		doFetch(PATHS.deprovisionUsers, {}, {usernamesToDeprovision: [username]}, json => {
					this.props.refresh()
				}, error => {
					this.props.handleDanger('Failed to deprovisioned user. ' + error)
				})
	}

	handleRestore(event){
		event.preventDefault()
		const username = this.props.deprovisionedUserDto.username
		doFetch(PATHS.restoreUsers, {}, {usernamesToRestore: [username]}, json => {
					this.props.refresh()
				}, error => {
					this.props.handleDanger('Failed to restore user. ' + error)
				})
	}

	render(){
		const status = this.props.deprovisionedUserDto.status
		let description = status.description
		if(!status.isUserEditable){
			const roles = this.props.deprovisionedUserDto.roles.reduce((acc, curr) => acc.length === 0 ? curr : acc + ', ' + curr, '')
			description += ' Roles at time of deprovisioning: ' + roles
		}
		return <form>
			<p>{description}</p>
			<hr/>
			<button type="submit" class="btn btn-danger" hidden={!status.allowDeprovision} onClick={this.handleDeprovision}>Disable User and Remove Roles</button>
			<button type="submit" class="btn btn-danger" hidden={!status.allowRestore} onClick={this.handleRestore}>Enable User and Restore Roles</button>
		</form>
	}
}

const ProvisioningStatusCard = withAlertCardContainer(ProvisioningStatusForm, 'Provisioning Status')

class EditRolesAndAccounts extends React.Component{

	constructor(props){
		super(props)
		this.handleSubmit = this.handleSubmit.bind(this)
	}

	handleSubmit(event){
		event.preventDefault()
		const editUserDetailsDto = {
			username: this.props.username,
			id: this.props.id,
			token: this.props.token,
			currentRoles: this.props.currentRoles,
			currentAccounts: this.props.currentAccounts
		}
		doFetch(PATHS.updateUserDetails, {}, editUserDetailsDto, userDetails => {
					if(userDetails.success){
						this.props.updateUserDetails(userDetails)
					}else{
						this.props.handleDanger('Failed to update. ' + userDetails.message)
					}
				}, error => {
					this.props.handleDanger('Failed to update. ' + error)
				})
	}

	render(){
		return <div>
			<div class="row">
				<div class="col-sm-6">
					<CheckList title={'Roles'}
						plural={'roles'}
						orderedKeys={this.props.availableRoles}
						checkedKeys={this.props.currentRoles}
						disabled={this.props.disabled}
						handleToggle={this.props.handleToggleRole}
						/>
				</div>
				<div class="col-sm-6">
					<CheckList title={'Accounts'}
						plural={'accounts'}
						orderedKeys={this.props.availableAccounts}
						checkedKeys={this.props.currentAccounts}
						disabled={this.props.disabled}
						handleToggle={this.props.handleToggleAccount}
						/>
				</div>
			</div>
			<hr/>
			<button class="btn btn-primary mx-auto" type="submit" disabled={this.props.disabled} onClick={this.handleSubmit}>Save Role and Account Changes</button>
		</div>
	}

}

const CheckList = props => {
	const {title, plural, orderedKeys, checkedKeys, disabled, handleToggle} = props
	return <div>
		<h3 class="card-title">{title}</h3>
			{orderedKeys.length === 0 ? 'No ' + plural + ' available.'
					: orderedKeys.map(key =>
						<div class="form-check" key={key}>
							<input type="checkbox" class="form-check-input" id={key} disabled={disabled} checked={checkedKeys[key]} onChange={handleToggle} />
							<label class="form-check-label" for={key}>{key}</label>
						</div>
					)
				}
	</div>
}

const EditRolesAndAccountsCard = withAlertCardContainer(EditRolesAndAccounts, 'Edit Roles and Accounts')

class EditPasswordForm extends React.Component{

	constructor(props){
		super(props)
		this.handleNewPasswordInput = this.handleNewPasswordInput.bind(this)
		this.handleUpdatePassword = this.handleUpdatePassword.bind(this)
		
		this.state = {
			newPassword: ''
		}
	}

	handleNewPasswordInput(event){
		this.setState({newPassword: event.target.value})
	}

	handleUpdatePassword(event){
		event.preventDefault()
		doFetch(PATHS.updatePassword, {}, {username: this.props.username, newPassword: this.state.newPassword}, userDetails => {
					if(userDetails.success){
						this.props.updateUserDetails(userDetails, () => {this.setState({newPassword: ''})})
					}else{
						this.props.handleDanger('Failed to update password. ' + userDetails.message)
					}
				}, error => {
					this.props.handleDanger('Failed to update password. ' + error)
				})
	}

	render(){
		return <form class="form-inline">
			<div class="form-group mr-3">
				<label for="newPassword" class="mr-3">New Password</label>
				<input type="text" class="form-control" id="newPassword" disabled={this.props.disabled} value={this.state.newPassword} onChange={this.handleNewPasswordInput}/>
			</div>
			<button type="submit" class="btn btn-primary" disabled={this.props.disabled} onClick={this.handleUpdatePassword}>Save New Password</button>
		</form>
	}
}

const EditPasswordCard = withAlertCardContainer(EditPasswordForm, 'Edit Password')

class PermissionRequests extends React.Component{

	constructor(props){
		super(props)
		this.handleDecline = this.handleDecline.bind(this)
	}

	handleDecline(event){
		event.preventDefault()
		doFetch(PATHS.declinePermissionRequests + '?userId=' + this.props.id, {}, {}, json => {
					if(json.success){
						this.props.refresh()
					}else{
						this.props.handleDanger('Failed to decline. ' + json.message)
					}
				}, error => {
					this.props.handleDanger('Failed to decline. ' + error)
				})
	}

	render(){
		const props = this.props
		return <div>
			{props.requests.length === 0 ? <p>No permission requests.</p>
					: <table class="table table-sm table-hover">
						<thead>
							<tr>
								<th>Request Time</th>
								<th>Request Text</th>
								<th>Resolution</th>
								<th>Resolution Time</th>
							</tr>
						</thead>
						<tbody>
							{props.requests.map(request => {
								const {requestTime, requestText, resolution, resolutionTime} = request
								const resolved = !!resolution
								return (
									<tr class={resolved ? '' : 'table-warning'}>
										<td>{requestTime}</td>
										<td>{requestText}</td>
										<td>{resolved ? resolution : <button type="submit" class="btn btn-danger" onClick={this.handleDecline}>Decline</button>}</td>
										<td>{resolved ? resolutionTime : ''}</td>
									</tr>
								)
							})}
						</tbody>
					</table>
			}
		</div>
	}

}

const PermissionRequestsCard = withAlertCardContainer(PermissionRequests, 'Permission Request History')

class ListAndEditUserPage extends React.Component{

	constructor(props){
		super(props)
		this.state = this.determineInitialState()

		this.openEditUser = this.openEditUser.bind(this)
		this.closeEditUser = this.closeEditUser.bind(this)
		this.handleOnPopState = this.handleOnPopState.bind(this)

		window.onpopstate = this.handleOnPopState
	}

	determineInitialState(){
		const usernameToEdit = INITIAL_USERNAME
		return {
			activeUsername: usernameToEdit ? usernameToEdit : null,
			isEditing: usernameToEdit ? true : false
		}
	}

	openEditUser(event){
		this.setState({
			activeUsername: event.target.name,
			isEditing: true
		}, () => {
			history.pushState(this.state, null, PATHS.editUser + '?username=' + encodeURIComponent(this.state.activeUsername))
			document.title = 'Datarouter - Edit User ' + this.state.activeUsername
		})
	}

	closeEditUser(){
		this.setState({isEditing:false}, () => {
			history.pushState(this.state, null, PATHS.viewUsers)
			document.title = 'Datarouter - Users'
		})
	}

	handleOnPopState(event){
		const stateToLoad = event.state ? event.state : this.determineInitialState()
		//avoid setting activeUsername unless the user is actually being edited, since it can trigger API requests
		const stateToSet = stateToLoad.isEditing ? stateToLoad : {isEditing: false}
		this.setState(stateToSet)
	}

	render(){
		return <div>
			<ViewUser display={this.state.isEditing === true}
				key={this.state.activeUsername}
				defaultUsername={this.state.activeUsername}
				closeEditUser={this.closeEditUser}/>
			<Users display={this.state.isEditing === false}
				openEditUser={this.openEditUser}/>
		</div>
	}

}

ReactDOM.render(
	<div>
		<div className="container">
			<ListAndEditUserPage />
		</div>
	</div>,
	document.getElementById('app')
)