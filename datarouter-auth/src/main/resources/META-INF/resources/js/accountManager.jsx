const { Router, Redirect, Route, IndexRoute, Link, browserHistory } = ReactRouter;

const Fetch = {
	fetchJson: (...args) => fetch(...args).then(response => response.json()),
	fetchOnly: (...args) => fetch(...args),
	get: (url) => Fetch.fetchJson(url, {
		method: 'GET',
		credentials: 'same-origin',
	}),
	post: (url, data, returnsJson = true) => Fetch[returnsJson ? 'fetchJson' : 'fetchOnly'](url, {
		method: 'POST',
		body: $.param(data),
		credentials: 'same-origin',
		headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'}
	}),
	postJson: (url, data, returnsJson = true) => Fetch[returnsJson ? 'fetchJson' : 'fetchOnly'](url, {
		method: 'POST',
		body: JSON.stringify(data),
		credentials: 'same-origin',
		headers: {'Content-Type': 'application/json;charset=UTF-8'}
	})
}

const SubmitButton = ({compact, className='', ...props}) => (
	<input
		type="submit"
		className={`btn btn-outline-secondary ${compact ? 'btn-sm' : ''} ${className}`}
		style={compact ? {margin: '0 5px'} : {}}
		{...props}
	/>
)

class AddAccountForm extends React.Component{
	constructor(props){
		super(props)
		this.state = {
			accountName:'', 
			callerTypes:[],
			selectedCallerType:''}
	}

	componentWillMount(){
		Fetch.get('getAvailableCallerTypes')
			.then(callerTypes => this.setState({callerTypes, selectedCallerType: callerTypes[0]}))
	}

	handleChange = (event) => {
		this.setState({accountName: event.target.value})
	}

	handleChange2 = (event) => {
		this.setState({selectedCallerType: event.target.value})
	}

	submitForm = (event) => {
		event.preventDefault()
		this.props.addAccount(this.state.accountName, this.state.selectedCallerType)
	}

	render(){
		return (
			<form>
				<h3>Create Account</h3>
				<div className="form-group">
					<label>Account Name</label>
					<div className="input-group">
						<input 
								type="text" 
								className="form-control" 
								value={this.state.accountName} 
								onChange={this.handleChange} />
						<select 
								class="form-control" 
								value={this.state.selectedCallerType} 
								onChange={this.handleChange2}>
						{this.state.callerTypes.map((callerType) => (<option value={callerType}>{callerType}</option>))}
						</select>
						<span className="input-group-append">
							<SubmitButton
								className="form-control-static btn"
								onClick={this.submitForm}
								disabled={!this.state.accountName}
								value="Create account"
							/>
						</span>
					</div>
				</div>
			</form>
		)
	}
}

class AccountLookupForm extends React.Component{

	constructor(props){
		super(props)
		this.state = {apiKey: '', accounts: [], message: null}
	}

	handleChange = (event) => {
		this.setState({apiKey: event.target.value})
	}

	submitForm = (event) => {
		event.preventDefault()
		Fetch.post('lookupAccount', {apiKey: this.state.apiKey})
			.then(response => {
				if(response && response.length){
					console.log('stuff')
					this.setState({message: null, accounts: response})
				}else{
					this.setState({message: 'Not found.', accounts: []})
				}
			})
	}

	render(){
		return (
			<form>
				<h3>Lookup Account by apiKey</h3>
				<div className="form-group">
					<label>apiKey</label>
					<div className="input-group">
						<input type="text" placeholder="apiKey (xx*xx allowed)" className="form-control" value={this.state.apiKey} onChange={this.handleChange} />
						<span className="input-group-append">
							<SubmitButton
								className="form-control-static btn"
								onClick={this.submitForm}
								disabled={!this.state.apiKey}
								value="Lookup account"
							/>
						</span>
					</div>
				</div>
				{this.state.accounts.map(account =>
					<div>
					<Link to={REACT_BASE_PATH + "details/" + account.accountName} title="Edit account">
						Account name: {account.accountName} {account.secretName ? ' (Secret name: ' + account.secretName + ')' : ''}
					</Link>
					</div>
				)}
				{this.state.message && <p>{this.state.message}</p>}
			</form>
		)
	}
}

