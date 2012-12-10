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

package com.evolveum.midpoint.model;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.model.util.ModelTUtil;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.provisioning.api.ProvisioningService;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_2.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_2.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_2.OperationOptionsType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_2.ResourceObjectShadowListType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceObjectShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.UserType;
import com.evolveum.midpoint.xml.ns._public.common.fault_1_wsdl.FaultMessage;
import com.evolveum.midpoint.xml.ns._public.model.model_1_wsdl.ModelPortType;
import com.evolveum.prism.xml.ns._public.query_2.PagingType;
import com.evolveum.prism.xml.ns._public.query_2.QueryType;
import com.evolveum.prism.xml.ns._public.types_2.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_2.ModificationTypeType;

/**
 * @author lazyman
 */

@ContextConfiguration(locations = {"classpath:ctx-model-unit-test.xml",
        "classpath:ctx-model.xml",
        "classpath:ctx-configuration-test-no-repo.xml",
        "classpath:ctx-task.xml",
        "classpath:ctx-audit.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ModelWebServiceTest extends AbstractTestNGSpringContextTests {

    private static final File TEST_FOLDER_CONTROLLER = new File("./src/test/resources/controller");
    @Autowired(required = true)
    ModelPortType modelService;
    @Autowired(required = true)
    ProvisioningService provisioningService;
    @Autowired(required = true)
    @Qualifier("cacheRepositoryService")
    RepositoryService repositoryService;
    
    @BeforeSuite
	public void setup() throws SchemaException, SAXException, IOException {
		PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
		PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
	}

    @BeforeMethod
    public void before() {
        Mockito.reset(provisioningService, repositoryService);
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void addNullObject() throws FaultMessage {
        try {
            modelService.addObject(null, new Holder<String>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Add must fail.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    @SuppressWarnings("unchecked")
    public void addUserWithoutName() throws Exception {
        final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        try {
            modelService.addObject(expectedUser, new Holder<String>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        Assert.fail("add must fail.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testGetNullOid() throws FaultMessage {
        try {
            modelService.getObject(ObjectTypes.USER.getObjectTypeUri(), null, new OperationOptionsType(),
                    new Holder<ObjectType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("get must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testGetEmptyOid() throws FaultMessage {
        try {
            modelService.getObject(ObjectTypes.USER.getObjectTypeUri(), "", new OperationOptionsType(),
                    new Holder<ObjectType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("get must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testGetNullOidAndPropertyRef() throws FaultMessage {
        try {
            modelService.getObject(ObjectTypes.USER.getObjectTypeUri(), null, null,
                    new Holder<ObjectType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("get must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testGetNullPropertyRef() throws FaultMessage {
        try {
            modelService.getObject(ObjectTypes.USER.getObjectTypeUri(), "001", null,
                    new Holder<ObjectType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("get must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void getNonexistingObject() throws FaultMessage, ObjectNotFoundException, SchemaException, FileNotFoundException, JAXBException {
    	final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        try {
            final String oid = "abababab-abab-abab-abab-000000000001";
            when(
                    repositoryService.getObject(any(Class.class), eq(oid),
                            any(OperationResult.class))).thenThrow(
                    new ObjectNotFoundException("Object with oid '" + oid + "' not found."));

            modelService.getObject(ObjectTypes.USER.getObjectTypeUri(), oid, new OperationOptionsType(),
                    new Holder<ObjectType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertObjectNotFoundFault(ex);
        } finally {
        	SecurityContextHolder.getContext().setAuthentication(null);
        }
        Assert.fail("get must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testDeleteNullOid() throws FaultMessage {
        try {
            modelService.deleteObject(ObjectTypes.USER.getObjectTypeUri(), null);
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("delete must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testDeleteEmptyOid() throws FaultMessage {
        try {
            modelService.deleteObject(ObjectTypes.USER.getObjectTypeUri(), "");
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("delete must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void testDeleteNonExisting() throws FaultMessage, ObjectNotFoundException, SchemaException, JAXBException, FileNotFoundException {
        try {
            final String oid = "abababab-abab-abab-abab-000000000001";
            when(
                    repositoryService.getObject(any(Class.class), eq(oid),
                            any(OperationResult.class))).thenThrow(
                    new ObjectNotFoundException("Object with oid '' not found."));

            final UserType user = PrismTestUtil.unmarshalObject(new File(
                    TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

            modelService.deleteObject(ObjectTypes.USER.getObjectTypeUri(), oid);
        } catch (FaultMessage ex) {
            ModelTUtil.assertObjectNotFoundFault(ex);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        Assert.fail("delete must fail");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullObjectType() throws FaultMessage {
        try {
            modelService.listObjects(null, new PagingType(), null, new Holder<ObjectListType>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullObjectTypeAndPaging() throws FaultMessage {
        try {
            modelService.listObjects(null, null, null, new Holder<ObjectListType>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullPagingList() throws FaultMessage {
        try {
            modelService.listObjects("", null, null, new Holder<ObjectListType>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void badPagingList() throws FaultMessage, SchemaException, FileNotFoundException, JAXBException {
        PagingType paging = new PagingType();
        paging.setMaxSize(-1);
        paging.setOffset(-1);

        final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        try {
            modelService.listObjects(ObjectTypes.USER.getObjectTypeUri(), paging, null,
                    new Holder<ObjectListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        } finally {
        	SecurityContextHolder.getContext().setAuthentication(null);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullQueryType() throws FaultMessage {
        try {
            modelService.searchObjects(ObjectTypes.USER.getObjectTypeUri(), null, null,
                    new Holder<ObjectListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullQueryTypeAndPaging() throws FaultMessage {
        try {
            modelService.searchObjects(ObjectTypes.USER.getObjectTypeUri(), null, null,
                    new Holder<ObjectListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void badPagingSearch() throws FaultMessage, SchemaException, FileNotFoundException, JAXBException {
        PagingType paging = new PagingType();
        paging.setMaxSize(-1);
        paging.setOffset(-1);

        final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        try {
        	QueryType queryType = new QueryType();
        	queryType.setPaging(paging);
            modelService.searchObjects(ObjectTypes.USER.getObjectTypeUri(), queryType, null,
                    new Holder<ObjectListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        } finally {
        	SecurityContextHolder.getContext().setAuthentication(null);
        }
        Assert.fail("Illegal argument exception was not thrown.");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullChangeModify() throws FaultMessage {
        try {
            modelService.modifyObject(ObjectTypes.USER.getObjectTypeUri(), null);
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nonExistingUidModify() throws FaultMessage, ObjectNotFoundException, SchemaException, JAXBException, FileNotFoundException {
        final String oid = "1";
        ObjectModificationType modification = new ObjectModificationType();
        ItemDeltaType mod1 = new ItemDeltaType();
        mod1.setModificationType(ModificationTypeType.ADD);
        ItemDeltaType.Value value = new ItemDeltaType.Value();
        value.getAny().add(DOMUtil.createElement(DOMUtil.getDocument(), new QName(SchemaConstants.NS_C, "fullName")));
        mod1.setValue(value);

        modification.getModification().add(mod1);
        modification.setOid(oid);

        when(
                repositoryService.getObject(any(Class.class), eq(oid),
                        any(OperationResult.class))).thenThrow(
                new ObjectNotFoundException("Oid '" + oid + "' not found."));

        final UserType user = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));

        try {
            modelService.modifyObject(ObjectTypes.USER.getObjectTypeUri(), modification);
        } catch (FaultMessage ex) {
            ModelTUtil.assertObjectNotFoundFault(ex);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullResourceOidListShadows() throws FaultMessage {
        try {
            modelService.listResourceObjectShadows(null, "notRelevant",
                    new Holder<ResourceObjectShadowListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void emptyResourceOidListShadows() throws FaultMessage {
        try {
            modelService.listResourceObjectShadows(null, "notRelevant",
                    new Holder<ResourceObjectShadowListType>(), new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullShadowTypeListShadows() throws FaultMessage {
        try {
            modelService.listResourceObjectShadows("1", null,
                    new Holder<ResourceObjectShadowListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void emptyShadowTypeListShadows() throws FaultMessage {
        try {
            modelService.listResourceObjectShadows("1", "", new Holder<ResourceObjectShadowListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = FaultMessage.class)
    public <T extends ResourceObjectShadowType> void nonexistingResourceOidListResourceShadow() throws FaultMessage, ObjectNotFoundException, SchemaException, FileNotFoundException, JAXBException {
        final String resourceOid = "abababab-abab-abab-abab-000000000001";
        final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        when(
                repositoryService.listResourceObjectShadows(eq(resourceOid),
                        eq((Class<T>) ObjectTypes.ACCOUNT.getClassDefinition()), any(OperationResult.class))).thenThrow(
                new ObjectNotFoundException("Resource with oid '" + resourceOid + "' not found."));

        try {
            modelService.listResourceObjectShadows(resourceOid, ObjectTypes.ACCOUNT.getObjectTypeUri(),
                    new Holder<ResourceObjectShadowListType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertObjectNotFoundFault(ex);
        } finally {
        	SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Test
    public void badResourceShadowTypeListResourceObjectShadows() throws FaultMessage, SchemaException, FileNotFoundException, JAXBException {
    	final UserType expectedUser = PrismTestUtil.unmarshalObject(new File(
                TEST_FOLDER_CONTROLLER, "./addObject/add-user-without-name.xml"), UserType.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(expectedUser, null));
        Holder<ResourceObjectShadowListType> listHolder = new Holder<ResourceObjectShadowListType>();
        modelService.listResourceObjectShadows(
                "abababab-abab-abab-abab-000000000001", ObjectTypes.GENERIC_OBJECT.getObjectTypeUri(),
                listHolder,
                new Holder<OperationResultType>());

        assertNotNull(listHolder.value);
        assertEquals(0, listHolder.value.getObject().size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void nullAccountOidListAccountShadowOwner() throws FaultMessage {
        try {
            modelService.listAccountShadowOwner(null, new Holder<UserType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument excetion must be thrown");
    }

    @Test(expectedExceptions = FaultMessage.class)
    public void emptyAccountOidListAccountShadowOwner() throws FaultMessage {
        try {
            modelService.listAccountShadowOwner("", new Holder<UserType>(),
                    new Holder<OperationResultType>());
        } catch (FaultMessage ex) {
            ModelTUtil.assertIllegalArgumentFault(ex);
        }
        Assert.fail("Illegal argument excetion must be thrown");
    }
}
