/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ImageRequestDtoTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ImageRequestDto.class, hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidToString() {
        assertThat(ImageRequestDto.class, hasValidBeanToString());
    }

    @Test
    public void shouldHaveEquals() {
        assertThat(ImageRequestDto.class, hasValidBeanEquals());
    }

    @Test
    public void shouldHaveHashCode() {
        assertThat(ImageRequestDto.class, hasValidBeanHashCode());
    }
}