const { Fragment, useState, useEffect, useRef, useCallback } = React;

const FETCH_OPTIONS = {
	credentials: "same-origin",
};

const FETCH_POST_OPTIONS = {
	...FETCH_OPTIONS,
	method: "POST",
	headers: { "Content-Type": "application/json" },
};

const ROLE_UPDATE_TYPES = {
	APPROVE: 'APPROVE',
	UNAPPROVE: 'UNAPPROVE',
	REVOKE: 'REVOKE',
};

const PagePermissionType = {
	ADMIN: "ADMIN",
	ROLES_ONLY: "ROLES_ONLY",
	NONE: "NONE",
}


const PAGE_SIZE = 20;

const doFetch = ({
	path,
	extraOptions = {},
	bodyObject,
	onSuccess,
	onError = () => undefined,
	setIsLoading = () => undefined
}) => {
	extraOptions = extraOptions || {};
	const allOptions = bodyObject
		? {
			...FETCH_POST_OPTIONS,
			...extraOptions,
			body: JSON.stringify(bodyObject),
		}
		: { ...FETCH_OPTIONS, ...extraOptions };
	setIsLoading(true);
	fetch(path, allOptions)
		.then((response) => {
			if (!response.ok) {
				throw new Error(`HTTP ${response.status}`);
			}
			try {
				return response.json();
			} catch (e) {
				throw new Error("Fetch request succeeded but JSON parsing failed.");
			}
		})
		.then((json) => {
			try {
				onSuccess && onSuccess(json);
			} catch (e) {
				throw new Error(
					"Fetch request succeeded but response processing failed."
				);
			}
		})
		.catch((error) => {
			onError && onError(error);
		})
		.finally(() => setIsLoading(false));
};

const getTotalNumApprovalsRequiredForRole = (userRoleMetadata) =>
	Object.values(userRoleMetadata.requirementStatusByApprovalType)
		.reduce((accumulator, requirementStatus) => accumulator + requirementStatus.requiredApprovals, 0);


function ViewUsersPage({ display, openEditUser }) {
	const [users, setUsers] = useState([]);
	const [index, setIndex] = useState(0);
	const [filterExpanded, setFilterExpanded] = useState(true);
	const [filteredUsers, setFilteredUsers] = useState([]);

	useEffect(() => {
		doFetch({
			path: PATHS.listUsers,
			onSuccess: (response) => {
				if (response.success) {
					setUsers(response.response);
					setListRefreshTimestamp(Date.now());
				}
				/* TODO error handling */
			},
		});
	}, []);

	const handleFilteredUsersUpdate = (updatedFilteredUsers) => {
		setFilteredUsers(updatedFilteredUsers);
		setIndex(0);
	};

	if (!display) {
		return null;
	}

	return (
		<div style={{ display: "flex" }}>
			<div className="container mb-3 bg-white">
				<div
					id="header-container"
					className="border-bottom my-4"
					style={{ display: "flex", justifyContent: 'space-between' }}
				>
					<h1>Users</h1>
					<div className="m-2" style={{ display: "flex", flexDirection: "column", justifyContent: "end" }}>
						<button
							type="button"
							className="btn btn-outline-info"
							style={{ height: "fit-content" }}
							onClick={() => setFilterExpanded(!filterExpanded)}>
							<i className="fas fa-filter fa-lg py-2 mr-2"/>
							{
								filterExpanded ?
									<i className="fas fa-chevron-up fa-lg"/>
									: <i className="fas fa-chevron-down fa-lg"/>

							}
						</button>
					</div>
				</div>
				<Filters
					filterExpanded={filterExpanded}
					users={users}
					handleFilteredUsersUpdate={handleFilteredUsersUpdate}
				/>
				<UserList
					users={filteredUsers}
					index={index}
					loadStartPage={() => setIndex(0)}
					loadPrevPage={() => setIndex(index < PAGE_SIZE ? 0 : index - PAGE_SIZE)}
					loadNextPage={() => setIndex(index + PAGE_SIZE < filteredUsers.length ? index + PAGE_SIZE : index)}
					openEditUser={openEditUser}
				/>
			</div>
		</div>
	);
}

