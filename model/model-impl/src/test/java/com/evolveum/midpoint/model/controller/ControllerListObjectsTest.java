/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.model.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.evolveum.midpoint.api.logging.Trace;
import com.evolveum.midpoint.common.jaxb.JAXBUtil;
import com.evolveum.midpoint.common.result.OperationResult;
import com.evolveum.midpoint.logging.TraceManager;
import com.evolveum.midpoint.model.test.util.equal.UserTypeComparator;
import com.evolveum.midpoint.provisioning.api.ProvisioningService;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.ObjectTypes;
import com.evolveum.midpoint.schema.PagingTypeFactory;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.PagingType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.UserType;

/**
 * 
 * @author lazyman
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context-model.xml",
		"classpath:application-context-model-unit-test.xml", "classpath:application-context-task.xml" })
public class ControllerListObjectsTest {

	private static final File TEST_FOLDER = new File("./src/test/resources/controller/listObjects");
	private static final Trace LOGGER = TraceManager.getTrace(ControllerListObjectsTest.class);
	@Autowired(required = true)
	private ModelController controller;
	@Autowired(required = true)
	private RepositoryService repository;
	@Autowired(required = true)
	private ProvisioningService provisioning;

	@Before
	public void before() {
		Mockito.reset(repository, provisioning);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullClassType() throws Exception {
		controller.listObjects(null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullPaging() throws Exception {
		controller.listObjects(UserType.class, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullResult() throws Exception {
		controller.listObjects(UserType.class, PagingTypeFactory.createListAllPaging(), null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void userList() throws Exception {
		final ObjectListType expectedUserList = ((JAXBElement<ObjectListType>) JAXBUtil.unmarshal(new File(
				TEST_FOLDER, "user-list.xml"))).getValue();

		when(
				repository.listObjects(eq(ObjectTypes.USER.getClassDefinition()), any(PagingType.class),
						any(OperationResult.class))).thenReturn(expectedUserList);

		OperationResult result = new OperationResult("List Users");
		try {
			final ObjectListType returnedUserList = controller.listObjects(
					ObjectTypes.USER.getClassDefinition(), new PagingType(), result);

			verify(repository, times(1)).listObjects(eq(ObjectTypes.USER.getClassDefinition()),
					any(PagingType.class), any(OperationResult.class));
			testObjectListTypes(expectedUserList, returnedUserList);
		} finally {
			LOGGER.debug(result.dump());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testObjectListTypes(ObjectListType expected, ObjectListType returned) {
		assertNotNull(expected);
		assertNotNull(returned);

		List<ObjectType> expectedList = expected.getObject();
		List<ObjectType> returnedList = returned.getObject();

		assertTrue(expectedList == null ? returnedList == null : returnedList != null);
		if (expectedList == null) {
			return;
		}
		assertEquals(expectedList.size(), returnedList.size());
		if (expectedList.size() == 0) {
			return;
		}

		if (expectedList.get(0) instanceof UserType) {
			testUserLists(new ArrayList(expectedList), new ArrayList(returnedList));
		}
	}

	private void testUserLists(List<UserType> expected, List<UserType> returned) {
		UserTypeComparator comp = new UserTypeComparator();
		for (int i = 0; i < expected.size(); i++) {
			UserType u1 = expected.get(i);
			UserType u2 = returned.get(i);

			assertTrue(comp.areEqual(u1, u2));
		}
	}
}
