/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter.impl.pnfd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mockit.Deencapsulation;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.impl.pnfd.parser.PnfdCustomNodeTypeBlockParser;
import org.openecomp.core.converter.pnfd.PnfdTransformationEngine;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;

@RunWith(Parameterized.class)
public class PnfTransformationEngineTest {

    public static final String INPUT_DIR = "pnfDescriptor/in/";
    public static final String OUTPUT_DIR = "pnfDescriptor/out/";
    private String inputFilename;
    private final YamlUtil yamlUtil = new YamlUtil();
    private final ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();

    public PnfTransformationEngineTest(final String inputFilename) {
        this.inputFilename = inputFilename;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<String> input() throws IOException {
        try (final Stream<Path> files = Files.list(getPathFromClasspath(INPUT_DIR))) {
            return files.map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void testTopologyTemplateConversions() throws IOException {
        final byte[] descriptor = getInputFileResource(inputFilename);
        final ServiceTemplateReaderService serviceTemplateReaderService = new ServiceTemplateReaderServiceImpl(descriptor);
        final ServiceTemplate serviceTemplate = new ServiceTemplate();

        PnfdTransformationEngine pnfdTransformationEngine = new PnfdNodeTypeTransformationEngine(
            serviceTemplateReaderService, serviceTemplate);
        pnfdTransformationEngine.transform();
        pnfdTransformationEngine = new PnfdNodeTemplateTransformationEngine(serviceTemplateReaderService, serviceTemplate);
        pnfdTransformationEngine.transform();

        final String yamlContent = yamlUtil.objectToYaml(serviceTemplate);
        final String result = yamlUtil.objectToYaml(yamlUtil.yamlToObject(yamlContent, ServiceTemplate.class));
        final String expectedResult = getExpectedResultFor(inputFilename);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testBuildParseBlock() {
        final PnfdCustomNodeTypeBlockParser blockParser = spy(new PnfdCustomNodeTypeBlockParser(null));
        final ConversionDefinition conversionDefinition = Mockito.mock(ConversionDefinition.class);
        final Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("type", null);
        stringObjectMap.put("name", null);
        assertEquals(Optional.empty(), Deencapsulation.invoke(blockParser, "buildParsedBlock",
            stringObjectMap, stringObjectMap, conversionDefinition));
    }

    @Test
    public void testReadDefinition() {
        final PnfdTransformationEngine engine = spy(
            new PnfdNodeTemplateTransformationEngine(null, null, "test.txt"));
        Deencapsulation.invoke(engine, "readDefinition");
        assertNull(Deencapsulation.getField(engine, "transformationDescription"));
    }

    private String getExpectedResultFor(final String inputFilename) throws IOException {
        try (final InputStream inputStream = getOutputFileResourceCorrespondingTo(inputFilename)) {
            final ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(inputStream, ServiceTemplate.class);
            return yamlUtil.objectToYaml(serviceTemplate);
        }
    }

    private static Path getPathFromClasspath(final String location) {
        return Paths.get(Thread.currentThread().getContextClassLoader().getResource(location).getPath());
    }

    private byte[] getInputFileResource(final String inputFilename) throws IOException {
        return getFileResource(INPUT_DIR + inputFilename);
    }

    private InputStream getOutputFileResourceCorrespondingTo(final String inputFilename) {
        final String outputFilename = getOutputFilenameFrom(inputFilename);
        return getFileResourceAsInputStream(OUTPUT_DIR + outputFilename);
    }

    private String getOutputFilenameFrom(final String inputFilename) {
        return inputFilename.replace("pnfDescriptor", "topologyTemplate");
    }

    private byte[] getFileResource(final String filePath) throws IOException {
        try (final InputStream inputStream = getFileResourceAsInputStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private InputStream getFileResourceAsInputStream(final String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

}