function Filters({ filterExpanded, users, handleFilteredUsersUpdate }) {
	const [openPermissionRequestsOnly, setOpenPermissionRequestsOnly] = useState(false);
	const [emailFilter, setEmailFilter] = useState("");
	const [hasAnyRoleFilterSet, setHasAnyRoleFilterSet] = useState(new Set());
	const [includeSamlRoles, setIncludeSamlRoles] = useState(false);
	const [showDeprovisionedUsers, setShowDeprovisionedUsers] = useState(false);
	const [allRoles, setAllRoles] = useState([]);
	const [multiSelector, setMultiSelector] = useState(null);
	const ref = useCallback((node) => {
		setMultiSelector(node);
	});

	const updateRoleFilter = () => {
		if (multiSelector) {
			setHasAnyRoleFilterSet(new Set(Array.from(multiSelector.options).filter(option => option.selected).map(option => option.value)));
		}
	};

	const rolesIncludesAnyFilter = (user) => {
		const userRoles = includeSamlRoles ? user.currentRolesWithSaml : user.currentRoles;
		return !hasAnyRoleFilterSet.size
			|| userRoles.some(role => hasAnyRoleFilterSet.has(role));
	};

	const deprovisionedUserFilter = (user) => showDeprovisionedUsers !== user.enabled;

	const handleFilterUpdates = () => {
		handleFilteredUsersUpdate(users.filter((user) =>
			!(openPermissionRequestsOnly && !user.hasPermissionRequest)
			&& !(emailFilter.length > 0 &&
				user.username.toLowerCase().indexOf(emailFilter.toLowerCase()) < 0)
			&& rolesIncludesAnyFilter(user)
			&& deprovisionedUserFilter(user)));
	};

	useEffect(() => {
		if (multiSelector) {
			require(["jquery", "multiple-select"], () => {
				$(multiSelector).multipleSelect({
					placeholder: "Select roles",
					onClick: () => updateRoleFilter(),
					onCheckAll: () => updateRoleFilter(),
					onUncheckAll: () => updateRoleFilter(),
				});
			});
		}
	}, [multiSelector]);

	useEffect(() => {
		doFetch({
			path: PATHS.getAllRoles,
			onSuccess: (response) => {
				if (response.success) {
					setAllRoles(response.response.roles
						.sort((a, b) => a.persistentString.localeCompare(b.persistentString)));
				}
			},
		});
	}, []);

	useEffect(() => {
		handleFilterUpdates();
	}, [users, openPermissionRequestsOnly, emailFilter, hasAnyRoleFilterSet, includeSamlRoles, showDeprovisionedUsers]);

	const handleEmailFilter = (event) => {
		setEmailFilter(event.target.value);
	};

	if (!filterExpanded || !allRoles.length) {
		return null;
	}

	return (
		<div className="d-flex align-items-center justify-content-between">
			<div className="form-group flex-grow-1 mr-2">
				<label htmlFor="emailFilter"> Email Filter: </label>
				<input
					type="text"
					name="emailFilter"
					className="form-control"
					value={emailFilter}
					onChange={handleEmailFilter}
					id="emailFilter"
				/>
			</div>
			<div className="form-group mr-2">
				<label htmlFor="roleFilter" style={{display: "block"}}> Role Filter: (has any)</label>
				<select
					ref={ref}
					className="form-control"
					multiple="multiple"
					style={{ display: "none", width: "350px" }}
				>
					{
						allRoles.map(role =>
							<option
								key={role.persistentString}
								selected={hasAnyRoleFilterSet.has(role.persistentString)}
							>
								{role.persistentString}
							</option>)
					}
				</select>
			</div>
			<div className="d-flex flex-column">
				<div className="form-check">
					<label className="form-check-label">
						<input
							className="form-check-input"
							type="checkbox"
							checked={includeSamlRoles}
							onChange={() => setIncludeSamlRoles(!includeSamlRoles)}
						/>
						Include roles from SAML groups
					</label>
				</div>
				<div className="form-check">
					<label className="form-check-label">
						<input
							className="form-check-input"
							type="checkbox"
							checked={openPermissionRequestsOnly}
							onChange={() => setOpenPermissionRequestsOnly(!openPermissionRequestsOnly)}
						/>
						Has open permission request
					</label>
				</div>
				<div className="form-check">
					<label className="form-check-label">
						<input
							className="form-check-input"
							type="checkbox"
							checked={showDeprovisionedUsers}
							onChange={() => setShowDeprovisionedUsers(!showDeprovisionedUsers)}
						/>
						Show deprovisioned users
					</label>
				</div>
			</div>
		</div>
	);
}

const UserList = ({ users, index, openEditUser, loadPrevPage, loadStartPage, loadNextPage }) => {
	const tableHeaderStyle = { borderTop: "none" };
	return (
	<div>
		<table className="table table-condensed">
			<thead>
			<tr>
				<th style={tableHeaderStyle}>Username</th>
				<th style={tableHeaderStyle}>ID</th>
				<th style={tableHeaderStyle}>Token</th>
				<th style={tableHeaderStyle}>Profile</th>
				<th style={tableHeaderStyle}></th>
				<th style={tableHeaderStyle}></th>
			</tr>
			</thead>
			<tbody>
			{users.slice(index, index + PAGE_SIZE).map((user) => (
				<tr className={user.enabled ? "" : "table-secondary"}>
					<td>{user.username}</td>
					<td>{user.id}</td>
					<td>{user.token}</td>
					<td>
						<a href={user.profileLink} className={user.profileClass}>
							profile
						</a>
					</td>
					<td>
						<Badges
							badges={user.hasPermissionRequest ? ["Permission Request"] : []}
						/>
					</td>
					<td>
						<button
							type="button"
							className="btn btn-primary"
							name={user.username}
							onClick={openEditUser}
						>
							Edit
						</button>
					</td>
				</tr>
			))}
			</tbody>
		</table>
		<nav>
			<ul className="pagination">
				<li>
					<a className="page-link" onClick={loadPrevPage}>
						Previous
					</a>
				</li>
				<li>
					<a className="page-link" onClick={loadStartPage}>
						Start
					</a>
				</li>
				<li>
					<a className="page-link" onClick={loadNextPage}>
						Next
					</a>
				</li>
			</ul>
		</nav>
	</div>
)};

