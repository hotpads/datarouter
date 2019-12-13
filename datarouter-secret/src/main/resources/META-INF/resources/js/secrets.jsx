const FETCH_OPTIONS = {
	credentials: 'same-origin',
	method: 'POST',
	headers: {
		'Content-Type': 'application/json'
	}
}

const PATH = 'handle'

class Secrets extends React.Component{
	constructor(props){
		super(props)

		this.state = {
			names: [],
			secrets: {},
			sharedNames: [],
			sharedSecrets: {},
			form: {name: '', value: '', secretClass: ''},
			filter: "",
			filteredNames: [],
			filteredSharedNames: [],
			tableMessage: 'Loading...',
			errorMessages: []
		}

		this.loadSecrets = this.loadSecrets.bind(this)
		this.handleForm = this.handleForm.bind(this)
		this.createSecret = this.createSecret.bind(this)
		this.readSecretValue = this.readSecretValue.bind(this)
		this.readSharedSecretValue = this.readSharedSecretValue.bind(this)
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
				sharedSecrets: sharedNames.reduce((acc, name) => { return {...acc, [name]: {name: name, value: null}}}, {}),
				tableMessage: names.length === 0 ? 'No secrets found.' : ''})
		}, (json) => {
			this.setState({tableMessage: 'Failed to load secrets. Try reloading the page.'})
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

	readSharedSecretValue(name){
		this.doFetch({op: 'READ_SHARED', name: name}, (json) => {
			this.setState((state) => this.updateSharedSecretFields(state, name, {'value': json.value}))
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

	updateSharedSecretFields(state, name, newFields){
		return {
			sharedSecrets: {
				...state.sharedSecrets,
				[name]: {
					...state.sharedSecrets[name],
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
		fetch(PATH, {...FETCH_OPTIONS, body: JSON.stringify(body)})
				.then(response => response.json())
				.then(json => {
					//console.log('JSON', json)
					if(json && json.opStatus === 'SUCCESS'){
						onSuccess && onSuccess(json)
					}else{
						//console.log('PROBLEM', 'opStatus', json.opStatus, 'message', json.message)
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
			<div>
				<h2 style={{color: "red"}}>Value inputs are JSON. Please use quotes around plain strings, e.g. "example".</h2>
				<ErrorMessages errorMessages={this.state.errorMessages} />
				<CreateForm form={this.state.form}
						handleForm={this.handleForm}
						createSecret={this.createSecret} />
				<h2>Secrets Filter</h2>
				<Filters filter={this.state.filter}
						handleFilter={this.handleFilter} />
				<h2>Secrets List</h2>
				<SecretsList tableMessage={this.state.tableMessage}
						names={this.state.filteredNames}
						secrets={this.state.secrets}
						readSecretValue={this.readSecretValue}
						updateSecretValue={this.updateSecretValue}
						deleteSecret={this.deleteSecret}
						handleNewValue={this.handleNewValue}
						readOnly={false} />
				<h2>Shared Secrets List</h2>
				<SecretsList tableMessage={this.state.tableMessage}
						names={this.state.filteredSharedNames}
						secrets={this.state.sharedSecrets}
						readSecretValue={this.readSharedSecretValue}
						updateSecretValue={null}
						deleteSecret={null}
						handleNewValue={null}
						readOnly={true} />
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
		<table className="table table-condensed">
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
		</table>
		{props.tableMessage.length !== 0 ? <h3>{props.tableMessage}</h3> : ''}
	</div>

ReactDOM.render(
	<div className="container">
		<Secrets />
	</div>,
	document.getElementById('app')
)