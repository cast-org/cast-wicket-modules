package org.cast.cwm.lti.service;

import com.google.gson.JsonObject;

public interface IJwtSigningService {

    /**
     * Sign payload.
     *
     * @param payload
     * @return token
     */
    String sign(JsonObject payload);
}
