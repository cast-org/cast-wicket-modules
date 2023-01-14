package org.cast.cwm.lti;

import com.google.gson.JsonObject;

public interface ILtiResourceHandler<T> {

    /**
     * A resource was requested:
     *  "https://purl.imsglobal.org/spec/lti/claim/custom": {
     *    "a-resource-identifier": "12345"
     *  }
     *
     * @param custom the custom element of the request payload
     * @return url to resource
     */
    String onResourceRequested(JsonObject custom);

    /**
     * Configure a resource to deep link to:
     * <pre>
     *   {
     *     "title": "A title",
     *     "custom": {
     *       "a-resource-identifier": "12345",
     *     }
     *   }
     * </pre>
     * Implementation should at least set a title and a custom attribute identifying the resource
     *
     * @param resource the resource to deep link to
     * @param item the item element for the resource
     * @param custom the nested custom element for the resource
     */
    void configureDeepLinkResource(T resource, JsonObject item, JsonObject custom);
}
