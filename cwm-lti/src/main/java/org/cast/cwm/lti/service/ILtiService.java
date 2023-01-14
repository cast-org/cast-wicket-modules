package org.cast.cwm.lti.service;

import com.google.gson.JsonObject;
import org.cast.cwm.data.LtiPlatform;

import java.util.List;

/**
 * Service for LTI resources.
 */
public interface ILtiService {

    /**
     * Handle the launch of a resource.
     *
     * @param platform the launching platform
     * @param payload the payload of the LTI request
     * @return redirect url
     */
    String onLaunch(LtiPlatform platform, JsonObject payload);

    /**
     * Create a response for deep linking of resources.
     *
     * @param resources resources to deep link to
     * @return payload
     */
    Response createDeepLinkingResponse(List<?> resources);

    class Response {

        public String url;

        public JsonObject payload;
    }
}
