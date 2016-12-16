/*
 * Copyright 2011-2016 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.service;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmDataBaseTestCase;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.cwm.db.service.IDBService;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author bgoldowsky
 */
public class UserUpdateSpreadsheetReaderTest extends CwmDataTestCase {

	UserSpreadsheetReader reader;

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) throws Exception {
		IUserService userService = helper.injectAndStubUserService(this);
		helper.injectMock(ICwmService.class);
		helper.injectMock(IDBService.class);
		helper.injectMock(ISiteService.class);
		helper.injectObject(IModelProvider.class, new SimpleModelProvider());

		// Our mock SiteService will acknowledge one existing site.
//		ISiteService siteService = getHelper().injectMock(ISiteService.class);
//		when(siteService.getSiteByName(eq("existing_site"))).thenReturn(Model.of(site));
//		when(siteService.newSite()).thenReturn(new Site());
//		when(siteService.newPeriod()).thenReturn(new Period());

		when(userService.getByUsername(eq("user"))).thenReturn(Model.of(loggedInUser));
		when(userService.getBySubjectId(eq("subj"))).thenReturn(Model.of(loggedInUser));
	}

	@Override
	public void setUpData() {
		super.setUpData();

		// We'll assume the logged in user is the only existing user to be updated.
		loggedInUser.setUsername("user");
		loggedInUser.setSubjectId("subj");
		loggedInUser.setFirstName("Mickey");
		loggedInUser.setLastName("Mouse");
	}

	@Test
	public void rejectsEmptyFile() {
		reader = new UserUpdateSpreadsheetReader();
		assertFalse(reader.readInput(IOUtils.toInputStream("")));
		assertEquals("Either a 'username' column or a 'subjectid' column must be included.",
				reader.getGlobalError());
	}

	@Test
	public void acceptsMinimalFile() {
		reader = new UserUpdateSpreadsheetReader();
		String minimalFile = "username,firstname\r\n"
				+ "user,first\r\n";
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("should have no global error", "", reader.getGlobalError());

		assertEquals("should have resulted in 1 potential user", 1, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals(Role.STUDENT, user.getRole());
		assertEquals("user", user.getUsername());
		assertEquals("first", user.getFirstName());
		assertEquals("Mouse", user.getLastName());
		assertEquals("first Mouse", user.getFullName());
	}

	@Test
	public void noChangeWhenCellIsBlank() {
		reader = new UserUpdateSpreadsheetReader();
		String minimalFile = "username,firstname\r\n"
				+ "user,\r\n";
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("should have no global error", "", reader.getGlobalError());

		assertEquals("should have resulted in 1 potential user", 1, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals(Role.STUDENT, user.getRole());
		assertEquals("user", user.getUsername());
		assertEquals("Mickey", user.getFirstName()); // critically, this hasn't changed to "".
		assertEquals("Mouse", user.getLastName());
		assertEquals("Mickey Mouse", user.getFullName());
	}

	@Test
	public void rejectsNonExistentUsername() {
		reader = new UserUpdateSpreadsheetReader();
		String minimalFile = "username,firstname\r\n"
				+ "user,first\r\n"
				+ "nonuser,first\r\n";
		assertFalse("Should reject this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("", reader.getGlobalError());

		assertEquals("should have resulted in 2 potential users", 2, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());

		potential = reader.getPotentialUsers().get(1);
		assertEquals("User with username=nonuser not found", potential.getError());
	}

	@Test
	public void updatesBasedOnSubjectId() {
		reader = new UserUpdateSpreadsheetReader();
		String minimalFile = "subjectid,firstname\r\n"
				+ "subj,Field\r\n";
		assertEquals("should have no global error", null, reader.getGlobalError());
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));

		assertEquals("should have resulted in 1 potential user", 1, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals("subj", user.getSubjectId());
		assertEquals("user", user.getUsername());
		assertEquals("Field", user.getFirstName());
		assertEquals("Mouse", user.getLastName());
	}
}