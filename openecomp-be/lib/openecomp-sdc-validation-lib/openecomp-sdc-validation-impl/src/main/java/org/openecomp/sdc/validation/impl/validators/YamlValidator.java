/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.validation.impl.validators;

import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.impl.util.YamlValidatorUtil;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class YamlValidator implements Validator {
  private static final ErrorMessageCode ERROR_CODE_YML_1 = new ErrorMessageCode("YML1");
  private static final ErrorMessageCode ERROR_CODE_YML_2 = new ErrorMessageCode("YML2");

  @Override
  public void validate(GlobalValidationContext globalContext) {
    Collection<String> files = globalContext.files(
        (fileName, globalValidationContext) -> fileName.endsWith(".yaml")
            || fileName.endsWith(".yml") || fileName.endsWith(".env"));

    files.stream().forEach(fileName -> validate(fileName, globalContext));
  }

  private void validate(String fileName, GlobalValidationContext globalContext) {
    Optional<InputStream> rowContent = globalContext.getFileContent(fileName);
    if (!rowContent.isPresent()) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_YML_1, Messages
                      .INVALID_YAML_FORMAT_REASON.getErrorMessage(),
                  Messages.EMPTY_YAML_FILE.getErrorMessage()));
      return; /* no need to continue validation */
    }

    try {
      convert(rowContent.get(), Map.class);
    } catch (Exception exception) {

      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_YML_2, Messages
                      .INVALID_YAML_FORMAT_REASON.getErrorMessage(),
                  YamlValidatorUtil.getParserExceptionReason(exception)));
    }
  }

  private <T> T convert(InputStream content, Class<T> type) {
    return new YamlUtil().yamlToObject(content, type);
  }
}