const AccountTable = ({accountDetails, deleteAccount}) => (
	<table className="sortable table table-condensed table-striped">
		<thead>
			<tr>
				<th>Account name</th>
				<th>Last used</th>
				<th>Credentials</th>
				<th>Secret Credentials</th>
				<th>Permissions</th>
				<th>User Mappings</th>
				<th>Caller Type</th>
				<th>Metrics</th>
				<th>Edit</th>
				<th>Delete</th>
			</tr>
		</thead>
		<tbody>
			{accountDetails.map(({account, credentials, secretCredentials, permissions, metricLink}) => (
				<tr key={account.accountName}>
					<td>{account.accountName}</td>
					<td sorttable_customkey={account.lastUsedMs}>{account.lastUsed}</td>
					<td className={credentials.length ? '' : 'table-warning'}>
						{credentials.length ? credentials.length : 'No credentials'}
					</td>
					<td className={secretCredentials.length ? '' : 'table-warning'}>
						{secretCredentials.length ? secretCredentials.length : 'No secret credentials'}
					</td>
					<td className={permissions.length ? '' : 'table-warning'}>
						{permissions.length ? (
							<span
								data-toggle="tooltip"
								title={`Permissions for ${account.accountName}:\n`
									+ permissions
										.map((permission, idx) => `${idx+1}. ${permission.endpoint}`)
										.join('\n')}
								className="badge"
							>
								{permissions.length}
							</span>
						) : (
							'No permissions'
						)}
					</td>
					<td>
						{account.enableUserMappings
							? (<i className="fas fa-check"></i>)
							: (<span></span>)
						}
					</td>
					<td>{account.callerType}</td>
					<td>
						<a href={metricLink} target="_blank" title="Account usage metrics">
							<i class="fa fa-chart-line"></i>
						</a>
					</td>
					<td>
						<Link to={REACT_BASE_PATH + "details/" + account.accountName} title="Edit account">
							<i class="fa fa-edit"></i>
						</Link>
					</td>
					<td>
						<a onClick={() => deleteAccount(account.accountName)} title="Delete account">
							<i className="fas fa-trash"></i>
						</a>
					</td>
				</tr>
			))}
		</tbody>
	</table>
)

const CredentialTable = ({credentials, addCredential, deleteCredential, setCredentialActivation}) => (
	<div>
		<h3>Credentials {!credentials.length && <span className="alert alert-warning">(This account has no credentials)</span>}</h3>
		{credentials.length ? <table className="sortable table table-condensed table-striped">
			<thead>
				<tr>
					<th>API key</th>
					<th>Secret key</th>
					<th>Last used</th>
					<th>Created</th>
					<th>Creator Username</th>
					<th>Status</th>
					<th>Delete</th>
				</tr>
			</thead>
			<tbody>
				{credentials.map(({apiKey, secretKey, lastUsed, created, creatorUsername, active}) => (
					<tr key={apiKey}>
						<td>{apiKey}</td>
						<td>{secretKey}</td>
						<td>{lastUsed}</td>
						<td>{created}</td>
						<td>{creatorUsername}</td>
						<ActivationTd
								active={active}
								keyName="apiKey"
								value={apiKey}
								message={`Are you sure you want to deactivate the credential with apiKey: \n${apiKey}? It can be activated again later.`}
								setCredentialActivation={setCredentialActivation}/>
						<td>
							<a onClick={() => deleteCredential(apiKey)} title="Delete credential">
								<i className="fas fa-trash"></i>
							</a>
						</td>
					</tr>
				))}
			</tbody>
		</table> : ''}
		<SubmitButton
			className="form-control-static btn"
			onClick={addCredential}
			value="Add credential"
		/>
	</div>
)

const SecretCredentialTable = ({secretCredentials, addSecretCredential, deleteSecretCredential,
		setCredentialActivation}) => (
	<div>
		<h3>Secret Credentials {!secretCredentials.length && <span className="alert alert-warning">(This account has no secret credentials)</span>}</h3>
		{secretCredentials.length ? <table className="sortable table table-condensed table-striped">
			<thead>
				<tr>
					<th>Secret Name</th>
					<th>Last used</th>
					<th>Created</th>
					<th>Creator Username</th>
					<th>Status</th>
					<th>Delete</th>
				</tr>
			</thead>
			<tbody>
				{secretCredentials.map(({secretName, lastUsed, created, creatorUsername, active}) => {
					return (
						<tr key={secretName}>
							<td>{secretName}</td>
							<td>{lastUsed}</td>
							<td>{created}</td>
							<td>{creatorUsername}</td>
							<ActivationTd
								active={active}
								keyName="secretName"
								value={secretName}
								message={`Are you sure you want to deactivate the credential with secretName: \n${secretName}? It can be activated again later.`}
								setCredentialActivation={setCredentialActivation}/>
							<td>
								<a onClick={() => deleteSecretCredential(secretName)} title="Delete secret credential">
									<i className="fas fa-trash"></i>
								</a>
							</td>
						</tr>
					)
				})}
			</tbody>
		</table> : ''}
		<SubmitButton
			className="form-control-static btn"
			onClick={addSecretCredential}
			value="Add secret credential"
		/>
	</div>
)