const Badges = ({ badges = [] }) => {
	return (
		<h4>
			{badges.map((content, index) => (
				<span
					className={
						"badge badge-danger" + (index < badges.length - 1 ? " mr-3" : "")
					}
				>
					{content}
				</span>
			))}
		</h4>
	);
};

function EditUserPage({ defaultUsername, closeEditUser }) {
	const [loaded, setLoaded] = useState(false);
	const [error, setError] = useState("");
	const [editorUsername, setEditorUsername] = useState("");
	const [pagePermissionType, setPagePermissionType] = useState(PagePermissionType.NONE);
	const [username, setUsername] = useState(defaultUsername);
	const [id, setId] = useState(null);
	const [token, setToken] = useState(null);
	const [profileLink, setProfileLink] = useState(null);
	const [requests, setRequests] = useState([]);
	const [history, setHistory] = useState([]);
	const [userRoleMetadataList, setUserRoleMetadataList] = useState([]);
	const [availableAccounts, setAvailableAccounts] = useState([]);
	const [currentAccounts, setCurrentAccounts] = useState({}); // accountName -> boolean for currently checked
	const [availableZoneIds, setAvailableZoneIds] = useState([]);
	const [currentZoneId, setCurrentZoneId] = useState(null);
	const [fullName, setFullName] = useState("");
	const [details, setDetails] = useState([]);
	const [hasProfileImage, setHasProfileImage] = useState(false);
	const [isSamlEnabled, setIsSamlEnabled] = useState(false);
	const [deprovisioned, setDeprovisioned] = useState(false);

	const updateUserDetails = (userDetails) => {
		if (!userDetails.success) {
			setLoaded(false);
			setError(userDetails.message);
			return;
		}
		setEditorUsername(userDetails.editorUsername);
		setPagePermissionType(userDetails.pagePermissionType);
		setUsername(userDetails.username);
		setId(userDetails.id);
		setToken(userDetails.token);
		setProfileLink(userDetails.profileLink);
		setRequests(userDetails.requests);
		setHistory(userDetails.history);
		setUserRoleMetadataList(userDetails.userRoleMetadataList.sort((a, b) => a.roleName.localeCompare(b.roleName)));
		setAvailableAccounts(userDetails.availableAccounts);
		setCurrentAccounts(userDetails.currentAccounts);
		setAvailableZoneIds(userDetails.availableZoneIds);
		setCurrentZoneId(userDetails.currentZoneId);
		setFullName(userDetails.fullName);
		setDetails(userDetails.details);
		setHasProfileImage(userDetails.hasProfileImage);
		setDeprovisioned(!userDetails.enabled);
		setLoaded(true);
		setError("");
	};

	const refresh = () => {
		const queryParam = "?username=" + encodeURIComponent(username);
		doFetch({
			path: PATHS.getUserDetails + queryParam,
			onSuccess: (userDetails) => updateUserDetails(userDetails),
			onError: (error) => {
				setError(error);
				setLoaded(false);
			}
		});
	};

	// Call getUserDetails API on initial render if the username is set.
	useEffect(() => username && refresh(), []);

	useEffect(() => {
		if(PagePermissionType.ADMIN !== pagePermissionType){
			return;
		}
		doFetch({
			path: PATHS.getIsSamlEnabled,
			onSuccess: (response) => {
				if (response.success) {
					setIsSamlEnabled(response.response.isSamlEnabled);
				}
			},
		});
	}, [pagePermissionType]);

	const header = (
		<h1 className="mb-4 pt-4 pb-2 border-bottom">
			Edit User{" "}
			<button
				type="button"
				className="btn btn-primary"
				onClick={closeEditUser}
			>
				Back to User List
			</button>
		</h1>
	);

	if (error && !loaded) {
		return (
			<div>
				{header}
				<div className="alert-danger">
					<h3>{"Failed to load user. " + error}</h3>
				</div>
			</div>
		);
	}

	return (
		<div className="container mb-3 bg-white">
			{ loaded && (
				<div>
					{header}
					<UserInformation
						pagePermissionType={pagePermissionType}
						fullName={fullName}
						details={details}
						hasProfileImage={hasProfileImage}
						username={username}
						id={id}
						token={token}
						profileLink={profileLink}
						availableZoneIds={availableZoneIds}
						currentZoneId={currentZoneId}
						updateUserDetails={updateUserDetails}
						setCurrentZoneId={setCurrentZoneId}
						deprovisioned={deprovisioned}
					/>
					{
						[PagePermissionType.ADMIN, PagePermissionType.ROLES_ONLY].includes(pagePermissionType) &&
						<EditRolesCard
							username={username}
							editorUsername={editorUsername}
							userRoleMetadataList={userRoleMetadataList}
							setUserRoleMetadataList={setUserRoleMetadataList}
							updateUserDetails={updateUserDetails}
							deprovisioned={deprovisioned}
							startCollapsed={deprovisioned}
						/>
					}
					{
						PagePermissionType.ADMIN === pagePermissionType && (
							<Fragment>
								<EditAccountsCard
									username={username}
									availableAccounts={availableAccounts}
									currentAccounts={currentAccounts}
									updateUserDetails={updateUserDetails}
									disabled={deprovisioned}
									startCollapsed={deprovisioned}
								/>
								{ !isSamlEnabled && (
									<EditPasswordCard
										username={username}
										disabled={deprovisioned}
										updateUserDetails={updateUserDetails}
										startCollapsed={deprovisioned}
									/>
								)}
								<PermissionRequestsCard
									id={id}
									requests={requests}
									refresh={refresh}
									startCollapsed={!deprovisioned}
								/>
								<UserHistoryCard
									id={id}
									history={history}
									refresh={refresh}
									startCollapsed={!deprovisioned}
								/>
							</Fragment>
						)
					}
				</div>
			)}
		</div>
	);
}

