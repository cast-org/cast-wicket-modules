/*
 * Copyright 2011-2019 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.lti;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.cast.cwm.lti.service.IJwtValidationService;
import org.cast.cwm.lti.service.ILtiService;

import javax.inject.Inject;

/**
 * Handler for LTI launches.
 */
@Slf4j
public class LtiLaunch implements IRequestHandler {

    @Inject
    private IJwtValidationService validationService;

    @Inject
    private ILtiService ltiService;

    private final String idToken;
    private final String state;

    public LtiLaunch() {
        Injector.get().inject(this);

        IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();
        log.debug("Request parameters in LTI Launch: {}", params.getParameterNames());

        idToken = params.getParameterValue("id_token").toString();
        if (Strings.isNullOrEmpty(idToken)) {
            log.error("id_token is empty, won't be able to validate");
        } else {
            log.debug("id_token: {}", idToken);
        }
        state = params.getParameterValue("state").toString();
    }

    @Override
    public void respond(IRequestCycle requestCycle) {
        WebResponse response = (WebResponse)requestCycle.getResponse();

        JsonObject validated;
        try {
            validated = validationService.validate(idToken);
        } catch (Exception e) {
            log.info("invalid id token {}", e.getMessage());
            e.printStackTrace();
            response.sendError(401, "LTI launch: invalid id token");
            return;
        }

        if (log.isDebugEnabled()) {
            log.info("Successfully validated LTI request: {}",
                    new GsonBuilder().setPrettyPrinting().create().toJson(validated));
        }

        try {
            LtiInitiation.checkNonce(validated.get("nonce").getAsString());
        } catch (Exception e) {
            log.info("check nonce: {}", e.getMessage(), e);
            response.sendError(401, "LTI launch: invalid nonce");
            return;
        }

        String redirect;
        try {
            redirect = ltiService.onLaunch(validated);
        } catch (Exception e) {
            log.info("invalid payload {}", e.getMessage(), e);
            response.sendError(400, "LTI launch: invalid payload");
            return;
        }

        log.debug("LTI Launch successful, redirecting to {}", redirect);
        response.sendRedirect(redirect);
    }
}