const ActivationTd = ({active, keyName, value, message, setCredentialActivation}) => {
	message = active ? message : null
	return <td>
		{active ? <span style={{color: 'green'}}>Active</span> : <span style={{color: 'red'}}>Inactive</span>} | <a href='' onClick={() => setCredentialActivation(keyName, value, !active, message)}>{active ? 'Deactivate' : 'Activate'}</a>
	</td>
}

class Accounts extends React.Component{
	constructor(props){
		super(props)
		this.state = {accountDetails: []}
	}

	componentWillMount(){
		Fetch.get('list').then(accountDetails => this.setState({accountDetails}))
	}

	addAccount = (accountName, callerType) => {
		Fetch.post('add', {accountName, callerType}).then(newAccountDetail => {
			const newAccountDetails = [...this.state.accountDetails, newAccountDetail]
			newAccountDetails.sort(({account: accountA}, {account: accountB}) =>
				accountA.accountName < accountB.accountName ? -1 : 1)
			this.setState({accountDetails: newAccountDetails})
		})
	}

	deleteAccount = (accountName) => {
		if(confirm(`Are you sure you want to delete the account named: \n${accountName}? This will also delete ALL of its credentials.`)){
			Fetch.post('delete', {accountName}, false).then(() => {
				const accountPredicate = ({account}) => account.accountName != accountName
				this.setState({accountDetails: this.state.accountDetails.filter(accountPredicate)})
			})
		}
	}

	render(){
		return (
			<div>
				<AccountTable
					accountDetails={this.state.accountDetails}
					deleteAccount={this.deleteAccount}
				/>
				{Hr}
				<AddAccountForm addAccount={this.addAccount} />
				{Hr}
				<AccountLookupForm />
			</div>
		)
	}
}

const AccountDetailsBreakdown = ({
	toggleUserMappings,
	account,
	error
}) => (
	<div>
		{error ? <strong style={{color: 'red'}}>{error}</strong> : ''}
		<dl>
			<dt>Name</dt>
			<dd>{account.accountName}</dd>
			<dt>Created</dt>
			<dd>{account.created}</dd>
			<dt>Creator</dt>
			<dd>{account.creator}</dd>
			<dt>Last used</dt>
			<dd>{account.lastUsed || '-'}</dd>
			<dt>User Mapping Enabled</dt>
			<dd>
				<code style={{marginRight: '1em'}}>{account.enableUserMappings ? "true" : "false"}</code>
				<SubmitButton compact onClick={toggleUserMappings} value="Toggle" />
			</dd>
		</dl>
	</div>
)

const Hr = <hr className={`mt-4 mb-3`} />

class AccountDetails extends React.Component{
	constructor(props){
		super(props)
		this.state = {
			details: null,
			availableEndpoints: [],
			selectedEndpoint: null,
			keypair: null
		}
	}

	componentWillMount(){
		Fetch.get('getDetails?accountName=' + this.props.params.accountName)
			.then(details => this.setState({details}))

		Fetch.get('getAvailableEndpoints')
			.then(availableEndpoints => this.setState({availableEndpoints, selectedEndpoint: availableEndpoints[0]}))
	}

	updateAccount(endpoint){
		Fetch.post(endpoint, {accountName: this.state.details.account.accountName})
			.then(details => {
				if(details.error){
					this.setState(prevState => {return {details: {...prevState.details, error: details.error}}})
				}else{
					this.setState({details})
				}
			})
	}

	toggleUserMappings = () => {
		this.updateAccount('toggleUserMappings')
	}

	handleSelectEndpoint = (event) => {
		this.setState({selectedEndpoint: event.target.value})
	}

	handleAddPermission = (event) => {
		event.preventDefault()
		const accountName = this.state.details.account.accountName
		const endpoint = this.state.selectedEndpoint
		Fetch.post('addPermission', {accountName, endpoint})
			.then(details => this.setState({details}))
	}

	deletePermission = (endpoint) => {
		const accountName = this.state.details.account.accountName
		Fetch.post('deletePermission', {accountName, endpoint})
			.then(details => this.setState({details}))
	}

	addCredential = () => {
		event.preventDefault()
		const accountName = this.state.details.account.accountName
		Fetch.post('addCredential', {accountName})
			.then(details => this.setState({details}))
	}

