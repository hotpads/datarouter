const FETCH_OPTIONS = {
	credentials: 'same-origin',
	method: 'POST',
	headers: {
		'Content-Type': 'application/json'
	}
}

const WRITE_OPS = ['CREATE','UPDATE','DELETE']

class SecretClientSupplierConfigSecrets extends React.Component{
	constructor(props){
		super(props)

		this.config = props.config
		this.isReadOnly = !WRITE_OPS.some(element => this.config.allowedOps[element])
		this.cardHeader = "name=" + this.config.configName + " supplierClass=" + this.config.supplierClass
				+ (this.isReadOnly ? ' (read-only)' : ' (read/write)')

		this.state = {
			names: [],
			secrets: {},
			sharedNames: [],
			form: {name: '', value: '', secretClass: ''},
			filter: "",
			filteredNames: [],
			filteredSharedNames: [],
			tableMessage: 'Loading...',
			sharedTableMessage: 'Loading...',
			errorMessages: []
		}

		this.loadSecrets = this.loadSecrets.bind(this)
		this.handleForm = this.handleForm.bind(this)
		this.createSecret = this.createSecret.bind(this)
		this.readSecretValue = this.readSecretValue.bind(this)
		this.handleNewValue = this.handleNewValue.bind(this)
		this.updateSecretValue = this.updateSecretValue.bind(this)
		this.deleteSecret = this.deleteSecret.bind(this)
		this.handleFilter = this.handleFilter.bind(this)
		this.addErrorMessage = this.addErrorMessage.bind(this)
		this.expireErrorMessage = this.expireErrorMessage.bind(this)
	}

	loadSecrets(){
		this.doFetch({op: 'LIST_ALL'}, (json) => {
			const names = this.sort(json.appSecretNames)
			const sharedNames = this.sort(json.sharedSecretNames)
			this.setState({
				names: names,
				sharedNames: sharedNames,
				filteredNames: names,
				filteredSharedNames: sharedNames,
				secrets: names.reduce((acc, name) => { return {...acc, [name]: {name: name, value: null, newValue: ''}}}, {}),
				tableMessage: names.length === 0 ? 'None' : '',
				sharedTableMessage: sharedNames.length === 0 ? 'None' : ''})
		}, (json) => {
			const message = 'Failed to load secrets. Try reloading the page.'
			this.setState({tableMessage: message, sharedTableMessage: message})
		})
	}

	handleForm(event){
		const {name, value} = event.target
		this.setState((state) => {
			return {form: {...state.form, [name]: value}}
		})
	}

	createSecret(event, form){
		event.preventDefault()
		const {name, value, secretClass} = form
		this.doFetch({op: 'CREATE', name: name, value: value, secretClass: secretClass || 'java.lang.String'}, (json) => {
			this.setState((state) => {
				const names = this.sort([...state.names, name])
				const secrets = {...state.secrets, [name]: {name: name, value: value, newValue: ''}}
				return {
					names: names,
					secrets: secrets,
					form: {name: '', value: '', secretClass: ''},
					filteredNames: this.applyFilters(names, state.filter),
				}
			})
		})
	}

	readSecretValue(name){
		this.doFetch({op: 'READ', name: name}, (json) => {
			this.setState((state) => this.updateSecretFields(state, name, {'value': json.value}))
		})
	}

	handleNewValue(event, name){
		const value = event.target.value
		this.setState((state) => this.updateSecretFields(state, name, {'newValue': value}))
	}

	updateSecretValue(name, value){
		const message = `Are you sure you want to update secret '${name}' to '${value}'? If the value is improperly formatted, it will not be readable outside of this page.`
		window.confirm(message) && this.doFetch({op: 'UPDATE', name: name, value: value}, (json) => {
			this.setState((state) => this.updateSecretFields(state, name, {'value': value, 'newValue': ''}))
		})
	}

