package org.cast.cwm.lti;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.wicket.injection.Injector;
import org.cast.cwm.data.LtiPlatform;
import org.cast.cwm.lti.service.IJwtSigningService;
import org.cast.cwm.lti.service.ILtiService;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@Slf4j
public class LtiSender<T> {

    private static final String SCORES_SUFFIX = "/scores";

    @Inject
    private ILtiService ltiService;

    @Inject
    private IJwtSigningService signingService;

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    private JsonParser parser = new JsonParser();

    public LtiSender() {
        Injector.get().inject(this);
    }

    /**
     * @link https://canvas.instructure.com/doc/api/file.oauth_endpoints.html#post-login-oauth2-token
     */
    protected String createToken(LtiPlatform platform) throws IOException {

        Instant now = Instant.now();
        JsonObject payload = new JsonObject();
        payload.addProperty("iss", platform.getClientId()); // https://stemfolio ???
        payload.addProperty("sub", platform.getClientId());
        payload.addProperty("aud", platform.getOAuth2TokenUrl());
        payload.addProperty("iat", now.getEpochSecond());
        payload.addProperty("exp", now.plusSeconds(60*60).getEpochSecond());
        payload.addProperty("jti", UUID.randomUUID().toString());

        if (log.isInfoEnabled()) {
            log.info(new GsonBuilder().setPrettyPrinting().create().toJson(payload));
        }

        String clientAssertion = signingService.sign(payload);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        parameters.add(new BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"));
        parameters.add(new BasicNameValuePair("client_assertion", clientAssertion));
        parameters.add(new BasicNameValuePair("scope", "https://purl.imsglobal.org/spec/lti-ags/scope/score"));

        ClassicHttpRequest post = ClassicRequestBuilder
                .post(platform.getOAuth2TokenUrl())
                .addHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .setEntity(new UrlEncodedFormEntity(parameters))
                .build();

        String body = httpClient.execute(post, this::handleResponse);
        JsonObject json = (JsonObject) parser.parse(body);
        return json.get("access_token").getAsString();
    }

    protected void send(String url, String token, String content) throws IOException {
        ClassicHttpRequest post = ClassicRequestBuilder
                .post(url)
                .addHeader("Authorization", "Bearer " + token)
                .setEntity(content, ContentType.parse("application/vnd.ims.lis.v1.score+json"))
                .build();

        httpClient.execute(post, this::handleResponse);
    }

    public void sendScore(T resource) {

        ILtiService.Request request;
        try {
            request = ltiService.giveScore(resource);
        } catch (IllegalStateException ex) {
            log.warn("could not send score, no resource state available");
            return;
        }

        String json = new GsonBuilder().setPrettyPrinting().create().toJson(request.payload);
        if (log.isInfoEnabled()) {
            log.info(json);
        }

        try {
            String token = createToken(request.platform);
            log.debug("created token");

            send(request.url + SCORES_SUFFIX, token, json);
            log.debug("sent score");
        } catch (IOException ex) {
            log.warn("could not send score", ex);
        }
    }

    private String handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
        log.debug("requested token {}", response.getCode());

        String body = EntityUtils.toString(response.getEntity());
        if (response.getCode() != HttpStatus.SC_CREATED) {
            log.error("request failed", body);
            throw new IOException("request failed " + response.getCode());
        }

        return body;
    }
}
