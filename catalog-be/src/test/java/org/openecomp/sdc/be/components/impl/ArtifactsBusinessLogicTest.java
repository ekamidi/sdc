/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.utils.ArtifactBuilder;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_ENV_NAME;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.HEAT_VF_ENV_NAME;

public class ArtifactsBusinessLogicTest {

	public static final User USER = new User("John", "Doh", "jh0003", "jh0003@gmail.com", "ADMIN",
			System.currentTimeMillis());
	private final static String RESOURCE_INSTANCE_NAME = "Service-111";
	private final static String INSTANCE_ID = "S-123-444-ghghghg";

	private final static String ARTIFACT_NAME = "service-Myservice-template.yml";
	private final static String ARTIFACT_LABEL = "assettoscatemplate";
	private final static String ES_ARTIFACT_ID = "123123dfgdfgd0";
	private final static byte[] PAYLOAD = "some payload".getBytes();
	public static final String RESOURCE_NAME = "My-Resource_Name with   space";
	public static final String RESOURCE_CATEGORY = "Network Layer 2-3/Router";
	public static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
	public static final String RESOURCE_SUBCATEGORY = "Router";
	final ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);

	TitanDao mockTitanDao = Mockito.mock(TitanDao.class);

	static ConfigurationSource configurationSource = new FSConfigurationSource(
			ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
	static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

	@InjectMocks
	private static ArtifactsBusinessLogic artifactBL;

	private static User user = null;
	private static Resource resourceResponse = null;
	private static ResponseFormatManager responseManager = null;

	@Mock
	private UserBusinessLogic userBusinessLogic;
	@Mock
	private ArtifactOperation artifactOperation;
	@Mock
	public ComponentsUtils componentsUtils;
	@Mock
	private IInterfaceLifecycleOperation lifecycleOperation;
	@Mock
	private IUserAdminOperation userOperation;
	@Mock
	private IElementOperation elementOperation;
	@Mock
	private ArtifactCassandraDao artifactCassandraDao;
	@Mock
	public ToscaOperationFacade toscaOperationFacade;
	@Mock
	private NodeTemplateOperation nodeTemplateOperation;
	@Mock
	private ArtifactsOperations artifactsOperations;
	@Mock
	private IGraphLockOperation graphLockOperation;
	@Mock
	TitanDao titanDao;

	public static final Resource resource = Mockito.mock(Resource.class);
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private static List<ArtifactType> getAllTypes() {
		List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();
		List<String> artifactTypesList = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getArtifactTypes();
		for (String artifactType : artifactTypesList) {
			ArtifactType artifactT = new ArtifactType();
			artifactT.setName(artifactType);
			artifactTypes.add(artifactT);
		}
		return artifactTypes;
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		Either<ArtifactDefinition, StorageOperationStatus> NotFoundResult = Either
				.right(StorageOperationStatus.NOT_FOUND);

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> NotFoundResult2 = Either
				.right(StorageOperationStatus.NOT_FOUND);
		when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Service), Mockito.anyBoolean()))
				.thenReturn(NotFoundResult2);
		when(artifactOperation.getArtifacts(Mockito.anyString(), eq(NodeTypeEnum.Resource), Mockito.anyBoolean()))
				.thenReturn(NotFoundResult2);

		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> notFoundInterfaces = Either
				.right(StorageOperationStatus.NOT_FOUND);
		when(lifecycleOperation.getAllInterfacesOfResource(Mockito.anyString(), Mockito.anyBoolean()))
				.thenReturn(notFoundInterfaces);

		Either<User, ActionStatus> getUserResult = Either.left(USER);

		when(userOperation.getUserData("jh0003", false)).thenReturn(getUserResult);

		Either<List<ArtifactType>, ActionStatus> getType = Either.left(getAllTypes());
		when(elementOperation.getAllArtifactTypes()).thenReturn(getType);

		when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VFC);

		// User data and management
		user = new User();
		user.setUserId("jh0003");
		user.setFirstName("Jimmi");
		user.setLastName("Hendrix");
		user.setRole(Role.ADMIN.name());

		// createResource
		resourceResponse = createResourceObject(true);
		Either<Resource, StorageOperationStatus> eitherCreate = Either.left(resourceResponse);
		Either<Integer, StorageOperationStatus> eitherValidate = Either.left(null);
		when(toscaOperationFacade.createToscaComponent(any(Resource.class))).thenReturn(eitherCreate);
		when(toscaOperationFacade.validateCsarUuidUniqueness(Mockito.anyString())).thenReturn(eitherValidate);
		Map<String, DataTypeDefinition> emptyDataTypes = new HashMap<String, DataTypeDefinition>();
		when(applicationDataTypeCache.getAll()).thenReturn(Either.left(emptyDataTypes));
		when(mockTitanDao.commit()).thenReturn(TitanOperationStatus.OK);

		Either<Component, StorageOperationStatus> resourceStorageOperationStatusEither = Either
				.right(StorageOperationStatus.BAD_REQUEST);
		when(toscaOperationFacade.getToscaElement(resourceResponse.getUniqueId()))
				.thenReturn(resourceStorageOperationStatusEither);
	}

	@Test
	public void testValidJson() {
		ArtifactDefinition ad = createArtifactDef();

		String jsonArtifact = "";

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		try {
			jsonArtifact = mapper.writeValueAsString(ad);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact,
				ArtifactDefinition.class);
		assertEquals(ad, afterConvert);
	}

	private ArtifactDefinition createArtifactDef() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1.yaml");
		ad.setArtifactLabel("label1");
		ad.setDescription("description");
		ad.setArtifactType(ArtifactTypeEnum.HEAT.getType());
		ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);
		return ad;
	}

	private Resource createResourceObject(boolean afterCreate) {
		Resource resource = new Resource();
		resource.setName(RESOURCE_NAME);
		resource.addCategory(RESOURCE_CATEGORY1, RESOURCE_SUBCATEGORY);
		resource.setDescription("My short description");
		List<String> tgs = new ArrayList<String>();
		tgs.add("test");
		tgs.add(resource.getName());
		resource.setTags(tgs);
		List<String> template = new ArrayList<String>();
		template.add("Root");
		resource.setDerivedFrom(template);
		resource.setVendorName("Motorola");
		resource.setVendorRelease("1.0.0");
		resource.setContactId("ya5467");
		resource.setIcon("MyIcon");

		if (afterCreate) {
			resource.setName(resource.getName());
			resource.setVersion("0.1");
			resource.setUniqueId(resource.getName().toLowerCase() + ":" + resource.getVersion());
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
			resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		}
		return resource;
	}

	@Test
	public void testInvalidStringGroupType() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", "www");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
				ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidNumberGroupType() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", 123);

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
				ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidGroupTypeWithSpace() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", " DEPLOYMENT");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
				ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidTimeoutWithSpace() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("timeout", "dfsdf15");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(),
				ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testValidMibAritactsConfiguration() {
		Map<String, ArtifactTypeConfig> componentDeploymentArtifacts = ConfigurationManager.getConfigurationManager()
				.getConfiguration().getResourceDeploymentArtifacts();
		Map<String, ArtifactTypeConfig> componentInstanceDeploymentArtifacts = ConfigurationManager
				.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();
		assertTrue(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType()));
		assertTrue(componentDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType()));
		assertTrue(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_POLL.getType()));
		assertTrue(componentInstanceDeploymentArtifacts.containsKey(ArtifactTypeEnum.SNMP_TRAP.getType()));
	}

	@Test
	public void testDownloadServiceArtifactByNames() {
		Service service = new Service();
		String serviceName = "myService";
		String serviceVersion = "1.0";
		String serviceId = "serviceId";
		service.setName(serviceName);
		service.setVersion(serviceVersion);
		service.setUniqueId(serviceId);

		String artifactName = "service-Myservice-template.yml";
		String artifactLabel = "assettoscatemplate";
		String esArtifactId = "123123dfgdfgd0";
		byte[] payload = "some payload".getBytes();
		ArtifactDefinition toscaTemplateArtifact = new ArtifactDefinition();
		toscaTemplateArtifact.setArtifactName(artifactName);
		toscaTemplateArtifact.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		toscaTemplateArtifact.setArtifactLabel(artifactLabel);
		toscaTemplateArtifact.setEsId(esArtifactId);
		toscaTemplateArtifact.setPayload(payload);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		toscaArtifacts.put(artifactLabel, toscaTemplateArtifact);
		service.setToscaArtifacts(toscaArtifacts);

		ESArtifactData esArtifactData = new ESArtifactData(esArtifactId);
		esArtifactData.setDataAsArray(payload);
		Either<ESArtifactData, CassandraOperationStatus> artifactfromESres = Either.left(esArtifactData);
		when(artifactCassandraDao.getArtifact(esArtifactId)).thenReturn(artifactfromESres);
		List<org.openecomp.sdc.be.model.Component> serviceList = new ArrayList<>();
		serviceList.add(service);
		Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> getServiceRes = Either
				.left(serviceList);
		when(toscaOperationFacade.getBySystemName(ComponentTypeEnum.SERVICE, serviceName)).thenReturn(getServiceRes);
		Either<byte[], ResponseFormat> downloadServiceArtifactByNamesRes = artifactBL
				.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
		assertTrue(downloadServiceArtifactByNamesRes.isLeft());
		assertTrue(downloadServiceArtifactByNamesRes.left().value() != null
				&& downloadServiceArtifactByNamesRes.left().value().length == payload.length);
	}

	@Test
	public void createHeatEnvPlaceHolder_vf_emptyHeatParameters() throws Exception {
		ArtifactDefinition heatArtifact = new ArtifactBuilder()
				.addHeatParam(ObjectGenerator.buildHeatParam("defVal1", "val1"))
				.addHeatParam(ObjectGenerator.buildHeatParam("defVal2", "val2")).build();

		Resource component = new Resource();
		when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
		when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
				eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
						.thenReturn(Either.left(new ArtifactDefinition()));
		Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
				heatArtifact, HEAT_VF_ENV_NAME, "parentId", NodeTypeEnum.Resource, "parentName", USER, component,
				Collections.emptyMap());
		assertTrue(heatEnvPlaceHolder.isLeft());
		assertNull(heatEnvPlaceHolder.left().value().getListHeatParameters());
	}

	@Test
	public void createHeatEnvPlaceHolder_resourceInstance_copyHeatParamasCurrValuesToHeatEnvDefaultVal()
			throws Exception {
		HeatParameterDefinition heatParam1 = ObjectGenerator.buildHeatParam("defVal1", "val1");
		HeatParameterDefinition heatParam2 = ObjectGenerator.buildHeatParam("defVal2", "val2");
		HeatParameterDefinition heatParam3 = ObjectGenerator.buildHeatParam("defVal3", "val3");
		ArtifactDefinition heatArtifact = new ArtifactBuilder().addHeatParam(heatParam1).addHeatParam(heatParam2)
				.addHeatParam(heatParam3).build();

		Resource component = new Resource();

		when(userBusinessLogic.getUser(anyString(), anyBoolean())).thenReturn(Either.left(USER));
		when(artifactsOperations.addHeatEnvArtifact(any(ArtifactDefinition.class), any(ArtifactDefinition.class),
				eq(component.getUniqueId()), eq(NodeTypeEnum.Resource), eq(true), eq("parentId")))
						.thenReturn(Either.left(new ArtifactDefinition()));

		Either<ArtifactDefinition, ResponseFormat> heatEnvPlaceHolder = artifactBL.createHeatEnvPlaceHolder(
				heatArtifact, HEAT_ENV_NAME, "parentId", NodeTypeEnum.ResourceInstance, "parentName", USER, component,
				Collections.emptyMap());

		assertTrue(heatEnvPlaceHolder.isLeft());
		ArtifactDefinition heatEnvArtifact = heatEnvPlaceHolder.left().value();
		List<HeatParameterDefinition> listHeatParameters = heatEnvArtifact.getListHeatParameters();
		assertEquals(listHeatParameters.size(), 3);
		verifyHeatParam(listHeatParameters.get(0), heatParam1);
		verifyHeatParam(listHeatParameters.get(1), heatParam2);
		verifyHeatParam(listHeatParameters.get(2), heatParam3);
	}

	@Test
	public void buildArtifactPayloadWhenShouldLockAndInTransaction() {
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactName(ARTIFACT_NAME);
		artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
		artifactDefinition.setEsId(ES_ARTIFACT_ID);
		artifactDefinition.setPayload(PAYLOAD);
		artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

		when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
		when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
				any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
		when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
		when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
		artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
				ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, true);
	}

	@Test
	public void buildArtifactPayloadWhenShouldLockAndNotInTransaction() {
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactName(ARTIFACT_NAME);
		artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
		artifactDefinition.setEsId(ES_ARTIFACT_ID);
		artifactDefinition.setPayload(PAYLOAD);
		artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

		when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
		when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
				any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
		when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
		when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
		artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
				ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, false);
		verify(titanDao, times(1)).commit();
	}
	
	
	private ArtifactDefinition buildArtifactPayload() {
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactName(ARTIFACT_NAME);
		artifactDefinition.setArtifactType(ArtifactTypeEnum.TOSCA_TEMPLATE.getType());
		artifactDefinition.setArtifactLabel(ARTIFACT_LABEL);
		artifactDefinition.setEsId(ES_ARTIFACT_ID);
		artifactDefinition.setPayload(PAYLOAD);
		artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.TOSCA);

		when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
		when(artifactsOperations.updateArtifactOnResource(any(ArtifactDefinition.class), any(), any(),
				any(NodeTypeEnum.class), any(String.class))).thenReturn(Either.left(artifactDefinition));
		when(artifactCassandraDao.saveArtifact(any())).thenReturn(CassandraOperationStatus.OK);
		when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
		artifactBL.generateAndSaveHeatEnvArtifact(artifactDefinition, String.valueOf(PAYLOAD),
				ComponentTypeEnum.SERVICE, new Service(), RESOURCE_INSTANCE_NAME, USER, INSTANCE_ID, true, false);
		verify(titanDao, times(1)).commit();
		return artifactDefinition;
	}

	private void verifyHeatParam(HeatParameterDefinition heatEnvParam, HeatParameterDefinition heatYamlParam) {
		assertEquals(heatEnvParam.getDefaultValue(), heatYamlParam.getCurrentValue());
		assertNull(heatEnvParam.getCurrentValue());
	}
	
	private ArtifactsBusinessLogic createTestSubject() {
		return new ArtifactsBusinessLogic();
	}

	@Test
	public void testBuildJsonStringForCsarVfcArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifact = new ArtifactDefinition();
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildJsonStringForCsarVfcArtifact", new Object[] { artifact });
	}

	@Test
	public void testCheckArtifactInComponent() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Component component = new Resource();
		String artifactId = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "checkArtifactInComponent",
				new Object[] { component, artifactId });
	}



	@Test
	public void testCheckCreateFields() throws Exception {
		ArtifactsBusinessLogic testSubject;
		// User user = USER;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactGroupTypeEnum type = ArtifactGroupTypeEnum.DEPLOYMENT;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "checkCreateFields", new Object[] { user, artifactInfo, type });
	}


	@Test
	public void testComposeArtifactId() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String resourceId = "";
		String artifactId = "";
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		;
		String interfaceName = "";
		String operationName = "";
		String result;

		// test 1
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "composeArtifactId",
				new Object[] { resourceId, artifactId, artifactInfo, interfaceName, operationName });
	}

	@Test
	public void testConvertParentType() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		NodeTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertParentType", new Object[] { componentType });
	}

	@Test
	public void testConvertToOperation() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		String operationName = "";
		Operation result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToOperation",
				new Object[] { artifactInfo, operationName });
	}

	@Test
	public void testCreateInterfaceArtifactNameFromOperation() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String operationName = "";
		String artifactName = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createInterfaceArtifactNameFromOperation",
				new Object[] { operationName, artifactName });
	}

	@Test
	public void testFetchArtifactsFromComponent() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String artifactId = "";
		Component component = createResourceObject(true);
		Map<String, ArtifactDefinition> artifacts = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "fetchArtifactsFromComponent",
				new Object[] { artifactId, component, artifacts });
	}


	





	@Test
	public void testValidateArtifact() throws Exception {
	ArtifactsBusinessLogic testSubject;String componentId = "";
	ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
	ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
	ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
	String artifactId = "";
	ArtifactDefinition artifactInfo = buildArtifactPayload();
	AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;
	
	Component component =  createResourceObject(true);
	Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
	boolean shouldLock = false;
	boolean inTransaction = false;
	ArtifactDefinition result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "validateArtifact", new Object[]{componentId, componentType, operation, artifactId, artifactInfo, auditingAction, user, component, component, errorWrapper, shouldLock, inTransaction});
	}

	
	@Test
	public void testHandleHeatEnvDownload() throws Exception {
	ArtifactsBusinessLogic testSubject;String componentId = "";
	ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
	
	Component component =  createResourceObject(true);
	ArtifactDefinition artifactInfo =  buildArtifactPayload();
	Either<ArtifactDefinition,ResponseFormat> validateArtifact = Either.left(artifactInfo);
	Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
	boolean shouldLock = false;
	boolean inTransaction = false;
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "handleHeatEnvDownload", new Object[]{componentId, componentType, user, component, validateArtifact, errorWrapper, shouldLock, inTransaction});
	}

	
	@Test
	public void testArtifactGenerationRequired() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Component component =  createResourceObject(true);
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "artifactGenerationRequired",
				new Object[] { component, artifactInfo });
	}


	@Test
	public void testUpdateGroupForHeat() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition artAfterUpdate = null;
		Component component = createResourceObject(true);
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "updateGroupForHeat", new Object[] { artifactInfo,
				artifactInfo, component, componentType });
	}

	
	@Test
	public void testUpdateGroupForHeat_1() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo = buildArtifactPayload();;;
		Component component = createResourceObject(true);
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "updateGroupForHeat",
				new Object[] { artifactInfo, artifactInfo, artifactInfo,
						artifactInfo, component, componentType });
	}


	
	@Test
	public void testHandleAuditing() throws Exception {
		ArtifactsBusinessLogic testSubject;
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
		Component component =  createResourceObject(true);
		String componentId = "";
		
		ArtifactDefinition artifactDefinition = buildArtifactPayload();;;
		String prevArtifactUuid = "";
		String currentArtifactUuid = "";
		ResponseFormat responseFormat = new ResponseFormat();
		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
		String resourceInstanceName = "";

		// test 1
		testSubject = createTestSubject();
		testSubject.handleAuditing(auditingActionEnum, component, componentId, user, artifactDefinition,
				prevArtifactUuid, currentArtifactUuid, responseFormat, componentTypeEnum, resourceInstanceName);
	}

	@Test
	public void testCreateArtifactAuditingFields() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactDefinition = null;
		String prevArtifactUuid = "";
		String currentArtifactUuid = "";
		Map<AuditingFieldsKeysEnum, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createArtifactAuditingFields(artifactDefinition, prevArtifactUuid, currentArtifactUuid);
	}



	


	
	@Test
	public void testIgnoreUnupdateableFieldsInUpdate() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
		ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifactInfo = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "ignoreUnupdateableFieldsInUpdate",
				new Object[] { operation, artifactInfo, artifactInfo });
	}

	
	@Test
	public void testFindArtifactOnParentComponent() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Component component = createResourceObject(true);
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		String parentId = "";
		ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
		ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
		String artifactId = "";
		Either<ArtifactDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "findArtifactOnParentComponent", new Object[] { component,
				componentType, parentId, operation, artifactId });
	}

	

	
	@Test
	public void testValidateInformationalArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		Component component = createResourceObject(true);
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateInformationalArtifact",
				new Object[] { artifactInfo, component });
	}

	



	
	@Test
	public void testGetUpdatedGroups() throws Exception {
	ArtifactsBusinessLogic testSubject;String artifactId = "";
	ArtifactDefinition artifactInfo = buildArtifactPayload();
	List<GroupDefinition> groups = new ArrayList<>();
	List<GroupDataDefinition> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "getUpdatedGroups", new Object[]{artifactId, artifactInfo, groups});
	}

	
	@Test
	public void testGetUpdatedGroupInstances() throws Exception {
	ArtifactsBusinessLogic testSubject;String artifactId = "";
	ArtifactDefinition artifactInfo = buildArtifactPayload();
	List<GroupDefinition> groups = new ArrayList<>();
	List<GroupInstance> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getUpdatedGroupInstances", new Object[]{artifactId, artifactInfo, groups});
	}

	

	
	@Test
	public void testFindArtifact_1() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String artifactId = "";
		Component component =  createResourceObject(true);
		String parentId = "";
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		Either<ImmutablePair<ArtifactDefinition, ComponentInstance>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "findArtifact",
				new Object[] { artifactId, component, parentId, componentType });
	}

	
	@Test
	public void testFetchArtifactsFromInstance() throws Exception {
	ArtifactsBusinessLogic testSubject;String artifactId = "";
	Map<String,ArtifactDefinition> artifacts = new HashMap<>();
	ComponentInstance instance = new ComponentInstance();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "fetchArtifactsFromInstance", new Object[]{artifactId, artifacts, instance});
	}

	

	
	@Test
	public void testGenerateCustomizationUUIDOnInstance() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String componentId = "";
		String instanceId = "";
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "generateCustomizationUUIDOnInstance",
				new Object[] { componentId, instanceId, componentType });
	}


	
	@Test
	public void testFindComponentInstance() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String componentInstanceId = "";
		Component component =  createResourceObject(true);;
		ComponentInstance result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "findComponentInstance",
				new Object[] { componentInstanceId, component });
	}

	
	@Test
	public void testValidateDeploymentArtifactConf() throws Exception {
	ArtifactsBusinessLogic testSubject;ArtifactDefinition artifactInfo =  buildArtifactPayload();
	Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
	ArtifactTypeEnum artifactType = ArtifactTypeEnum.AAI_SERVICE_MODEL;
	Map<String,ArtifactTypeConfig> resourceDeploymentArtifacts = new HashMap<>();
	
	
	// test 1
	testSubject=createTestSubject();
	Deencapsulation.invoke(testSubject, "validateDeploymentArtifactConf", new Object[]{artifactInfo, responseWrapper, artifactType, resourceDeploymentArtifacts});
	}

	
	@Test
	public void testFillDeploymentArtifactTypeConf() throws Exception {
		ArtifactsBusinessLogic testSubject;
		NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
		Map<String, ArtifactTypeConfig> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "fillDeploymentArtifactTypeConf",
				new Object[] { parentType });
	}

	
	@Test
	public void testValidateArtifactTypeExists() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Wrapper<ResponseFormat> responseWrapper = null;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();

		// default test
		testSubject = createTestSubject();
		testSubject.validateArtifactTypeExists(responseWrapper, artifactInfo);
	}

	
	@Test
	public void testGetDeploymentArtifactTypeConfig() throws Exception {
		ArtifactsBusinessLogic testSubject;
		NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
		ArtifactTypeEnum artifactType = ArtifactTypeEnum.AAI_SERVICE_MODEL;
		ArtifactTypeConfig result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getDeploymentArtifactTypeConfig",
				new Object[] { parentType,artifactType });
	}



	
	@Test
	public void testValidateHeatEnvDeploymentArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Component component = createResourceObject(true);
		String parentId = "";
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		NodeTypeEnum parentType = NodeTypeEnum.AdditionalInfoParameters;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateHeatEnvDeploymentArtifact",
				new Object[] { component, parentId, artifactInfo, parentType });
	}

	
	@Test
	public void testFillArtifactPayloadValidation() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		Wrapper<byte[]> payloadWrapper = new Wrapper<>();;
		ArtifactDefinition artifactDefinition = buildArtifactPayload();

		// default test
		testSubject = createTestSubject();
		testSubject.fillArtifactPayloadValidation(errorWrapper, payloadWrapper, artifactDefinition);
	}

	

	


	
	@Test
	public void testValidateValidYaml() throws Exception {
	ArtifactsBusinessLogic testSubject;Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
	ArtifactDefinition artifactInfo =  buildArtifactPayload();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "validateValidYaml", new Object[]{errorWrapper, artifactInfo});
	}

	
	@Test
	public void testIsValidXml() throws Exception {
		ArtifactsBusinessLogic testSubject;
		byte[] xmlToParse = new byte[] { ' ' };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isValidXml", new Object[] { xmlToParse });
	}

	


	
	@Test
	public void testIsValidJson() throws Exception {
		ArtifactsBusinessLogic testSubject;
		byte[] jsonToParse = new byte[] { ' ' };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isValidJson", new Object[] { jsonToParse });
	}

	
	@Test
	public void testValidateSingleDeploymentArtifactName() throws Exception {
	ArtifactsBusinessLogic testSubject;Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
	String artifactName = "";
	Component component = createResourceObject(true);
	NodeTypeEnum parentType = null;
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "validateSingleDeploymentArtifactName", new Object[]{errorWrapper, artifactName, component, NodeTypeEnum.class});
	}


	
	@Test
	public void testValidateHeatDeploymentArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		boolean isCreate = false;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifact = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateHeatDeploymentArtifact",
				new Object[] { isCreate, artifactInfo, artifactInfo });
	}

	
	@Test
	public void testValidateResourceType() throws Exception {
	ArtifactsBusinessLogic testSubject;
	ResourceTypeEnum resourceType = ResourceTypeEnum.VF;
	ArtifactDefinition artifactInfo =  buildArtifactPayload();
	List<String> typeList = new ArrayList<>();
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();
	result=Deencapsulation.invoke(testSubject, "validateResourceType", new Object[]{resourceType, artifactInfo, typeList});
	}

	
	@Test
	public void testValidateAndConvertHeatParamers() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		String artifactType = "";
		Either<ArtifactDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateAndConvertHeatParamers",
				new Object[] { artifactInfo, artifactType });
	}

	
	@Test
	public void testGetDeploymentArtifacts() throws Exception {
		ArtifactsBusinessLogic testSubject;
		Component component = createResourceObject(true);
		NodeTypeEnum parentType = null;
		String ciId = "";
		List<ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDeploymentArtifacts(component, parentType, ciId);
	}


	
	@Test
	public void testValidateFirstUpdateHasPayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifact = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateFirstUpdateHasPayload",
				new Object[] { artifactInfo, artifactInfo });
	}

	
	@Test
	public void testValidateAndSetArtifactname() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateAndSetArtifactname",
				new Object[] { artifactInfo });
	}

	
	@Test
	public void testValidateArtifactTypeNotChanged() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifact = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateArtifactTypeNotChanged",
				new Object[] { artifactInfo, artifactInfo });
	}

	
	@Test
	public void testValidateOrSetArtifactGroupType() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifact = null;
		Either<ArtifactDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateOrSetArtifactGroupType",
				new Object[] { artifactInfo, artifactInfo });
	}

	
	@Test
	public void testCheckAndSetUnUpdatableFields() throws Exception {
		ArtifactsBusinessLogic testSubject;
		
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		ArtifactDefinition currentArtifact = null;
		ArtifactGroupTypeEnum type = null;

		// test 1
		testSubject = createTestSubject();
		type = null;
		Deencapsulation.invoke(testSubject, "checkAndSetUnUpdatableFields", new Object[] { user,
				artifactInfo, artifactInfo, ArtifactGroupTypeEnum.class });
	}

	
	@Test
	public void testCheckAndSetUnupdatableHeatParams() throws Exception {
	ArtifactsBusinessLogic testSubject;
	List<HeatParameterDefinition> heatParameters = new ArrayList<>();
	List<HeatParameterDefinition> currentParameters = new ArrayList<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "checkAndSetUnupdatableHeatParams", new Object[]{heatParameters, currentParameters});
	}

	
	@Test
	public void testGetMapOfParameters() throws Exception {
	ArtifactsBusinessLogic testSubject;
	List<HeatParameterDefinition> currentParameters = new ArrayList<>();
	Map<String,HeatParameterDefinition> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getMapOfParameters", new Object[]{currentParameters});
	}





	
	@Test
	public void testHandlePayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		boolean isArtifactMetadataUpdate = false;
		Either<byte[], ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "handlePayload",
				new Object[] { artifactInfo, isArtifactMetadataUpdate });
	}

	


	
	@Test
	public void testValidateYmlPayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		byte[] decodedPayload = new byte[] { ' ' };
		String artifactType = "";
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateYmlPayload",
				new Object[] { decodedPayload, artifactType });
	}

	
	@Test
	public void testValidateXmlPayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		byte[] payload = new byte[] { ' ' };
		String artifactType = "";
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateXmlPayload", new Object[] { payload, artifactType });
	}

	
	@Test
	public void testValidateJsonPayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		byte[] payload = new byte[] { ' ' };
		String type = "";
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateJsonPayload", new Object[] { payload, type });
	}
	




	
	@Test
	public void testValidateUserRole() throws Exception {
		ArtifactsBusinessLogic testSubject;
		
		AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;
		String componentId = "";
		String artifactId = "";
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
		ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateUserRole",
				new Object[] { user, auditingAction, componentId, artifactId, componentType,
						operation });
	}

	@Test
	public void testDetectAuditingType() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
		ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
		String origMd5 = "";
		AuditingActionEnum result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "detectAuditingType",
				new Object[] { operation, origMd5 });
	}

	

	
	@Test
	public void testCreateEsArtifactData() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDataDefinition artifactInfo = buildArtifactPayload();;;
		byte[] artifactPayload = new byte[] { ' ' };
		ESArtifactData result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createEsArtifactData(artifactInfo, artifactPayload);
	}

	


	
	@Test
	public void testIsArtifactMetadataUpdate() throws Exception {
		ArtifactsBusinessLogic testSubject;
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isArtifactMetadataUpdate",
				new Object[] { auditingActionEnum });
	}

	
	@Test
	public void testIsDeploymentArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo =  buildArtifactPayload();
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isDeploymentArtifact", new Object[] { artifactInfo });
	}

	


	
	@Test
	public void testSetArtifactPlaceholderCommonFields() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String resourceId = "";
		
		ArtifactDefinition artifactInfo =  buildArtifactPayload();

		// test 1
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setArtifactPlaceholderCommonFields",
				new Object[] { resourceId, user, artifactInfo });

	}


	@Test
	public void testCreateEsHeatEnvArtifactDataFromString() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactDefinition = buildArtifactPayload();;;
		String payloadStr = "";
		Either<ESArtifactData, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createEsHeatEnvArtifactDataFromString",
				new Object[] { artifactDefinition, payloadStr });
	}

	


	


	
	@Test
	public void testUpdateArtifactOnGroupInstance() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		Component component =  createResourceObject(true);
		String instanceId = "";
		String prevUUID = "";
		ArtifactDefinition artifactInfo = buildArtifactPayload();;;
		Either<ArtifactDefinition, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		prevUUID = "";
		result = Deencapsulation.invoke(testSubject, "updateArtifactOnGroupInstance",
				new Object[] { componentType, component, instanceId, prevUUID, artifactInfo,
						artifactInfo });
	}

	
	@Test
	public void testGenerateHeatEnvPayload() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactDefinition = buildArtifactPayload();;;
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "generateHeatEnvPayload",
				new Object[] { artifactDefinition });
	}



	

	
	@Test
	public void testBuildJsonForUpdateArtifact() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactInfo = buildArtifactPayload();;;
		ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
		List<ArtifactTemplateInfo> updatedRequiredArtifacts = new ArrayList<>();
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildJsonForUpdateArtifact(artifactInfo, artifactGroupType, updatedRequiredArtifacts);
	}

	
	@Test
	public void testBuildJsonForUpdateArtifact_1() throws Exception {
		ArtifactsBusinessLogic testSubject;
		String artifactId = "";
		String artifactName = "";
		String artifactType = "";
		ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.DEPLOYMENT;
		String label = "";
		String displayName = "";
		String description = "";
		byte[] artifactContent = new byte[] { ' ' };
		List<ArtifactTemplateInfo> updatedRequiredArtifacts = new ArrayList<>();
		List<HeatParameterDefinition> heatParameters = new ArrayList<>();
		Map<String, Object> result;

		// test 1
		testSubject = createTestSubject();
		artifactId = "";
		result = testSubject.buildJsonForUpdateArtifact(artifactId, artifactName, artifactType, artifactGroupType,
				label, displayName, description, artifactContent, updatedRequiredArtifacts, heatParameters);
	}

	



	
	@Test
	public void testReplaceCurrHeatValueWithUpdatedValue() throws Exception {
	ArtifactsBusinessLogic testSubject;
	List<HeatParameterDefinition> currentHeatEnvParams = new ArrayList<>();
	List<HeatParameterDefinition> updatedHeatEnvParams = new ArrayList<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "replaceCurrHeatValueWithUpdatedValue", new Object[]{currentHeatEnvParams, updatedHeatEnvParams});
	}

	
	
	@Test
	public void testExtractArtifactDefinition() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifactDefinition = buildArtifactPayload();;
		Either<ArtifactDefinition, Operation> eitherArtifact = Either.left(artifactDefinition);
		ArtifactDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.extractArtifactDefinition(eitherArtifact);
	}



	
	@Test
	public void testSetHeatCurrentValuesOnHeatEnvDefaultValues() throws Exception {
		ArtifactsBusinessLogic testSubject;
		ArtifactDefinition artifact = null;
		ArtifactDefinition artifactInfo = buildArtifactPayload();;;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setHeatCurrentValuesOnHeatEnvDefaultValues",
				new Object[] { artifactInfo, artifactInfo });
	}

	
	@Test
	public void testBuildHeatEnvFileName() throws Exception {
	ArtifactsBusinessLogic testSubject;
	ArtifactDefinition heatArtifact = null;
	ArtifactDefinition artifactInfo = buildArtifactPayload();;;
	Map<String,Object> placeHolderData = new HashMap<>();
	
	
	// default test
	testSubject=createTestSubject();Deencapsulation.invoke(testSubject, "buildHeatEnvFileName", new Object[]{artifactInfo, artifactInfo, placeHolderData});
	}

	
	@Test
	public void testHandleEnvArtifactVersion() throws Exception {
	ArtifactsBusinessLogic testSubject;
	ArtifactDefinition artifactInfo = buildArtifactPayload();;;
	Map<String,String> existingEnvVersions = new HashMap<>();
	
	
	// test 1
	testSubject=createTestSubject();
	Deencapsulation.invoke(testSubject, "handleEnvArtifactVersion", new Object[]{artifactInfo, existingEnvVersions});
	}

	
	@Test
	public void testHandleArtifactsRequestForInnerVfcComponent() throws Exception {
		ArtifactsBusinessLogic testSubject;
		List<ArtifactDefinition> artifactsToHandle = new ArrayList<>();;
		Resource component = createResourceObject(true);;
		
		List<ArtifactDefinition> vfcsNewCreatedArtifacts = new ArrayList<>();;
		ArtifactsBusinessLogic arb = new ArtifactsBusinessLogic();
		ArtifactOperationInfo operation = arb.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE);
		boolean shouldLock = false;
		boolean inTransaction = false;
		Either<List<ArtifactDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.handleArtifactsRequestForInnerVfcComponent(artifactsToHandle, component, user,
				vfcsNewCreatedArtifacts, operation, shouldLock, inTransaction);
	}

	

	

	


	

	
	@Test
	public void testUpdateAuditParametersWithArtifactDefinition() throws Exception {
	ArtifactsBusinessLogic testSubject;
	Map<AuditingFieldsKeysEnum,Object> additionalParams = new HashMap<>();
	ArtifactDefinition artifactInfo = buildArtifactPayload();;;
	
	
	// test 1
	testSubject=createTestSubject();
	Deencapsulation.invoke(testSubject, "updateAuditParametersWithArtifactDefinition", new Object[]{additionalParams, artifactInfo});
	}

	
	@Test
	public void testSetNodeTemplateOperation() throws Exception {
		ArtifactsBusinessLogic testSubject;
		NodeTemplateOperation nodeTemplateOperation = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setNodeTemplateOperation", new Object[] { NodeTemplateOperation.class });
	}
}