	deleteSecret(target){
		const message = `Are you sure you want to delete secret '${target}'?`
		window.confirm(message) && this.doFetch({op: 'DELETE', name: target}, (json) => {
			this.setState((state) => {
				const newNames = state.names.filter(name => name !== target)
				const newFilteredNames = state.filteredNames.filter(name => name !== target)
				const newSecrets = newFilteredNames.reduce((acc, name) => {
					return {
						...acc,
						[name]: state.secrets[name]
					}
				}, {})
				return {
					...state,
					names: newNames,
					filteredNames: newFilteredNames,
					secrets: newSecrets
				}
			})
		})
	}

	updateSecretFields(state, name, newFields){
		return {
			secrets: {
				...state.secrets,
				[name]: {
					...state.secrets[name],
					...newFields
				}
			}
		}
	}

	handleFilter(event){
		const newValue = event.target.value
		this.setState((state, props) => {
			const newFilteredNames = this.applyFilters(state.names, newValue)
			const newFilteredSharedNames = this.applyFilters(state.sharedNames, newValue)
			return {
				filter: newValue,
				filteredNames: newFilteredNames,
				filteredSharedNames: newFilteredSharedNames
			}
		})
	}

	doFetch(body, onSuccess, onError){
		body = {...body, configName: this.config.configName}
		fetch(PATH_HANDLE, {...FETCH_OPTIONS, body: JSON.stringify(body)})
				.then(response => response.json())
				.then(json => {
					if(json && json.opStatus === 'SUCCESS'){
						onSuccess && onSuccess(json)
					}else{
						console.log('PROBLEM', 'opStatus', json.opStatus, 'message', json.message)
						this.addErrorMessage('ERROR: ' + json.message)
						onError && onError(json)
					}
				}).catch(error => {
					this.addErrorMessage('Network error. Try again.')
				})
	}

	addErrorMessage(message){
		this.setState(state => {
			setTimeout(this.expireErrorMessage, 5000)
			return {errorMessages: [...state.errorMessages, message]}
		})
	}

	expireErrorMessage(){
		this.setState(state => {return{errorMessages: state.errorMessages.slice(1)}})
	}

	sort(arr){
		return arr.sort((a,b) => a.toLowerCase().localeCompare(b.toLowerCase()))
	}

	applyFilters(names, filter){
		return names.filter(name => {
			if(filter.length > 0 && name.toLowerCase().indexOf(filter.toLowerCase()) < 0){
				return false
			}
			return true
		})
	}

	componentDidMount(){
		this.loadSecrets()
	}

	render(){
		return(
			<div class="card mb-3">
				<div class="card-header">{this.cardHeader}</div>
				<div class="card-body">
					<ErrorMessages errorMessages={this.state.errorMessages} />
					{this.isReadOnly ? '' : (<div>
						<CreateForm form={this.state.form}
								handleForm={this.handleForm}
								createSecret={this.createSecret} />
					</div>)}
					<Filters filter={this.state.filter}
							handleFilter={this.handleFilter} />
					<SecretsList tableMessage={this.state.tableMessage}
							names={this.state.filteredNames}
							secrets={this.state.secrets}
							readSecretValue={this.readSecretValue}
							updateSecretValue={this.updateSecretValue}
							deleteSecret={this.deleteSecret}
							handleNewValue={this.handleNewValue}
							readOnly={this.isReadOnly} />
					<SharedSecretNameList tableMessage={this.state.sharedTableMessage}
							names={this.state.filteredSharedNames} />
				</div>
			</div>
		)
	}
}

const ErrorMessages = props =>
	{
		return props.errorMessages.map(message =>
				<div class="alert alert-danger" role="alert">
					{message}
				</div>)
	}