const withAlertCardContainer = (WrappedComponent, headerText) => (props) => {
	const { startCollapsed } = props;
	const [display, setDisplay] = useState(false);
	const [bootstrapClass, setBootstrapClass] = useState("");
	const [message, setMessage] = useState("");
	const [collapseBody, setCollapseBody] = useState(startCollapsed || false);
	const [showUnsavedAlert, setShowUnsavedAlert] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const ref = useRef(null);

	const handle = (bootstrapClass, message) => {
		setDisplay(true);
		setBootstrapClass(bootstrapClass);
		setMessage(message);
	};

	const handleDanger = (message) => {
		handle("alert-danger", message);
	};

	const handleWarning = (message) => {
		handle("alert-warning", message);
	};

	const handleSuccess = (message) => {
		handle("alert-success", message);
	};

	const handleToggleCollapse = () => {
		setCollapseBody(!collapseBody);
		// provides a smooth transition for the collapse
		if(ref.current) {
			require(["jquery"], () => {
				$(ref.current).collapse(collapseBody ? "show" : "hide");
			});
		}
	};

	const setIsLoadingWrapper = (isLoadingNewValue) => {
		if (isLoadingNewValue) {
			setDisplay(false);
		}
		setIsLoading(isLoadingNewValue);
	}

	return (
		<div className="card mt-3">
			<div className="card-header" style={{"cursor": "pointer"}} onClick={handleToggleCollapse}>
				<div className="row">
					<div className="col">
						<i className={`fas fa-chevron-${collapseBody ? "down" : "up"}`} />&nbsp;{headerText}
					</div>
					{ showUnsavedAlert &&
						<div className="col">
							<div className="alert-warning"
								 style={{
									 float: "right",
									 width: "fit-content"
							}}>
							<strong>Make sure to save to persist updates</strong>
							</div>
						</div>
					}
				</div>
			</div>
			<div ref={ref} className={`collapse ${startCollapsed ? "" : "show"}`}>
				<div className="card-body">
					{display && (
						<div className={"alert " + bootstrapClass}>
							<p>{message}</p>
							<span
								className="btn btn-link font-weight-light font-italic"
								onClick={() => setDisplay(false)}
							>
								<small>dismiss</small>
							</span>
						</div>
					)}
					{ isLoading ?
						<div className="d-flex justify-content-center">
							<div
								className="spinner-border text-primary"
								style={{ width: "3rem", height: "3rem" }}
								role="status"
							>
								<span className="sr-only">Loading...</span>
							</div>
						</div>
						:
						<WrappedComponent
							handleDanger={handleDanger}
							handleWarning={handleWarning}
							handleSuccess={handleSuccess}
							setShowUnsavedAlert={setShowUnsavedAlert}
							setIsLoading={setIsLoadingWrapper}
							{...props}
						/>
					}
				</div>
			</div>
		</div>
	);
};

function UserInformation(
	{
		pagePermissionType,
		fullName,
		details,
		hasProfileImage,
		username,
		id,
		token,
		profileLink,
		availableZoneIds,
		currentZoneId,
		updateUserDetails,
		deprovisioned,
	}
) {

	const handleTimeZoneChange = (event) => {
		// TODO DATAROUTER-3355: error handling
		doFetch({
			path: PATHS.updateTimeZone,
			bodyObject: { username, timeZoneId: event.target.value },
			onSuccess: (response) => {
				updateUserDetails(response.response);
			},
		});
	};

	return (
		<div className="row pb-3">
			<div className="col-sm-8" >
				<table className="table-responsive">
					<tr>
						<td
							rowSpan={100}
							style={{
								verticalAlign: "top",
							}}
						>
							{hasProfileImage && (
								<div
									className="mr-2"
									style={{
										height: 120,
										width: 120,
										backgroundSize: "cover",
										backgroundPosition: "center",
										backgroundImage:
											"url(" +
											PATHS.getUserProfileImage +
											"?username=" +
											username +
											")",
									}}
								/>
							)}
						</td>
						<td colSpan={100}>
							<h2>{fullName}</h2>
						</td>
					</tr>
					<tr>
						<td className="px-1">
							<b>Username</b>
						</td>
						<td className="px-1">
							{username} [<a href={profileLink}>Profile</a>]
						</td>
					</tr>
					{[
						...details,
						{ name: "ID", value: id },
						{ name: "Token", value: token },
					].map(({ name, value, link }) => (
						<tr key={name}>
							<td className="px-1">
								<b>{name}</b>
							</td>
							<td className="px-1">
								<a href={link}>{value}</a>
							</td>
						</tr>
					))}
				</table>
			</div>
			<div className="col-sm-4">
				<Select
					title={"Time Zone"}
					options={availableZoneIds}
					defaultValue={currentZoneId}
					onChange={handleTimeZoneChange}
					containerStyle={{ float: "right" }}
					disabled={pagePermissionType !== PagePermissionType.ADMIN || deprovisioned}
				/>
			</div>
		</div>
	);
}

