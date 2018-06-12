/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general;

/**
 * Created by TALIO on 11/22/2016.
 */
public class General {
    private Hypervisor hypervisor;
    private Image image;
    protected Recovery recovery;
    private String dnsConfiguration;
    private String vmCloneUsage;
    private String nfcNamingCode;
    private String nfcFunction;

    public Hypervisor getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(Hypervisor hypervisor) {
        this.hypervisor = hypervisor;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Recovery getRecovery() {
        return recovery;
    }

    public void setRecovery(Recovery recovery) {
        this.recovery = recovery;
    }

    public String getDnsConfiguration() {
        return dnsConfiguration;
    }

    public void setDnsConfiguration(String dnsConfiguration) {
        this.dnsConfiguration = dnsConfiguration;
    }

    public String getVmCloneUsage() {
        return vmCloneUsage;
    }

    public void setVmCloneUsage(String vmCloneUsage) {
        this.vmCloneUsage = vmCloneUsage;
    }

    public String getNfcNamingCode() {
        return nfcNamingCode;
    }

    public void setNfcNamingCode(String nfcNamingCode) {
        this.nfcNamingCode = nfcNamingCode;
    }

    public String getNfcFunction() {
        return nfcFunction;
    }

    public void setNfcFunction(String nfcFunction) {
        this.nfcFunction = nfcFunction;
    }
}
