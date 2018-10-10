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

package org.openecomp.sdc.versioning.types;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VersionInfo {
  private Version activeVersion;
  private Version latestFinalVersion;
  private List<Version> viewableVersions = new ArrayList<>();
  private List<Version> finalVersions = new ArrayList<>();
  private VersionStatus status;
  private String lockingUser;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("VersionInfo{");
    sb.append("activeVersion=").append(activeVersion);
    sb.append(", latestFinalVersion=").append(latestFinalVersion);
    sb.append(", viewableVersions=").append(viewableVersions);
    sb.append(", finalVersions=").append(finalVersions);
    sb.append(", status=").append(status);
    sb.append(", lockingUser='").append(lockingUser).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