function Tooltip({ title, body }) {
	const ref = useRef(null);

	useEffect(() => {
		if (ref.current) {
			require(["jquery", "multiple-select"], () => {
				$(ref.current).tooltip();
			});
		}
	}, [ref]);

	return (
		<p
			data-toggle="tooltip"
			data-placement="right"
			ref={ref}
			title={title}
			style={{
				width: 'fit-content',
				marginBottom: '0em', // override bootstrap
				paddingTop: '0.5em', // to make on hover more forgiving
				paddingBottom: '0.5em', // to make on hover more forgiving
				paddingRight: '1em', // to make on hover more forgiving
				textDecoration: "underline dashed",
			}}
		>
			{ body }
		</p>
	);
}

function EditRoleTable({ userRoleMetadataList, editorUsername, deprovisioned, handleToggleRole }) {

	const getStringFromApprovalStatuses = (userRoleMetadata) => {
		let mapFunction = userRoleMetadata.hasRole ?
			(key => `${requirementStatusByApprovalType[key].requiredApprovals} ${key}`)
			: (key => `(${requirementStatusByApprovalType[key].currentApprovers.length}/${requirementStatusByApprovalType[key].requiredApprovals}) ${key}`);

		const requirementStatusByApprovalType = userRoleMetadata.requirementStatusByApprovalType;
		return Object.keys(requirementStatusByApprovalType)
			.map(mapFunction)
			.join(', ');
	};

	const getCurrentRoleGrantingGroupsString = (userRoleMetadata) => {
		return userRoleMetadata.groupsHasWithRole ? userRoleMetadata.groupsHasWithRole.join(', ') : '';
	}

	const getActionButton = (userRoleMetadata) => {
		if (userRoleMetadata.isDefaultRole) {
			// Cannot approve/revoke default roles
			return <Fragment></Fragment>;
		}
		let buttonClassName, buttonText, disabled = false;
		const editorHasApproved = Object.values(userRoleMetadata.requirementStatusByApprovalType)
			.some(requirementStatus => requirementStatus.currentApprovers.includes(editorUsername));
		if (userRoleMetadata.hasRole && !userRoleMetadata.updateType) {
			buttonClassName = "btn btn-danger mx-auto";
			buttonText = "Revoke";
			disabled = !userRoleMetadata.editorCanRevoke;
		} else if (userRoleMetadata.updateType === ROLE_UPDATE_TYPES.REVOKE) {
			buttonClassName = "btn btn-warning mx-auto";
			buttonText = "Undo Revoke";
		} else if (editorHasApproved) {
			buttonClassName = "btn btn-warning mx-auto";
			buttonText = "Undo Approval";
		} else {
			buttonClassName = "btn btn-primary mx-auto";
			buttonText = "Approve";
			disabled = !userRoleMetadata.editorPrioritizedApprovalType;
		}
		disabled = disabled || deprovisioned;
		return (
			<button
				className={buttonClassName}
				onClick={() => handleToggleRole(userRoleMetadata.roleName)}
				disabled={disabled}
				style={{ minWidth: '5.5em' }} // make 'Approve' and 'Revoke the same size
			>
				{buttonText}
			</button>
		);
	};

	const getApprovalStatusIcon = (userRoleMetadata) => {
		const marginLeft = '4px';
		if (userRoleMetadata.hasRole) {
			return <i className="fas fa-check-circle fa-lg" style={{ color: '#128a29', marginLeft }} />;
		} else if (userRoleMetadata.groupsHasWithRole && userRoleMetadata.groupsHasWithRole.length) {
			return (
				<Tooltip
					title="User has role from SAML groups, but not individually"
					body={
						<i
							className="far fa-check-circle fa-lg"
							style={{ color: '#71c27a', marginLeft }}
						/>
					}
				/>
			);
		} else {
			return <i className="fa fa-ban fa-lg" style={{ color: '#be1c00', marginLeft }} />;
		}
	}

	return (
		<div>
			<h3 className="card-title">Roles</h3>
			<table className="table table-sm table-striped">
				<thead>
				<tr>
					<th>Approval Status</th>
					<th>Role</th>
					<th>Risk Factor</th>
					<th>Approval Requirements</th>
					<th>Current Role-Granting Groups</th>
					<th className="text-right">Action</th>
				</tr>
				</thead>
				<tbody>
				{userRoleMetadataList.map((userRoleMetadata) => (
					<tr>
						<td className="align-middle">
							{
								getApprovalStatusIcon(userRoleMetadata)
							}
						</td>
						<td className="align-middle">
							<Tooltip title={ userRoleMetadata.roleDescription } body={ userRoleMetadata.roleName } />
						</td>
						<td>
							<Tooltip
								title={ userRoleMetadata.roleRiskFactorDescription }
								body={ userRoleMetadata.roleRiskFactor }
							/>
						</td>
						<td className="align-middle">{ getStringFromApprovalStatuses(userRoleMetadata) }</td>
						<td className="align-middle">{ getCurrentRoleGrantingGroupsString(userRoleMetadata) }</td>
						<td className="text-right">{ getActionButton(userRoleMetadata) }</td>
					</tr>
				))}
				</tbody>
			</table>
		</div>
	);
}

