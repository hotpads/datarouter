const { Router, Redirect, Route, IndexRoute, Link, browserHistory } = ReactRouter;

const BASE_PATH = CONTEXT_PATH + "/admin/accounts/";

const postHeaders = {
	'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
};

const compareAccounts = (accountA, accountB) => accountA.key.accountName < accountB.key.accountName ? -1 : 1;

const buttonStyle = {'margin':'0 5px'};

class AddAccountForm extends React.Component{
	constructor(props){
		super(props);
		this.state = {
			accountName: ''
		};
		this.handleChange = this.handleChange.bind(this);
		this.submitForm = this.submitForm.bind(this);
	}

	handleChange(event){
		this.setState({accountName: event.target.value});
	}

	submitForm(event){
		event.preventDefault();
		this.props.addAccount(this.state.accountName);
	}
	
	render(){
		return (
				<form>
					<div className="form-group">
						<label>Account Name</label>
						<input type="text"
								className="form-control"
								value={this.state.accountName}
								onChange={this.handleChange} />
					</div> 
					<input type="submit"
							className="btn btn-default"
							onClick={this.submitForm}
							disabled={this.state.accountName == ''}
							value="Create account" />
				</form>
		);
	}
}
class AccountList extends React.Component{
	render(){
		return (
				<table className="table table-condensed">
					<thead>
						<tr>
							<th>Account name</th>
							<th>API key</th>
							<th>Secret key</th>
							<th>Last used</th>
							<th></th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						{this.props.accounts.map(account =>
							<tr key={account.key.accountName}>
								<td>{account.key.accountName}</td>
								<td>{account.apiKey}</td>
								<td>{account.secretKey}</td>
								<td>{account.lastUsed}</td>
								<td>
									<Link to={BASE_PATH + "details/" + account.key.accountName}>
										<span className="glyphicon glyphicon-pencil"></span>
									</Link>
								</td>
								<td>
									<a onClick={() => this.props.deleteAccount(account.key.accountName)}>
										<span className="glyphicon glyphicon-remove"></span>
									</a>
								</td>
							</tr>
						)}
					</tbody>
				</table>
		);
	}
}
class Accounts extends React.Component{
	constructor(props){
		super(props);
		this.state = {
			accounts: []
		};
		this.addAccount = this.addAccount.bind(this);
		this.deleteAccount = this.deleteAccount.bind(this);
	}

	componentWillMount(){
		fetch('list', {
			credentials: 'same-origin'
		}).then((response) => response.json())
		.then((accounts) => this.setState({accounts}));
	}

	addAccount(accountName){
		fetch('add', {
			credentials: 'same-origin',
			method: 'POST',
			headers: postHeaders,
			body: $.param({accountName: accountName})
		}).then((response) => response.json())
		.then((account) => {
			var accounts = this.state.accounts.concat([account]);
			accounts.sort(compareAccounts);
			this.setState({accounts});
		});
	}

	deleteAccount(accountName){
		fetch('delete', {
			credentials: 'same-origin',
			method: 'POST',
			headers: postHeaders,
			body: $.param({accountName: accountName})
		}).then(() => {
			var accounts = this.state.accounts.filter((account) => account.key.accountName != accountName);
			this.setState({accounts});
		});
	}

	render(){
		return ( 
				<div>
					<AddAccountForm addAccount={this.addAccount} />
					<AccountList 
							accounts={this.state.accounts}
							deleteAccount={this.deleteAccount}
							resetApiKey={this.resetApiKey}
							resetSecretKey={this.resetSecretKey}
					/>
				</div>
		);
	}
}
class AccountDetails extends React.Component{
	constructor(props){
		super(props);
		this.state = {
			details: null,
			availableEndpoints: [],
			selectedEndpoint: null,
			isServerTypeDev:false
		};
		this.handleSelectEndpoint = this.handleSelectEndpoint.bind(this);
		this.handleAddPermission = this.handleAddPermission.bind(this);
		this.deletePermission = this.deletePermission.bind(this);
		this.resetApiKeyToDefault = this.resetApiKeyToDefault.bind(this);
		this.resetSecretKeyToDefault = this.resetSecretKeyToDefault.bind(this);
		this.generateApiKey = this.generateApiKey.bind(this);
		this.generateSecretKey = this.generateSecretKey.bind(this);
	}
	
	componentWillMount(){
		fetch('getDetails?accountName=' + this.props.params.accountName, {
			credentials: 'same-origin',
			method: 'GET',
		}).then(response => response.json())
		.then(details => {
			this.setState({details});
		});
		
		fetch('getAvailableEndpoints', {
			credentials: 'same-origin',
			method: 'GET',
		}).then(response => response.json())
		.then(availableEndpoints => {
			this.setState({availableEndpoints, selectedEndpoint: availableEndpoints[0]});
		});

		fetch('isServerTypeDev', {
			credentials: 'same-origin',
			method: 'GET',
		}).then(response => response.json())
		.then(isServerTypeDev => {
			this.setState({isServerTypeDev});
		});
	}

