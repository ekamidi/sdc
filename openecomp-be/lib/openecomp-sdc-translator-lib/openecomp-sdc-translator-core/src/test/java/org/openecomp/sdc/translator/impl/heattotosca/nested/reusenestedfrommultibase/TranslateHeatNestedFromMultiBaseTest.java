/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */


package org.openecomp.sdc.translator.impl.heattotosca.nested.reusenestedfrommultibase;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

public class TranslateHeatNestedFromMultiBaseTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/reusenestedfrommultibase/inputs";
    outputFilesPath = "/mock/heat/nested/reusenestedfrommultibase/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
    validateNestedTemplateConsolidationData(TestConstants.TEST_MULTIPLE_NESTED_RESOURCE);
  }

}