const CreateForm = props =>
	<div>
		<h2>Create Secret</h2>
		<h2 style={{color: "red"}}>Value inputs are JSON. Please use quotes around plain strings, e.g. "example".</h2>
		<form class="form-inline" onSubmit={(e) => props.createSecret(e, props.form)}>
			<label class="mb-3 mr-sm-2" for="name">Name:</label>
			<input class="form-control mb-3 mr-sm-2" type="text" name="name" value={props.form.name} onChange={props.handleForm}/>
			<label class="mb-3 mr-sm-2" for="value">Value:</label>
			<input class="form-control mb-3 mr-sm-2" type="text" autocomplete="off" name="value" value={props.form.value} onChange={props.handleForm}/>
			<label class="mb-3 mr-sm-2" for="class">Class:</label>
			<input class="form-control mb-3 mr-sm-2" type="text" name="secretClass" placeholder="default: java.lang.String" value={props.form.secretClass} onChange={props.handleForm}/>
			<button class="btn btn-primary mb-3" type="submit">Create</button>
		</form>
	</div>

const Filters = props =>
	<div class="input-group mb-3">
		<div class="input-group-prepend">
			<span class="input-group-text">Secret name filter:</span>
		</div>
		<input type="text" class="form-control" name="filter" value={props.filter} onChange={props.handleFilter}/>
	</div>

const SecretsList = props =>
	<div>
		<h2>Secrets</h2>
		{props.names.length ?
		(<table className="table table-condensed">
			<thead>
				<tr>
					<th>Name</th>
					<th>Value</th>
					{!props.readOnly && <th>New Value</th>}
					{!props.readOnly && <th>Update Value</th>}
					{!props.readOnly && <th>Delete</th>}
				</tr>
			</thead>
			<tbody>
				{props.names.map(name => {
						const secret = props.secrets[name]
						return (
							<tr>
								<td>{secret.name}</td>
								<td>
									{secret.value == null
										? <button class="btn btn-primary" type="button" onClick={() => props.readSecretValue(secret.name)}>Read</button>
										: secret.value}
								</td>
								{!props.readOnly && <td><textarea cols="50" rows="3" name="newValue" value={secret.newValue} onChange={(event) => props.handleNewValue(event, secret.name)}/></td>}
								{!props.readOnly && <td><button class="btn btn-warning" type="button" onClick={() => props.updateSecretValue(secret.name, secret.newValue)}>Update</button></td>}
								{!props.readOnly && <td><button class="btn btn-danger" type="button" onClick={() => props.deleteSecret(secret.name)}>Delete</button></td>}
							</tr>
						)
					}
				)}
			</tbody>
		</table>) : ''}
		{props.tableMessage.length !== 0 ? <h3>{props.tableMessage}</h3> : ''}
	</div>

const SharedSecretNameList = props =>
	<div>
		<h2>Available Shared Secret Names</h2>
		{props.names.length ?
		(<ul class="list-unstyled">
			{props.names.map(name => <li>{name}</li>)}
		</ul>) : ''}
		{props.tableMessage.length !== 0 ? <h3>{props.tableMessage}</h3> : ''}
	</div>

class Secrets extends React.Component{

	constructor(props){
		super(props)

		this.state = {
			configNames: [],
			configs: {},
			pageMessage: 'Loading...',
		}

		this.loadConfigs = this.loadConfigs.bind(this)
	}

	loadConfigs(onSuccess, onError){
		fetch(PATH_CONFIG, {...FETCH_OPTIONS})
				.then(response => response.json())
				.then(json => {
					if(json && json.configNames){
						this.setState({
							configNames: json.configNames,
							configs: json.configs,
							pageMessage: json.configNames.length === 0 ? 'No configs found.' : ''
						})
					}else{
						console.log('failure', json)
						this.setState({pageMessage: 'Failed to load secret client supplier configs. Try refreshing.'})
					}
				}).catch(error => {
					console.log('error', error)
					this.setState({pageMessage: 'Failed to load secret client supplier configs. Try refreshing.'})
				})
	}

	componentDidMount(){
		this.loadConfigs()
	}

	render(){
		return(
			<div className="container-fluid">
				<h1>Secrets by SecretClientSupplierConfig</h1>
				{this.state.pageMessage ? (<h3>{this.state.pageMessage}</h3>) : ''}
				{this.state.configNames.map(configName => <SecretClientSupplierConfigSecrets config={this.state.configs[configName]} key={configName} />)}
			</div>
		)
	}

}

ReactDOM.render(<Secrets />, document.getElementById('app'))