	deleteCredential = (apiKey) => {
		if(confirm(`Are you sure you want to delete the credential with API key: \n${apiKey}?`)){
			const accountName = this.state.details.account.accountName
			Fetch.post('deleteCredential', {apiKey, accountName})
				.then(details => this.setState({details}))
		}
	}

	setCredentialActivation = (key, value, active, message) => {
		event.preventDefault()
		const accountName = this.state.details.account.accountName
		if(!message || message && confirm(message)){
			Fetch.postJson('setCredentialActivation', {accountName, active, [key]: value})
				.then(details => this.setState({details}))
		}
	}

	addSecretCredential = () => {
		event.preventDefault()
		const accountName = this.state.details.account.accountName
		Fetch.post('addSecretCredential', {accountName})
			.then(json => {
				this.setState({details: json.details, keypair: json.keypair})
			})
	}

	deleteSecretCredential = (secretName) => {
		if(confirm(`Are you sure you want to delete the secret credential with name: \n${secretName}?`)){
			const accountName = this.state.details.account.accountName
			Fetch.post('deleteSecretCredential', {secretName, accountName})
				.then(details => this.setState({details}))
		}
	}

	clearKeypair = () => {
		event.preventDefault()
		this.setState({keypair: null})
	}

	render(){
		if(this.state.keypair){
			const {apiKey, secretKey} = this.state.keypair
			return (
				<div>
					<h3>This is the new keypair. This is the <strong style={{color: 'red'}}>only time</strong> it will be displayed.</h3>
					<dl>
						<dt>API Key</dt>
						<dd>{apiKey}</dd>
						<dt>Secret Key</dt>
						<dd>{secretKey}</dd>
					</dl>
					<SubmitButton
						className="form-control-static btn"
						onClick={this.clearKeypair}
						value="Back"/>
				</div>
			)
		}

		const backButton = <Link className="btn btn-primary" to={REACT_BASE_PATH + "manage"}>Back</Link>
		if (!this.state.details)
			return backButton

		const {
			details,
			details: {permissions, credentials, secretCredentials},
			selectedEndpoint,
			availableEndpoints
		} = this.state

		return (
			<div>
				<AccountDetailsBreakdown
					toggleUserMappings={this.toggleUserMappings}
					account={details.account}
					error={details.error}
				/>
				{Hr}
				<CredentialTable
					credentials={credentials}
					addCredential={this.addCredential}
					deleteCredential={this.deleteCredential}
					setCredentialActivation={this.setCredentialActivation}/>
				{Hr}
				<SecretCredentialTable
					secretCredentials={secretCredentials}
					addSecretCredential={this.addSecretCredential}
					deleteSecretCredential={this.deleteSecretCredential}
					setCredentialActivation={this.setCredentialActivation}/>
				{Hr}
				{!!availableEndpoints.length &&
					<div>
						<h3>Permissions {!permissions.length && <span className="alert alert-warning">(This account has no permissions)</span>}</h3>
						<form>
							<div className="input-group">
								<div class="input-group-prepend">
									<span class="input-group-text" id="basic-addon1">Endpoints</span>
								</div>
								<div className="input-group">
									<select className="form-control" value={selectedEndpoint || ""} onChange={this.handleSelectEndpoint}>
										{availableEndpoints.map(endpoint =>
											<option key={endpoint} value={endpoint}>
												{endpoint}
											</option>
										)}
									</select>
								</div>
								<div className="input-group-append">
									<SubmitButton
										className="form-control-static btn"
										onClick={this.handleAddPermission}
										value="Add permission"
									/>
								</div>
							</div>
						</form>
						{permissions.length == 0 ||
							<table className="table table-sm">
								<thead>
									<tr>
										<th scope="col">Endpoint</th>
										<th />
									</tr>
								</thead>
								<tbody>
									{permissions.map(({endpoint}) =>
										<tr key={endpoint}>
											<td>{endpoint == "all" ? "All endpoints" : endpoint}</td>
											<td className="text-right">
												<span className="fa fa-trash" onClick={() => this.deletePermission(endpoint)} />
											</td>
										</tr>
									)}
								</tbody>
							</table>
						}
					</div>
				}
				{Hr}
				{backButton}
			</div>
		)
	}
}

ReactDOM.render(
	<div className="container-fluid">
		<h1>DatarouterAccounts</h1>
		<Router history={browserHistory}>
			<Route path={REACT_BASE_PATH + "manage"} component={Accounts} />
			<Route path={REACT_BASE_PATH + "details/:accountName"} component={AccountDetails} />
			<Redirect from="*" to={REACT_BASE_PATH + "manage"} />
		</Router>
	</div>,
	document.getElementById('app')
)
