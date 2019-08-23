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

package org.openecomp.sdc.be.servlets;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.mixin.GroupTypeMixin;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.stereotype.Controller;
import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "group types resource"))
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupTypesEndpoint {

    private final GroupTypeBusinessLogic groupTypeBusinessLogic;

    public GroupTypesEndpoint(GroupTypeBusinessLogic groupTypeBusinessLogic) {
        this.groupTypeBusinessLogic = groupTypeBusinessLogic;
    }

    @GET
    @Path("/groupTypes")
    @Operation(description = "Get group types ", method = "GET", summary = "Returns group types",
            responses = @ApiResponse(content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = GroupTypeDefinition.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "group types found"), @ApiResponse(
            responseCode = "400",
            description = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "500", description = "Internal Error")})
    @ResponseView(mixin = {GroupTypeMixin.class})
    public List<GroupTypeDefinition> getGroupTypes(@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Parameter(
                    description = "An optional parameter to indicate the type of the container from where this call is executed") @QueryParam("internalComponentType") String internalComponentType) {
        return groupTypeBusinessLogic.getAllGroupTypes(userId, internalComponentType);
    }

}