	resetApiKeyToDefault(){
		this.updateAccount('resetApiKeyToDefault');
	}

	resetSecretKeyToDefault(){
		this.updateAccount('resetSecretKeyToDefault');
	}

	generateApiKey(){
		this.updateAccount('generateApiKey');
	}

	generateSecretKey(){
		this.updateAccount('generateSecretKey');
	}

	updateAccount(endpoint){
		fetch(endpoint, {
			credentials: 'same-origin',
			method: 'POST',
			headers: postHeaders,
			body: $.param({accountName: this.state.details.account.key.accountName})
		}).then(response => response.json())
		.then(details => {
			this.setState({details});
		});
	}
	
	handleSelectEndpoint(event){
		console.log(event.target.value);
		this.setState({selectedEndpoint: event.target.value});
	}
	
	handleAddPermission(event){
		event.preventDefault();
		fetch('addPermission', {
			credentials: 'same-origin',
			method: 'POST',
			headers: postHeaders,
			body: $.param({
				accountName: this.state.details.account.key.accountName,
				endpoint: this.state.selectedEndpoint,
			})
		}).then(response => response.json())
		.then(details => {
			this.setState({details});
		});
	}
	
	deletePermission(endpoint){
		fetch('deletePermission', {
			credentials: 'same-origin',
			method: 'POST',
			headers: postHeaders,
			body: $.param({
				accountName: this.state.details.account.key.accountName,
				endpoint: endpoint
			})
		}).then(response => response.json())
		.then(details => {
			this.setState({details});
		});
	}
	
	render(){
		return (
			<div>
				{this.state.details == null ||
					<div style={{marginBottom: "10px"}}>
						<dl>
							<dt>Name</dt>
							<dd>{this.state.details.account.key.accountName}</dd>
							<dt>API key</dt>
							<dd>
								{this.state.details.account.apiKey}
								<input type="submit"
										style={buttonStyle}
										className="btn btn-default btn-xs"
										onClick={this.generateApiKey}
										value="Generate Key" />
								{this.state.isServerTypeDev == true &&
										<input type="submit"
											style={buttonStyle}
											className="btn btn-default btn-xs"
											onClick={this.resetApiKeyToDefault}
											value="Reset to Default" />
								}
							</dd>
							<dt>Secret key</dt>
							<dd>
								{this.state.details.account.secretKey}
								<input type="submit"
										style={buttonStyle}
										className="btn btn-default btn-xs"
										onClick={this.generateSecretKey}
										value="Generate Key" />
								{this.state.isServerTypeDev == true &&
									<input type="submit"
										style={buttonStyle}
										className="btn btn-default btn-xs"
										onClick={this.resetSecretKeyToDefault}
										value="Reset to Default" />
								}
							</dd>
							<dt>Created</dt>
							<dd>
							{this.state.details.account.created}
							</dd>
							<dt>Creator</dt>
							<dd>
							{this.state.details.account.creator}
							</dd>
							<dt>Last used</dt>
							<dd>
							{this.state.details.account.lastUsed}
							</dd>
						</dl>
						{this.state.availableEndpoints.length == 0 ||
							<div>
								<h3>Permissions</h3>
								<form>
									<div className="form-group">
										<label>Endpoint</label>
										<select 
												className="form-control" 
												value={this.state.selectedEndpoint == null ? ""
														: this.state.selectedEndpoint}
												onChange={this.handleSelectEndpoint}>
											{this.state.availableEndpoints.map(endpoint => 
												<option key={endpoint} value={endpoint}>
													{endpoint}
												</option>
											)}
										</select>
									</div>
									<input type="submit" 
											className="btn btn-default" 
											onClick={this.handleAddPermission}
											value="Add permission" />
								</form>
								{this.state.details.permissions.length == 0 ||
									<table className="table table-condensed">
										<thead>
											<tr>
												<th>Endpoint</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											{this.state.details.permissions.map(permission => 
												<tr key={permission.endpoint}>
													<td>
														{permission.endpoint == "all"  ? "All endpoints" 
																: permission.endpoint}
													</td>
													<td>
														<span 
																className="glyphicon glyphicon-remove"
																onClick={() => this.deletePermission(
																		permission.endpoint)}
														>
														</span>
													</td>
												</tr>
											)}
										</tbody>
									</table>
								}
							</div>
						}
					</div>
				}
				<Link className="btn btn-default" to={BASE_PATH + "manage"}>Back</Link>
			</div>
		);
	}
}

ReactDOM.render(
	<div className="container">
		<h1>Accounts</h1>
		<Router history={browserHistory}>
	        <Route path={BASE_PATH + "manage"} component={Accounts} />
	        <Route path={BASE_PATH + "details/:accountName"} component={AccountDetails} />
			<Redirect from="*" to={BASE_PATH + "manage"} />
	    </Router>
	</div>,
	document.getElementById('app')
);