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

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.LtiPlatform;
import org.cast.cwm.lti.service.ILtiService;
import org.cast.cwm.service.ISiteService;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler the initiation of a LTI request (preflight).
 */
@Slf4j
public class LtiInitiation implements IRequestHandler {

    private static final MetaDataKey<String> NONCE = new MetaDataKey<String>() {
    };

    @Inject
    private ISiteService siteService;

    @Inject
    private ILtiService ltiService;

    private String iss;
    private String loginHint;
    private String targetLinkUri;
    private String ltiMessageHint;
    private String clientId;
    private String ltiDeploymentId;

    LtiInitiation() {
        Injector.get().inject(this);

        IRequestParameters params = RequestCycle.get().getRequest().getRequestParameters();

        iss = params.getParameterValue("iss").toString();
        loginHint = params.getParameterValue("login_hint").toString();
        targetLinkUri = params.getParameterValue("target_link_uri").toString();
        ltiMessageHint = params.getParameterValue("lti_message_hint").toString();
        clientId = params.getParameterValue("client_id").toString();
        ltiDeploymentId = params.getParameterValue("lti_deployment_id").toString();
    }

    @Override
    public void respond(IRequestCycle requestCycle) {
        WebResponse response = (WebResponse)requestCycle.getResponse();

        LtiPlatform platform = siteService.getPlatformByIssuerAndClientId(iss, clientId).getObject();
        if (platform == null) {
            log.warn("No LTI platform found for issuer {} and clientId {}", iss, clientId);
            response.sendError(HttpsURLConnection.HTTP_UNAUTHORIZED, "Unknown LTI issuer and client ID");
            return;
        }
        if (ltiDeploymentId != null) {
            if (!platform.getDeploymentId().equals(ltiDeploymentId)) {
                log.warn("invalid deployment id {}; expected {}", ltiDeploymentId, platform.getDeploymentId());
                response.sendError(HttpsURLConnection.HTTP_BAD_REQUEST, "invalid payload");
                return;
            }
        }

        Url url = Url.parse(platform.getOidcAuthRequestUrl(), StandardCharsets.UTF_8, true);

        Map<String, String> map = new HashMap<>();

        map.put("response_type", "id_token");
        map.put("redirect_uri", targetLinkUri);
        map.put("response_mode", "form_post");
        map.put("client_id", clientId);
        map.put("scope", "openid");
        map.put("state", "state");
        map.put("login_hint", loginHint);
        map.put("lti_message_hint", ltiMessageHint);
        map.put("prompt", "none");
        map.put("nonce", newNonce());

        map.forEach(url::addQueryParameter);
        log.debug("LTI initiation from known client; sending redirect to {}", url.toString(Url.StringMode.FULL));
        response.sendRedirect(url.toString(Url.StringMode.FULL));
    }

    private static String newNonce() {
        String nonce = UUID.randomUUID().toString();
        CwmSession.get().setMetaData(NONCE, nonce);
        return nonce;
    }

    public static void checkNonce(String nonce) {
        String knownNonce = CwmSession.get().getMetaData(NONCE);
        if (knownNonce == null) {
            throw new IllegalStateException("no nonce");
        }
        if (!knownNonce.equals(nonce)) {
            throw new IllegalArgumentException(String.format("wrong nonce '%s'", nonce));
        }
    }
}
