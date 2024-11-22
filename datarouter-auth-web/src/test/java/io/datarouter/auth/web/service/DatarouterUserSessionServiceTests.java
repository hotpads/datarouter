/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.auth.web.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.datarouter.auth.model.dto.InterpretedSamlAssertion;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.service.DatarouterUserCreationService;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.session.DatarouterUserSessionService;
import io.datarouter.auth.session.Session;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSession;

public class DatarouterUserSessionServiceTests{

	private static final String USERNAME = "username";
	private static final long USER_ID = 123L;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private RoleManager roleManager;
	@Mock
	private DatarouterUserService datarouterUserService;
	@Mock
	private DatarouterUserHistoryService userHistoryService;
	@Mock
	private DatarouterUserCreationService userCreationService;
	@Mock
	private DatarouterUserDao userDao;
	@Mock
	private BaseDatarouterSessionDao sessionDao;
	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private DatarouterUserSessionService userSessionService;

	@BeforeMethod
	public void setUp(){
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSignInUserFromSamlResponseNoSamlGroups(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of(), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);


		verify(userHistoryService, never()).recordSamlSignOnChanges(any(), any());
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertTrue(((DatarouterSession)session).getRoles().isEmpty());
	}

	@Test
	public void testSignInUserFromSamlResponseNewUser(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of(), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.empty());
		when(userCreationService.createAutomaticUser(
				USERNAME,
				DatarouterUserCreationService.SAML_USER_CREATION_DESCRIPTION))
				.thenReturn(user);

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, never()).recordSamlSignOnChanges(any(), any());
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertTrue(((DatarouterSession)session).getRoles().isEmpty());
	}

	@Test
	public void testSignInUserFromSamlResponseNoSamlGroupChanges(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		user.setRoles(List.of(new Role("role1"), new Role("role2")));
		user.setSamlGroups(List.of("group1", "group2"));
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of("group1", "group2"), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));
		when(roleManager.getRolesForGroup("group1")).thenReturn(Set.of(new Role("role3"), new Role("role4")));
		when(roleManager.getRolesForGroup("group2")).thenReturn(Set.of(new Role("role5"), new Role("role6")));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, never()).recordSamlSignOnChanges(any(), any());
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertEquals(new HashSet<>(user.getSamlGroups()), Set.of("group1", "group2"));
		Assert.assertEquals(
				new HashSet<>(((DatarouterSession)session).getRoles()),
				Set.of(
						new Role("role1"),
						new Role("role2"),
						new Role("role3"),
						new Role("role4"),
						new Role("role5"),
						new Role("role6")));
	}

	@Test
	public void testSignInUserFromSamlResponseNewSamlGroupsNoRoleChanges(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of("group1", "group2"), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, times(1))
				.recordSamlSignOnChanges(
						user,
						"""
								Changes detected from last SAML sign on.
								SAML groups gained: group1, group2.
								No roles provided by new SAML groups.""");
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertEquals(new HashSet<>(user.getSamlGroups()), Set.of("group1", "group2"));
		Assert.assertTrue(((DatarouterSession)session).getRoles().isEmpty());
	}

	@Test
	public void testSignInUserFromSamlResponseLostSamlGroupsNoRoleChanges(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		user.setSamlGroups(List.of("group1", "group2"));
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of(), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, times(1))
				.recordSamlSignOnChanges(
						user,
						"""
								Changes detected from last SAML sign on.
								SAML groups lost: group1, group2.
								No roles lost due to lost SAML groups.""");
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertEquals(user.getSamlGroups(), List.of());
		Assert.assertTrue(((DatarouterSession)session).getRoles().isEmpty());
	}

	@Test
	public void testSignInUserFromSamlResponseLostAndGainedSamlGroupsNoRoleChanges(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		user.setSamlGroups(List.of("group1", "group2"));
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of("group3", "group4"), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, times(1))
				.recordSamlSignOnChanges(
						user,
						"""
								Changes detected from last SAML sign on.
								SAML groups gained: group3, group4.
								SAML groups lost: group1, group2.
								No roles provided by new SAML groups.
								No roles lost due to lost SAML groups.""");
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertEquals(new HashSet<>(user.getSamlGroups()), Set.of("group3", "group4"));
		Assert.assertTrue(((DatarouterSession)session).getRoles().isEmpty());
	}

	@Test
	public void testSignInUserFromSamlResponseLostAndGainedSamlGroupsRoleChanges(){
		DatarouterUser user = new DatarouterUser(USER_ID, USERNAME);
		user.setEnabled(true);
		user.setSamlGroups(List.of("group1", "group2"));
		user.setRoles(List.of(new Role("role3"), new Role("role8")));
		InterpretedSamlAssertion assertion =
				new InterpretedSamlAssertion(USERNAME, Set.of("group3", "group4"), Set.of());
		when(datarouterUserService.findUserByUsername(USERNAME, true)).thenReturn(Optional.of(user));
		when(roleManager.getRolesForGroup("group1")).thenReturn(Set.of(new Role("role1"), new Role("role2")));
		when(roleManager.getRolesForGroup("group2")).thenReturn(Set.of(new Role("role3"), new Role("role4")));

		when(roleManager.getRolesForGroup("group3")).thenReturn(Set.of(new Role("role5"), new Role("role6")));
		when(roleManager.getRolesForGroup("group4")).thenReturn(Set.of(new Role("role7"), new Role("role8")));

		Session session = userSessionService.signInUserFromSamlResponse(request, assertion);

		verify(userHistoryService, times(1))
				.recordSamlSignOnChanges(
						user,
						"""
								Changes detected from last SAML sign on.
								SAML groups gained: group3, group4.
								SAML groups lost: group1, group2.
								Net roles gained: role5, role6, role7.
								Net roles lost: role1, role2, role4.""");
		verify(userDao, times(1)).put(user);
		verify(sessionDao, times(1)).put((DatarouterSession) session);
		Assert.assertEquals(new HashSet<>(user.getSamlGroups()), Set.of("group3", "group4"));
		Assert.assertEquals(new HashSet<>(user.getRolesIgnoreSaml()), Set.of(new Role("role3"), new Role("role8")));
		Assert.assertEquals(
				new HashSet<>(((DatarouterSession)session).getRoles()),
				Set.of(
						new Role("role3"),
						new Role("role5"),
						new Role("role6"),
						new Role("role7"),
						new Role("role8")));
	}

}