const MultiApprovalRequirementRevokeConfirmationModal = ({ handleSubmit }) => (
	<div className="modal fade" id="confirmationModal" tabIndex="-1" role="dialog"
		 aria-labelledby="confirmationModalTitle" aria-hidden="true">
		<div className="modal-dialog" role="document">
			<div className="modal-content">
				<div className="modal-header">
					<h5 className="modal-title" id="confirmationModalTitle">Confirmation Required</h5>
					<button type="button" className="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div className="modal-body">
					You've revoked roles which require multiple people to approve. If you continue, the role will need
					each of those approval requirements to be met once again to be reprovisioned.
				</div>
				<div className="modal-footer">
					<button type="button" className="btn btn-secondary" data-dismiss="modal">Cancel</button>
					<button
						type="button"
						className="btn btn-primary"
						data-dismiss="modal"
						onClick={handleSubmit}
					>
						Confirm Changes
					</button>
				</div>
			</div>
		</div>
	</div>
);

function EditRoles({
	username,
	editorUsername,
	userRoleMetadataList,
	setUserRoleMetadataList,
	updateUserDetails,
	deprovisioned,
	handleSuccess,
	handleWarning,
	handleDanger,
	setShowUnsavedAlert,
	setIsLoading,
}) {
	const [numChangesUnsaved, setNumChangesUnsaved] = useState(0);
	const [numRestrictedRolesRevoked, setNumRestrictedRolesRevoked] = useState(0);

	const handleSubmit = (event) => {
		event.preventDefault();
		const updates = userRoleMetadataList
			.filter(userRoleMetadata => Boolean(userRoleMetadata.updateType))
			.map(userRoleMetadata => ({ roleName: userRoleMetadata.roleName, updateType: userRoleMetadata.updateType }));
		const editRolesRequest = {
			username,
			updates
		};
		doFetch({
			path: PATHS.editRoles,
			bodyObject: editRolesRequest,
			onSuccess: (response) => {
				if (response.success) {
					updateUserDetails(response.response);
					setNumChangesUnsaved(0);
					setShowUnsavedAlert(false);
					// check for partial failure
					if (response.error) {
						handleWarning(response.error.message);
					} else {
						handleSuccess("Roles updated");
					}
				} else {
					handleDanger("Failed to update. " + response.error.message);
				}
			},
			onError: (error) => {
				handleDanger("Failed to update. " + error);
			},
			setIsLoading,
		});
	};

	const handleToggleRole = (roleName) => {
		const userRoleMetadata = userRoleMetadataList.find(userRoleMetadata => userRoleMetadata.roleName === roleName);

		const isRevoke = !userRoleMetadata.updateType && userRoleMetadata.hasRole;
		const isUndoRevoke = userRoleMetadata.updateType === ROLE_UPDATE_TYPES.REVOKE;
		if (isRevoke) {
			userRoleMetadata.updateType = ROLE_UPDATE_TYPES.REVOKE;
			userRoleMetadata.hasRole = false;
			if (getTotalNumApprovalsRequiredForRole(userRoleMetadata) > 1) {
				setNumRestrictedRolesRevoked(numRestrictedRolesRevoked + 1);
				userRoleMetadata.isMultiApprovalRevoke = true;
			}
		} else if (isUndoRevoke) {
			userRoleMetadata.updateType = null;
			userRoleMetadata.hasRole = true;
			if (userRoleMetadata.isMultiApprovalRevoke) {
				setNumRestrictedRolesRevoked(numRestrictedRolesRevoked - 1);
				userRoleMetadata.isMultiApprovalRevoke = false;
			}
		} else {
			// The editorPrioritizedApprovalType will be the type of current approval if the editor has approved
			const editorHasApproved = userRoleMetadata
				.requirementStatusByApprovalType[userRoleMetadata.editorPrioritizedApprovalType]
				.currentApprovers.includes(editorUsername);
			if (!editorHasApproved) {
				const approversForPrioritizedApprovalType = userRoleMetadata
					.requirementStatusByApprovalType[userRoleMetadata.editorPrioritizedApprovalType].currentApprovers;
				approversForPrioritizedApprovalType.push(editorUsername);
				userRoleMetadata.updateType = userRoleMetadata.updateType ? null : ROLE_UPDATE_TYPES.APPROVE;
			} else {
				userRoleMetadata.requirementStatusByApprovalType[userRoleMetadata.editorPrioritizedApprovalType]
					.currentApprovers =
					userRoleMetadata.requirementStatusByApprovalType[userRoleMetadata.editorPrioritizedApprovalType]
						.currentApprovers
						.filter(username => username !== editorUsername);
				userRoleMetadata.updateType = userRoleMetadata.updateType ? null : ROLE_UPDATE_TYPES.UNAPPROVE;
			}
			userRoleMetadata.hasRole = Object.values(userRoleMetadata.requirementStatusByApprovalType)
				.every(requirementStatus =>
					requirementStatus.currentApprovers.length === requirementStatus.requiredApprovals);
		}
		setUserRoleMetadataList([...userRoleMetadataList]);
		const newNumChangesUnsaved = numChangesUnsaved + (userRoleMetadata.updateType ? 1 : -1);
		setNumChangesUnsaved(newNumChangesUnsaved);
		setShowUnsavedAlert(Boolean(newNumChangesUnsaved));
	};

	const normalSaveButton = (
		<button
			className="btn btn-primary mx-auto"
			type="submit"
			disabled={deprovisioned || numChangesUnsaved === 0}
			onClick={handleSubmit}
		>
			Save
		</button>
	);

	const revokeConfirmationButton = (
		<Fragment>
			<MultiApprovalRequirementRevokeConfirmationModal handleSubmit={handleSubmit} />
			<button
				className="btn btn-primary mx-auto"
				data-toggle="modal"
				data-target="#confirmationModal"
			>
				Save
			</button>
		</Fragment>
	);

	return (
		<div>
			<div className="row">
				<div className="col-sm">
					<EditRoleTable
						userRoleMetadataList={userRoleMetadataList}
						editorUsername={editorUsername}
						handleToggleRole={handleToggleRole}
						deprovisioned={deprovisioned}
					/>
				</div>
			</div>
			{ numRestrictedRolesRevoked > 0 ? revokeConfirmationButton : normalSaveButton }
		</div>
	);
}

