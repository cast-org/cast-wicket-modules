package org.cast.cwm.lti.service;

import com.google.gson.JsonObject;
import com.nimbusds.jose.proc.SecurityContext;
import org.cast.cwm.data.LtiPlatform;

public interface IJwtValidationService {

    /**
     * Validate token.
     *
     * @param token
     * @return validation result
     */
    Result validate(String token);

    /**
     * Result of validation.
     */
    class Result implements SecurityContext {
        /**
         * The launching platform.
         */
        public LtiPlatform platform;

        /**
         * The validated payload.
         */
        public JsonObject payload;
    }
}
