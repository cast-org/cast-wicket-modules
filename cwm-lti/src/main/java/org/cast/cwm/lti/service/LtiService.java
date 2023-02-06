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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.CwmSession;
import org.cast.cwm.data.*;
import org.cast.cwm.db.service.IDBService;
import org.cast.cwm.db.service.IModelProvider;
import org.cast.cwm.lti.ILtiResourceHandler;
import org.cast.cwm.service.IEventService;
import org.cast.cwm.service.ISiteService;
import org.cast.cwm.service.IUserService;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class LtiService implements ILtiService {

    private static final MetaDataKey<DeepLinkingState> LINKING_STATE_ATTRIBUTE = new MetaDataKey<DeepLinkingState>() {
    };

    private static final String PLATFORM = "https://purl.imsglobal.org/spec/lti/claim/tool_platform";

    private static final String MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
    private static final String MESSAGE_TYPE_RESOURCE_REQUEST = "LtiResourceLinkRequest";
    private static final String MESSAGE_TYPE_LINKING_REQUEST = "LtiDeepLinkingRequest";
    private static final String MESSAGE_TYPE_LINKING_RESPONSE = "LtiDeepLinkingResponse";

    // for MESSAGE_TYPE_RESOURCE_REQUEST
    //
    // "https://purl.imsglobal.org/spec/lti/claim/resource_link": {
    //   "id": "74126",
    //   "title": "Local test",
    //   "description": "local test"
    // }
    private static final String RESOURCE_LINK = "https://purl.imsglobal.org/spec/lti/claim/resource_link";

    // for MESSAGE_TYPE_RESOURCE_REQUEST
    //
    // "https://purl.imsglobal.org/spec/lti/claim/custom": {
    //   "challenge": 42
    // }
    private static final String CUSTOM = "https://purl.imsglobal.org/spec/lti/claim/custom";

    // for MESSAGE_TYPE_LINKING_REQUEST
    //
    // "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings": {
    //   "accept_types": [
    //     "link",
    //     "file",
    //     "html",
    //     "ltiResourceLink",
    //     "image"
    //   ],
    //   "accept_media_types": "image/*,text/html",
    //   "accept_presentation_document_targets": [
    //     "iframe",
    //     "window",
    //     "embed"
    //   ],
    //   "accept_multiple": true,
    //   "auto_create": true,
    //   "title": "This is the default title",
    //   "text": "This is the default text",
    //   "data": "Some random opaque data that MUST be sent back",
    //   "deep_link_return_url": "https://lti-ri.imsglobal.org/platforms/3662/contexts/54505/deep_links"
    // }
    private static final String LINKING_SETTINGS = "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings";

    // for MESSAGE_TYPE_LINKING_RESPONSE
    // "https://purl.imsglobal.org/spec/lti-dl/claim/content_items": [
    //   {
    //     "type": "ltiResourceLink",
    //     "title": "My Home Page",
    //     "url": "https://something.example.com/page.html",
    //     "icon": {
    //       "url": "https://lti.example.com/image.jpg",
    //       "width": 100,
    //       "height": 100
    //     },
    //     "thumbnail": {
    //       "url": "https://lti.example.com/thumb.jpg",
    //       "width": 90,
    //       "height": 90
    //     }
    //   }
    // ]
    private static final String LINKING_CONTENT_ITEMS = "https://purl.imsglobal.org/spec/lti-dl/claim/content_items";

    private static final String ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";
    private static final String ROLE_LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Learner";
    private static final String ROLE_INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor";
    private static final String ROLE_MEMBERSHIP_INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";
    private static final String ROLE_MEMBERSHIP_LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";

    private static final String CONTEXT = "https://purl.imsglobal.org/spec/lti/claim/context";

    @Inject
    private IUserService userService;

    @Inject
    private ISiteService siteService;

    @Inject
    private IEventService eventService;

    @Inject
    private IDBService dbService;

    @Inject
    private IModelProvider modelProvider;

    @Inject
    private ILtiResourceHandler hooks;

    @Override
    public String onLaunch(LtiPlatform platform, JsonObject payload) {

        Site site = platform.getSite();
        Period period = initPeriod(payload, site);
        User user = initUser(payload, site, period);
        dbService.flushChanges();
        login(user, period, site);

        String messageType = payload.get(MESSAGE_TYPE).getAsString();
        switch (messageType) {
            case MESSAGE_TYPE_RESOURCE_REQUEST:
                JsonObject custom = payload.getAsJsonObject(CUSTOM);

                return hooks.onResourceRequested(custom);
            case MESSAGE_TYPE_LINKING_REQUEST:
                JsonObject deepLinkingSettings = payload.getAsJsonObject(LINKING_SETTINGS);
                DeepLinkingState state = new DeepLinkingState();
                state.iss = payload.get("iss").getAsString();
                state.aud = payload.get("aud").getAsString();
                state.returnUrl = deepLinkingSettings.get("deep_link_return_url").getAsString();
                if (deepLinkingSettings.get("data") != null) {
                    state.data = deepLinkingSettings.get("data").getAsString();
                }
                CwmSession.get().setMetaData(LINKING_STATE_ATTRIBUTE, state);
                return "/lti/linking";
        }
        throw new IllegalArgumentException(String.format("unrecognized message type '%s'", messageType));
    }

    private void login(User user, Period period, Site site) {
        CwmSession session = CwmSession.get();

        User currentUser = session.getUser();
        if (currentUser == null || !currentUser.getId().equals(user.getId())) {
            session.signOut();
            session.bind();
            session.signIn(user, true);
            eventService.createLoginSession(null);

            session.setCurrentPeriodModel(modelProvider.modelOf(period));
            session.setCurrentSiteModel(modelProvider.modelOf(site));
        }
    }

    private Period initPeriod(JsonObject payload, Site site) {

        JsonObject context = payload.getAsJsonObject(CONTEXT);
        String ltiId = getLtiId(context.get("id"));

        Period period = null;
        for (Period candidate : site.getPeriods()) {
            if (ltiId.equals(candidate.getLtiId())) {
                period = candidate;
                break;
            }
        }
        if (period == null) {
            period = siteService.newPeriod();
            period.setLtiId(ltiId);
            period.setClassId("lti-" + ltiId);
            period.setSite(site);
            site.getPeriods().add(period);
            dbService.save(site);
        }
        period.setName(context.get("title").getAsString());

        return period;
    }

    private User initUser(JsonObject payload, Site site, Period period) {
        String ltiId = getLtiId(payload.get("sub"));
        User user = userService.getBySiteAndLtiId(site, ltiId).getObject();
        if (user == null) {
            user = userService.newUser();
            user.setCreateDate(new Date());
            user.setValid(true);
            user.setPassword(UUID.randomUUID().toString());
            user.setUsername(UUID.randomUUID().toString());
            user.setSubjectId(UUID.randomUUID().toString());
            user.setLtiId(ltiId);
        }
        user.setFirstName(payload.get("given_name").getAsString());
        user.setLastName(payload.get("family_name").getAsString());
        user.setRole(mapRole(payload));
        if (!user.getPeriods().contains(period)) {
            user.getPeriods().add(period);
        }
        dbService.save(user);

        return user;
    }

    private Role mapRole(JsonObject payload) {
        JsonArray roles = payload.getAsJsonArray(ROLES);
        if (roles.contains(new JsonPrimitive(ROLE_LEARNER)) || roles.contains(new JsonPrimitive(ROLE_MEMBERSHIP_LEARNER))) {
            return Role.STUDENT;
        }
        if (roles.contains(new JsonPrimitive(ROLE_INSTRUCTOR)) || roles.contains(new JsonPrimitive(ROLE_MEMBERSHIP_INSTRUCTOR))) {
            return Role.TEACHER;
        }
        return Role.GUEST;
    }

    /**
     * Get the  LTI id.
     *
     * @param id from LTI
     * @return id
     */
    private String getLtiId(JsonElement id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return id.getAsString();
    }

    @Override
    public Response createDeepLinkingResponse(List<?> resources) {

        DeepLinkingState state = getLinkingState();

        JsonObject payload = new JsonObject();
        payload.addProperty("iss", state.aud);
        payload.addProperty("aud", state.iss);
        if (state.data != null) {
            payload.addProperty("https://purl.imsglobal.org/spec/lti-dl/claim/data", state.data);
        }
        payload.addProperty("https://purl.imsglobal.org/spec/lti/claim/version", "1.3.0");
        payload.addProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id", "1");
        payload.addProperty("nonce", UUID.randomUUID().toString());
        payload.addProperty(MESSAGE_TYPE, MESSAGE_TYPE_LINKING_RESPONSE);

        JsonArray items = new JsonArray();
        payload.add(LINKING_CONTENT_ITEMS, items);

        String url = RequestCycle.get().getUrlRenderer().renderFullUrl( Url.parse("/lti/launch"));
        for (Object resource : resources) {
            JsonObject item = new JsonObject();
            item.addProperty("type", "ltiResourceLink");
            item.addProperty("url", url);
            JsonObject custom = new JsonObject();
            item.add("custom", custom);
            hooks.configureDeepLinkResource(resource, item, custom);
            items.add(item);
        }

        Response response = new Response();
        response.url = getLinkingState().returnUrl;
        response.payload = payload;

        return response;
    }

    private DeepLinkingState getLinkingState() {
        DeepLinkingState state = CwmSession.get().getMetaData(LINKING_STATE_ATTRIBUTE);
        if (state == null) {
            throw new IllegalStateException("no linking state");
        }
        return state;
    }

    private static class DeepLinkingState implements Serializable {

        String iss;
        String aud;
        String data;
        String returnUrl;
    }
}