const EditRolesCard = withAlertCardContainer(
	EditRoles,
	"Edit Roles"
);

function EditAccounts({
	username,
	availableAccounts,
	currentAccounts,
	updateUserDetails,
	disabled,
	handleSuccess,
	handleDanger,
	setShowUnsavedAlert,
	setIsLoading,
}) {
	const [selectedAccounts, setSelectedAccounts] = useState({...currentAccounts});
	const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

	const handleToggleAccount = (event) => {
		selectedAccounts[event.target.id] = !selectedAccounts[event.target.id];
		setSelectedAccounts({...selectedAccounts}); // new object required for re-render
		const selectionsHaveChanged = JSON.stringify(selectedAccounts) !== JSON.stringify(currentAccounts);
		setHasUnsavedChanges(selectionsHaveChanged)
		setShowUnsavedAlert(selectionsHaveChanged);
	};

	const handleSubmit = (event) => {
		event.preventDefault();
		const requestDto = {
			username,
			updates: selectedAccounts,
		};
		doFetch({
			path: PATHS.editAccounts,
			bodyObject: requestDto,
			onSuccess: (response) => {
				if (response.success) {
					updateUserDetails(response.response);
					setShowUnsavedAlert(false);
					handleSuccess("Accounts updated");
				} else {
					handleDanger("Failed to update. " + response.error.message);
				}
			},
			onError: (error) => {
				handleDanger("Failed to update. " + error);
			},
			setIsLoading,
		});
	};

	return (
		<div>
			<div className="row">
				<div className="col-sm-6">
					<CheckList
						title={"Accounts"}
						plural={"accounts"}
						orderedKeys={availableAccounts}
						checkedKeys={selectedAccounts}
						disabled={disabled}
						handleToggle={handleToggleAccount}
					/>
				</div>
			</div>
			<hr/>
			<button
				className="btn btn-primary mx-auto"
				type="submit"
				disabled={disabled || !hasUnsavedChanges}
				onClick={handleSubmit}
			>
				Save
			</button>
		</div>
	);
}

const EditAccountsCard = withAlertCardContainer(
	EditAccounts,
	"Edit Accounts"
);

const CheckList = ({ title, plural, orderedKeys, checkedKeys, disabled, handleToggle }) => {
	return (
		<div>
			<h3 className="card-title">{title}</h3>
			{orderedKeys.length === 0
				? "No " + plural + " available."
				: orderedKeys.map((key) => (
					<div className="form-check" key={key}>
						<input
							type="checkbox"
							className="form-check-input"
							id={key}
							disabled={disabled}
							checked={checkedKeys[key]}
							onChange={handleToggle}
						/>
						<label className="form-check-label" for={key}>
							{key}
						</label>
					</div>
				))}
		</div>
	);
};

const Select = ({ title, options, defaultValue, onChange, containerStyle = {}, disabled=false }) => (
	<div style={ containerStyle }>
		<h4 className="card-title">{title}</h4>
		<select onChange={onChange} disabled={disabled}>
			{options.map((zone) => (
				<option key={zone} selected={defaultValue == zone} value={zone}>
					{zone}
				</option>
			))}
		</select>
	</div>
);

function EditPasswordForm({ username, updateUserDetails, disabled, handleSuccess, handleDanger, setIsLoading }) {
	const [newPassword, setNewPassword] = useState("");

	const handleUpdatePassword = (event) => {
		event.preventDefault();
		doFetch({
			path: PATHS.updatePassword,
			bodyObject: { username, newPassword },
			onSuccess: (userDetails) => {
				if (userDetails.success) {
					updateUserDetails(userDetails, () => {
						setNewPassword("");
					});
					handleSuccess("Password updated");
				} else {
					handleDanger("Failed to update password. " + userDetails.message);
				}
			},
			onError: (error) => {
				handleDanger("Failed to update password. " + error);
			},
			setIsLoading,
		});
	};

	return (
		<form className="form-inline">
			<div className="form-group mr-3">
				<label htmlFor="newPassword" className="mr-3">
					New Password
				</label>
				<input
					type="text"
					className="form-control"
					id="newPassword"
					disabled={disabled}
					value={newPassword}
					onChange={(event) => setNewPassword(event.target.value)}
				/>
			</div>
			<button
				type="submit"
				className="btn btn-primary"
				disabled={disabled}
				onClick={handleUpdatePassword}
			>
				Save New Password
			</button>
		</form>
	);
}

