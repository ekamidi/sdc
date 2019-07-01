/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.testng.Assert;

public class ServiceArtifactValidationTaskTest {

	private ServiceArtifactValidationTask createTestSubject() {
		ArtifactValidationUtils artifactValidationUtilsMock = mock(ArtifactValidationUtils.class);
		return new ServiceArtifactValidationTask(artifactValidationUtilsMock);
	}

	@Test
	public void testValidate() throws Exception {
		ServiceArtifactValidationTask testSubject;
		GraphVertex vertex = null;
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertex);
	}

}
