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
package org.cast.cwm.lti.service;

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
import org.cast.cwm.data.LtiPlatform;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 */
@Slf4j
public class LtiSender extends Thread implements ILtiSender {

    @Inject
    private ILtiService ltiService;

    @Inject
    private IJwtSigningService signingService;

    private BlockingQueue<ILtiService.Request> queue = new LinkedBlockingQueue<>();

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    private JsonParser parser = new JsonParser();

    public LtiSender() {
        setDaemon(true);
    }

    @Override
    public void run() {

        try {
            do {
                ILtiService.Request request = queue.take(); // block until a message is ready to go.
                deliver(request);
            } while (!this.isInterrupted());
        } catch (InterruptedException e) {
            // when interrupted, shuts down.
        }
        log.warn("EmailSender thread Interrupted, exiting");
    }

    public <R> void sendScore(R resourceResponse) {

        ILtiService.Request request;
        try {
            request = ltiService.giveScore(resourceResponse);
        } catch (IllegalStateException ex) {
            log.warn("could not send score, no resource state available");
            return;
        }

        queue.add(request);

        if (!this.isAlive()) {
            this.start();
        }
    }

    private void deliver(ILtiService.Request request) {

        try {
            log.debug("delivering");
            String token = createToken(request.platform);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(request.payload);

            send(request.url, token, json);
        } catch (IOException ex) {
            log.error("deliver failed", ex);
        }
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

        if (log.isDebugEnabled()) {
            log.debug("creating token {}", new GsonBuilder().setPrettyPrinting().create().toJson(payload));
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

        log.debug("created token {}", body);

        JsonObject json = (JsonObject) parser.parse(body);
        return json.get("access_token").getAsString();
    }

    protected void send(String url, String token, String content) throws IOException {
        log.debug("sending {}", content);

        ClassicHttpRequest post = ClassicRequestBuilder
                .post(url)
                .addHeader("Authorization", "Bearer " + token)
                .setEntity(content, ContentType.parse("application/vnd.ims.lis.v1.score+json"))
                .build();

        httpClient.execute(post, this::handleResponse);

        log.debug("sent");
    }

    /**
     * Handles the response. Any code not {@link HttpStatus#SC_CREATED} or {@link HttpStatus#SC_OK}
     * is considered a failure.
     *
     * @param response
     */
    private String handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
        log.debug("handling response {}", response.getCode());

        String body = EntityUtils.toString(response.getEntity());
        if (response.getCode() != HttpStatus.SC_CREATED && response.getCode() != HttpStatus.SC_OK) {
            log.error("request failed", body);
            throw new IOException("request failed " + response.getCode());
        }

        return body;
    }
}