const EditPasswordCard = withAlertCardContainer(
	EditPasswordForm,
	"Edit Password"
);

const historyTableCellStyle = {
	border: "none",
	paddingLeft: 0,
	whiteSpace: "pre-wrap",
};

const keyWidth = "110px";
const historyTableKeyCellStyle = {
	fontWeight: 500,
	width: keyWidth,
	maxWidth: keyWidth,
	minWidth: keyWidth,
	...historyTableCellStyle
};

function PermissionRequests({ requests, refresh, handleSuccess, handleDanger, id }) {

	const handleDecline = (event) => {
		event.preventDefault();
		doFetch({
			path: PATHS.declinePermissionRequests + "?userId=" + id,
			onSuccess: (json) => {
				if (json.success) {
					refresh();
					handleSuccess("Permission successfully declined");
				} else {
					handleDanger("Failed to decline. " + json.message);
				}
			},
			onError: (error) => {
				handleDanger("Failed to decline. " + error);
			},
		});
	};

	return (
		<div>
			{requests.length === 0 ? (
				<p>No permission requests.</p>
			) : (
				requests.map((request, index) => {
					const {
						requestTimeMs,
						requestText,
						resolution,
						resolutionTimeMs,
						editor,
					} = request;

					const resolved = !!resolution;
					const requestTime = new Date(requestTimeMs).toLocaleString();
					const resolvedTime = resolved
						? new Date(resolutionTimeMs).toLocaleString()
						: "N/A";
					return (
						<Fragment key={requestTimeMs}>
							<table className="table table-sm">
								<tbody>
								<tr>
									<td style={historyTableKeyCellStyle}>Time</td>
									<td style={historyTableCellStyle}>
										{requestTime} - {resolvedTime}
									</td>
								</tr>
								<tr>
									<td style={historyTableKeyCellStyle}>Editor</td>
									<td style={historyTableCellStyle}>{editor}</td>
								</tr>
								<tr>
									<td style={historyTableKeyCellStyle}>Request Text</td>
									<td style={historyTableCellStyle}>{requestText}</td>
								</tr>
								<tr>
									<td style={historyTableKeyCellStyle}>Resolution</td>
									<td style={historyTableCellStyle}>
										{resolved ? (
											resolution
										) : (
											<button
												type="submit"
												className="btn btn-danger"
												onClick={handleDecline}
											>
												Decline
											</button>
										)}
									</td>
								</tr>
								</tbody>
							</table>
							{index !== requests.length - 1 && <hr />}
						</Fragment>
					);
				})
			)}
		</div>
	);
}

const UserHistory = ({ history }) => {
	return (
		<div>
			{history.length === 0 ? (
				<p>No user history</p>
			) : (
				history.map((h, index) => {
					const { timeMs, editor, changes, changeType } = h;
					return (
						<Fragment key={timeMs}>
							<table className="table table-sm">
								<tbody>
									<tr>
										<td style={historyTableKeyCellStyle}>Time</td>
										<td style={historyTableCellStyle}>
											{new Date(timeMs).toLocaleString()}
										</td>
									</tr>
									<tr>
										<td style={historyTableKeyCellStyle}>Editor</td>
										<td style={historyTableCellStyle}>{editor}</td>
									</tr>
									<tr>
										<td style={historyTableKeyCellStyle}>Change Type</td>
										<td style={historyTableCellStyle}>{changeType}</td>
									</tr>
									<tr>
										<td style={historyTableKeyCellStyle}>Changes</td>
										<td style={historyTableCellStyle}>{changes}</td>
									</tr>
								</tbody>
							</table>
							{index !== history.length - 1 && <hr />}
						</Fragment>
					);
				})
			)}
		</div>
	);
};

const PermissionRequestsCard = withAlertCardContainer(
	PermissionRequests,
	"Permission Request History"
);

const UserHistoryCard = withAlertCardContainer(UserHistory, "User History");

function ListAndEditUserPage() {
	const [activeUsername, setActiveUsername] = useState(INITIAL_USERNAME || null);
	const [isEditing, setIsEditing] = useState(Boolean(INITIAL_USERNAME));

	const openEditUser = (event) => {
		setActiveUsername(event.target.name);
		setIsEditing(true);
		// TODO more elegant handling of back button between /viewUsers and /editUser
		history.replaceState(null, null, PATHS.editUser + "?username=" + encodeURIComponent(event.target.name));
		document.title = "Datarouter - Edit User " + event.target.name;
	};

	const closeEditUser = () => {
		setIsEditing(false);
		// TODO more elegant handling of back button between /viewUsers and /editUser
		history.replaceState(null, null, PATHS.viewUsers);
		document.title = "Datarouter - Users";
	};

	return (
		<Fragment>
			{ isEditing ? (
				<EditUserPage
					defaultUsername={activeUsername}
					closeEditUser={closeEditUser}
				/>
			) : (
				<ViewUsersPage
					display={!isEditing}
					openEditUser={openEditUser}
				/>
				)
			}
		</Fragment>
	);
}

ReactDOM.render(
	<div style={{ minHeight: "100vh"}}>
		<ListAndEditUserPage />
	</div>,
	document.getElementById("app")
);
