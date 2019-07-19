/*
 * Copyright (c) 2019 Evolveum
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
package com.evolveum.midpoint.testing.story;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.Collection;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.evolveum.icf.dummy.resource.ConflictException;
import com.evolveum.icf.dummy.resource.DummyAccount;
import com.evolveum.icf.dummy.resource.SchemaViolationException;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.test.DummyResourceContoller;
import com.evolveum.midpoint.test.asserter.DummyAccountAsserter;
import com.evolveum.midpoint.test.util.MidPointTestConstants;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * Test for various exotic mapping-related configurations.
 */
@ContextConfiguration(locations = {"classpath:ctx-story-test-main.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestMappingMadness extends AbstractStoryTest {
	
	public static final File TEST_DIR = new File(MidPointTestConstants.TEST_RESOURCES_DIR, "mapping-madness");
	
	protected static final File RESOURCE_DUMMY_TOLERANT_FILE = new File(TEST_DIR, "resource-dummy-tolerant.xml");
	protected static final String RESOURCE_DUMMY_TOLERANT_OID = "3ec5bb34-a715-11e9-b4ce-2f312ebfee0a";
	protected static final String RESOURCE_DUMMY_TOLERANT_NAME = "tolerant";

	protected static final File RESOURCE_DUMMY_TOLERANT_RANGE_FILE = new File(TEST_DIR, "resource-dummy-tolerant-range.xml");
	protected static final String RESOURCE_DUMMY_TOLERANT_RANGE_OID = "3de6715c-a7a3-11e9-9318-e73bf1ed5ed9";
	protected static final String RESOURCE_DUMMY_TOLERANT_RANGE_NAME = "tolerant-range";
	
	protected static final File RESOURCE_DUMMY_SMART_RANGE_FILE = new File(TEST_DIR, "resource-dummy-smart-range.xml");
	protected static final String RESOURCE_DUMMY_SMART_RANGE_OID = "41510274-a7aa-11e9-a083-1b48cd667229";
	protected static final String RESOURCE_DUMMY_SMART_RANGE_NAME = "smart-range";
	
	protected static final File RESOURCE_DUMMY_NONTOLERANT_FILE = new File(TEST_DIR, "resource-dummy-nontolerant.xml");
	protected static final String RESOURCE_DUMMY_NONTOLERANT_OID = "a09869c0-a7c3-11e9-85ac-6f2cafd8f0c2";
	protected static final String RESOURCE_DUMMY_NONTOLERANT_NAME = "nontolerant";

	private static final String JACK_TITLE_WHATEVER_UPPER = "WHATEVER";
	private static final String JACK_TITLE_WHATEVER_LOWER = "whatever";
	private static final String JACK_TITLE_PIRATE = "Pirate";
	private static final String JACK_TITLE_CAPTAIN = "Captain";

	private static final String JACK_MAD_TITLE = "Madman";
	private static final String JACK_MAD_SHIP = "Black Madness";
	private static final String JACK_MAD_WEAPON_1 = "Tongue";
	private static final String JACK_MAD_WEAPON_2 = "Imagination";
	private static final String JACK_MAD_LOCATION = "Wonderland";
	private static final String JACK_MAD_DRINK_1 = "DrinkMe";
	private static final String JACK_MAD_DRINK_2 = "DrinkMeAgain";

	private static final String JACK_QUOTE = "Where's the rum?";

	@Override
	public void initSystem(Task initTask, OperationResult initResult) throws Exception {
		super.initSystem(initTask, initResult);

		initDummyResourcePirate(RESOURCE_DUMMY_TOLERANT_NAME, RESOURCE_DUMMY_TOLERANT_FILE, RESOURCE_DUMMY_TOLERANT_OID, initTask, initResult);
		initDummyResourcePirate(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, RESOURCE_DUMMY_TOLERANT_RANGE_FILE, RESOURCE_DUMMY_TOLERANT_RANGE_OID, initTask, initResult);
		initDummyResourcePirate(RESOURCE_DUMMY_SMART_RANGE_NAME, RESOURCE_DUMMY_SMART_RANGE_FILE, RESOURCE_DUMMY_SMART_RANGE_OID, initTask, initResult);
		initDummyResourcePirate(RESOURCE_DUMMY_NONTOLERANT_NAME, RESOURCE_DUMMY_NONTOLERANT_FILE, RESOURCE_DUMMY_NONTOLERANT_OID, initTask, initResult);
		
		modifyUserReplace(USER_JACK_OID, UserType.F_TITLE, initTask, initResult, createPolyString(JACK_TITLE_PIRATE));
	}
	
	/**
	 * Just a basic setup. Create the accounts with title=pirate.
	 */
	@Test
	public void test100AssignJackDummyAccounts() throws Exception {
		final String TEST_NAME = "test100AssignJackDummyAccounts";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();

		// WHEN
        displayWhen(TEST_NAME);
        
        assignAccountToUser(USER_JACK_OID, RESOURCE_DUMMY_TOLERANT_OID, null, task, result);
        assignAccountToUser(USER_JACK_OID, RESOURCE_DUMMY_TOLERANT_RANGE_OID, null, task, result);
        assignAccountToUser(USER_JACK_OID, RESOURCE_DUMMY_SMART_RANGE_OID, null, task, result);
        assignAccountToUser(USER_JACK_OID, RESOURCE_DUMMY_NONTOLERANT_OID, null, task, result);

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertJackPirateAccount(RESOURCE_DUMMY_TOLERANT_NAME);
		assertJackPirateAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME);
		assertJackPirateAccount(RESOURCE_DUMMY_SMART_RANGE_NAME);
		assertJackPirateAccount(RESOURCE_DUMMY_NONTOLERANT_NAME);
	}
	
	/**
	 * Change title from pirate to captain. Mapping madness begins.
	 * See the comments in the code for explanation.
	 */
	@Test
	public void test105ModifyJackTitleCaptain() throws Exception {
		final String TEST_NAME = "test105ModifyJackTitleCaptain";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();
        
        setAccountQuotes();

		// WHEN
        displayWhen(TEST_NAME);
        
        modifyUserReplace(USER_JACK_OID, UserType.F_TITLE, task, result, createPolyString(JACK_TITLE_CAPTAIN));

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, shipize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_TITLE_CAPTAIN)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, weaponize(JACK_TITLE_CAPTAIN))
			// location: singlevalue, tolerant, non-authoritative
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			// drink: multivalue, tolerant and non-authoritative.
			//        non-authoritative = old value is not removed
			//        tolerant = that value is not removed by reconiliation
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, shipize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_TITLE_CAPTAIN)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, weaponize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			// Authoritative mappings
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, shipize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_TITLE_CAPTAIN)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, weaponize(JACK_TITLE_CAPTAIN))
			// Non-authoritative mappings.
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			// This mapping is non-authoritative. But the old value (pirate) is in mapping range and it is not produced by the mapping. It should be gone.
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, shipize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_TITLE_CAPTAIN)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, weaponize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, drinkize(JACK_TITLE_CAPTAIN));
		
		assertAccountQuotes();
	}
	
	/**
	 * Switch title to WHATEVER. This means that mappings will produce null.
	 * 
	 * Authoritative mappings should still remove the value.
	 * Non-authoritative mappings should keep the values.
	 */
	@Test
	public void test110ModifyJackTitleWhatever() throws Exception {
		final String TEST_NAME = "test110ModifyJackTitleWhatever";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();

		// WHEN
        displayWhen(TEST_NAME);
        
        modifyUserReplace(USER_JACK_OID, UserType.F_TITLE, task, result, createPolyString(JACK_TITLE_WHATEVER_UPPER));

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		// Mappings return null, which means no value.
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			// Mappings for title, ship and weapon and authoritative. Therefore old value is removed anyway.
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			// location and drink are non-authoritative. Old values should NOT be removed.
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			// Mappings for title, ship and weapon are authoritative. 
			// Therefore the value is explicitly removed even if it is not in the range.
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			// Non-authoritative mappings. But the mapping range is smart, and in this case it is reduced to "none".
			// Therefore the old value (captain) is not in mapping range. It should stay.
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, drinkize(JACK_TITLE_CAPTAIN));

		// Mapping will produce nothing. Those are non-tolerant attributes. Nothing means nothing. All values removed.
		assertNoAttributes(assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME));
		
		assertAccountQuotes();
	}

	private void assertNoAttributes(DummyAccountAsserter<Void> asserter) {
		asserter
			.assertFullName(USER_JACK_FULL_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME);
	}

	/**
	 * Just to make sure that situation is stable (reconcile will not ruin anything).
	 */
	@Test
	public void test112ReconcileJackWhatever() throws Exception {
		final String TEST_NAME = "test112ReconcileJackWhatever";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();
        
		// WHEN
        displayWhen(TEST_NAME);
        
        reconcileUser(USER_JACK_OID, null, task, result);

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, 
					drinkize(JACK_TITLE_PIRATE), drinkize(JACK_TITLE_CAPTAIN));
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_CAPTAIN))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, drinkize(JACK_TITLE_CAPTAIN));
		
		assertNoAttributes(assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME));
		
		assertAccountQuotes();
	}
	
	/**
	 * Set a completely different set of values on a resource.
	 * As those attributes are tolerant and mappings will produce null output,
	 * resource values should not be affected.
	 */
	@Test
	public void test120MadJack() throws Exception {
		final String TEST_NAME = "test120MadJack";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();
        
        // Make sure that quotes are re-set. Especially in the nontolerant resource.
        setAccountQuotes();
        
        setAccountMad(getDummyAccount(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME));
        setAccountMad(getDummyAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME));
        setAccountMad(getDummyAccount(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME));

		// WHEN
        displayWhen(TEST_NAME);
        
        reconcileUser(USER_JACK_OID, null, task, result);

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_NAME);
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME);
		assertJackMadAccount(RESOURCE_DUMMY_SMART_RANGE_NAME);
		
		// Mapping will produce nothing. Those are non-tolerant attributes. Nothing means nothing.
		// All the mad values should be removed. 
		assertNoAttributes(assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME));
		
		assertAccountQuotes();
	}
	
	/**
	 * Change title from WHATEVER to whatever. This is a change of user, but mapping will produce the same
	 * null value for both. Therefore nothing should be changed on a resource.
	 */
	@Test
	public void test130ModifyJackTitleWhateverLower() throws Exception {
		final String TEST_NAME = "test130ModifyJackTitleWhateverLower";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();

		// WHEN
        displayWhen(TEST_NAME);
        
        modifyUserReplace(USER_JACK_OID, UserType.F_TITLE, task, result, createPolyString(JACK_TITLE_WHATEVER_LOWER));

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_NAME);
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME);
		assertJackMadAccount(RESOURCE_DUMMY_SMART_RANGE_NAME);
		assertNoAttributes(assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME));
		
		assertAccountQuotes();
	}
	
	/**
	 * Change title to no value. This means that mapping will also produce no value.
	 * This will not really affect tolerant and tolerant-range resources. In that case
	 * it is the same situation as "whatever". But smart-range resource behaves differently.
	 * No value in title means that range is NOT reduced. Therefore the old values are gone.
	 */
	@Test
	public void test140ModifyJackTitleEmpty() throws Exception {
		final String TEST_NAME = "test140ModifyJackTitleEmpty";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();

		// WHEN
        displayWhen(TEST_NAME);
        
        modifyUserReplace(USER_JACK_OID, UserType.F_TITLE, task, result /* no value */);

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_NAME);
		assertJackMadAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME);
		
		assertDummyAccountByUsername(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			// Authoritative mappings. Range is "all". Values should be gone.
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME)
			// Non-authoritative mappings. But the range is "all". Values should be gone.
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME);
		
		assertNoAttributes(assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME));
	
		assertAccountQuotes();
	}

	@Test
	public void test199UnassignJackDummyAccount() throws Exception {
		final String TEST_NAME = "test199UnassignJackDummyAccount";
		displayTestTitle(TEST_NAME);
		
		Task task = createTask(TEST_NAME);
        OperationResult result = task.getResult();

		// WHEN
        displayWhen(TEST_NAME);
        
        unassignAccountFromUser(USER_JACK_OID, RESOURCE_DUMMY_TOLERANT_OID, null, task, result);
        unassignAccountFromUser(USER_JACK_OID, RESOURCE_DUMMY_TOLERANT_RANGE_OID, null, task, result);
        unassignAccountFromUser(USER_JACK_OID, RESOURCE_DUMMY_SMART_RANGE_OID, null, task, result);
        unassignAccountFromUser(USER_JACK_OID, RESOURCE_DUMMY_NONTOLERANT_OID, null, task, result);

		// THEN
		displayThen(TEST_NAME);
		assertSuccess(result);
		
		assertNoDummyAccount(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME);
		assertNoDummyAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME);
		assertNoDummyAccount(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME);
		assertNoDummyAccount(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME);
	}
	
	private void setAccountQuotes() throws ConnectException, FileNotFoundException, SchemaViolationException, ConflictException, InterruptedException {
		getDummyAccount(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME)
			.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		getDummyAccount(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME)
			.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		getDummyAccount(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		getDummyAccount(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME)
			.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
	}

	private void assertAccountQuotes() throws ConnectException, FileNotFoundException, SchemaViolationException, ConflictException, InterruptedException {
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_NAME, USER_JACK_USERNAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		assertDummyAccountByUsername(RESOURCE_DUMMY_TOLERANT_RANGE_NAME, USER_JACK_USERNAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		assertDummyAccountByUsername(RESOURCE_DUMMY_SMART_RANGE_NAME, USER_JACK_USERNAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME, JACK_QUOTE);
		assertDummyAccountByUsername(RESOURCE_DUMMY_NONTOLERANT_NAME, USER_JACK_USERNAME)
			.assertNoAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_QUOTE_NAME);
	}
	
	private void assertJackPirateAccount(String dummyName) throws ConnectException, FileNotFoundException, SchemaViolationException, ConflictException, InterruptedException {
		assertDummyAccountByUsername(dummyName, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_TITLE_PIRATE)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, shipize(JACK_TITLE_PIRATE))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, weaponize(JACK_TITLE_PIRATE))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, locationize(JACK_TITLE_PIRATE))
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, drinkize(JACK_TITLE_PIRATE));
	}
	
	private DummyAccount setAccountMad(DummyAccount dummyAccount) throws ConnectException, FileNotFoundException, SchemaViolationException, ConflictException, InterruptedException {
		dummyAccount.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_MAD_TITLE);
		dummyAccount.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, JACK_MAD_SHIP);
		dummyAccount.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, JACK_MAD_WEAPON_1, JACK_MAD_WEAPON_2);
		dummyAccount.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, JACK_MAD_LOCATION);
		dummyAccount.replaceAttributeValues(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, JACK_MAD_DRINK_1, JACK_MAD_DRINK_2);
		return dummyAccount;
	}

	private void assertJackMadAccount(String dummyName) throws ConnectException, FileNotFoundException, SchemaViolationException, ConflictException, InterruptedException {
		assertDummyAccountByUsername(dummyName, USER_JACK_USERNAME)
			.assertFullName(USER_JACK_FULL_NAME)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_TITLE_NAME, JACK_MAD_TITLE)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_SHIP_NAME, JACK_MAD_SHIP)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_WEAPON_NAME, JACK_MAD_WEAPON_1, JACK_MAD_WEAPON_2)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_LOCATION_NAME, JACK_MAD_LOCATION)
			.assertAttribute(DummyResourceContoller.DUMMY_ACCOUNT_ATTRIBUTE_DRINK_NAME, JACK_MAD_DRINK_1, JACK_MAD_DRINK_2);
	}
	
	private String shipize(String title) {
		return "Sail like a " + title;
	}
	
	private String weaponize(String title) {
		return "Fight like a " + title;
	}
	
	private String locationize(String title) {
		return "Live like a " + title;
	}

	private String drinkize(String title) {
		return "Drink like a " + title;
	}

}