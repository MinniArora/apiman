/*
 * Copyright 2014 JBoss Inc
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

package org.overlord.apiman.dt.api.rest.contract;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.overlord.apiman.dt.api.beans.idm.UserPermissionsBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;

/**
 * The Permissions API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("permissions")
public interface IPermissionsResource {

    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserPermissionsBean getPermissionsForUser(@PathParam("userId") String userId) throws UserNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserPermissionsBean getPermissionsForCurrentUser() throws UserNotFoundException;
    
}