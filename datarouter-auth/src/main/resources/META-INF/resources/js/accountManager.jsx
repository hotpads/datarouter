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
		this.state = {accountName: ''}
	}

	handleChange = (event) => {
		this.setState({accountName: event.target.value})
	}

	submitForm = (event) => {
		event.preventDefault()
		this.props.addAccount(this.state.accountName)
	}

	render(){
		return (
			<form>
				<div className="form-group">
					<label>Account Name</label>
					<div className="input-group">
						<input type="text" className="form-control" value={this.state.accountName} onChange={this.handleChange} />
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

const AccountTable = ({accountDetails, deleteAccount}) => (
	<table className="sortable table table-condensed table-striped">
		<thead>
			<tr>
				<th>Account name</th>
				<th>API key</th>
				<th>Secret key</th>
				<th>Last used</th>
				<th>Permissions</th>
				<th>User Mappings</th>
				<th/>
				<th/>
			</tr>
		</thead>
		<tbody>
			{accountDetails.map(({account, permissions}) => (
				<tr key={account.key.accountName}>
					<td>{account.key.accountName}</td>
					<td>{account.apiKey}</td>
					<td>{account.secretKey}</td>
					<td>{account.lastUsed}</td>
					<td className={permissions.length > 0 ? '' : 'table-warning'}>
						{permissions.length > 0 ? (
							<span
								data-toggle="tooltip"
								title={`Permissions for ${account.key.accountName}:\n`
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
					<td>
						<Link to={REACT_BASE_PATH + "details/" + account.key.accountName}>
							<i class="fa fa-edit"></i>
						</Link>
					</td>
					<td>
						<a onClick={() => deleteAccount(account.key.accountName)}>
							<i className="fas fa-trash"></i>
						</a>
					</td>
				</tr>
			))}
		</tbody>
	</table>
)

class Accounts extends React.Component{
	constructor(props){
		super(props)
		this.state = {accountDetails: []}
	}

	componentWillMount(){
		Fetch.get('list').then(accountDetails => this.setState({accountDetails}))
	}

	addAccount = (accountName) => {
		Fetch.post('add', {accountName}).then(newAccountDetail => {
			const newAccountDetails = [...this.state.accountDetails, newAccountDetail]
			newAccountDetails.sort(({account: accountA}, {account: accountB}) =>
				accountA.key.accountName < accountB.key.accountName ? -1 : 1)
			this.setState({accountDetails: newAccountDetails})
		})
	}

	deleteAccount = (accountName) => {
		if(confirm(`Are you sure you want to delete the account named: \n${accountName}?`)){
			Fetch.post('delete', {accountName}, false).then(() => {
				const accountPredicate = ({account}) => account.key.accountName != accountName
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
					resetApiKey={this.resetApiKey}
					resetSecretKey={this.resetSecretKey}
				/>
				<AddAccountForm addAccount={this.addAccount} />
			</div>
		)
	}
}

const AccountDetailsBreakdown = ({
	generateApiKey,
	resetApiKeyToDefault,
	generateSecretKey,
	resetSecretKeyToDefault,
	toggleUserMappings,
	isServerTypeDev,
	account
}) => (
	<dl>
		<dt>Name</dt>
		<dd>{account.key.accountName}</dd>
		<dt>API key</dt>
		<dd>
			<code style={{marginRight: '1em'}}>{account.apiKey}</code>
			<SubmitButton compact onClick={generateApiKey} value="Generate Key" />
			{!!isServerTypeDev && <SubmitButton compact onClick={resetApiKeyToDefault} value="Reset to Default" />}
		</dd>
		<dt>Secret key</dt>
		<dd>
			<code style={{marginRight: '1em'}}>{account.secretKey}</code>
			<SubmitButton compact onClick={generateSecretKey} value="Generate Key" />
			{!!isServerTypeDev && <SubmitButton compact onClick={resetSecretKeyToDefault} value="Reset to Default" />}
		</dd>
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
)

class AccountDetails extends React.Component{
	constructor(props){
		super(props);
		this.state = {
			details: null,
			availableEndpoints: [],
			selectedEndpoint: null,
			isServerTypeDev:false
		}
	}

	componentWillMount(){
		Fetch.get('getDetails?accountName=' + this.props.params.accountName)
			.then(details => this.setState({details}))

		Fetch.get('getAvailableEndpoints')
			.then(availableEndpoints => this.setState({availableEndpoints, selectedEndpoint: availableEndpoints[0]}))

		Fetch.get('isServerTypeDev')
			.then(isServerTypeDev => this.setState({isServerTypeDev}))
	}

	updateAccount(endpoint){
		Fetch.post(endpoint, {accountName: this.state.details.account.key.accountName})
			.then(details => this.setState({details}))
	}

	resetApiKeyToDefault = () => {
		this.updateAccount('resetApiKeyToDefault');
	}

	resetSecretKeyToDefault = () => {
		this.updateAccount('resetSecretKeyToDefault');
	}

	generateApiKey = () => {
		this.updateAccount('generateApiKey');
	}

	generateSecretKey = () => {
		this.updateAccount('generateSecretKey');
	}

	toggleUserMappings = () => {
		this.updateAccount('toggleUserMappings');
	}

	handleSelectEndpoint = (event) => {
		this.setState({selectedEndpoint: event.target.value});
	}

	handleAddPermission = (event) => {
		event.preventDefault();
		const accountName = this.state.details.account.key.accountName
		const endpoint = this.state.selectedEndpoint
		Fetch.post('addPermission', {accountName, endpoint})
			.then(details => this.setState({details}))
	}

	deletePermission = (endpoint) => {
		const accountName = this.state.details.account.key.accountName
		Fetch.post('deletePermission', {accountName, endpoint})
			.then(details => this.setState({details}))
	}

	render(){
		const backButton = <Link className="btn btn-default" to={REACT_BASE_PATH + "manage"}>Back</Link>

		if (!this.state.details)
			return backButton

		const {
			details,
			details: {permissions},
			selectedEndpoint,
			isServerTypeDev,
			availableEndpoints
		} = this.state

		return (
			<div>
				<AccountDetailsBreakdown
					generateApiKey={this.generateApiKey}
					resetApiKeyToDefault={this.resetApiKeyToDefault}
					generateSecretKey={this.generateSecretKey}
					resetSecretKeyToDefault={this.resetSecretKeyToDefault}
					toggleUserMappings={this.toggleUserMappings}
					isServerTypeDev={isServerTypeDev}
					account={details.account}
				/>
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
				<Link className="btn btn-primary" to={REACT_BASE_PATH + "manage"}>Back</Link>
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
