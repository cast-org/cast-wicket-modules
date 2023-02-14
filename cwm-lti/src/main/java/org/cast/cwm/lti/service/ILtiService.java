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

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
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
     * @return response
     */
    Request respondDeepLinking(String launchUrl, List<?> resources);

    /**
     * Create a response for a score of a resource.
     *
     * @param resource
     * @return response
     *
     * @link https://canvas.instructure.com/doc/api/score.html#method.lti/ims/scores.create
     */
    Request giveScore(Object resource);

    /**
     * A request to a {@link LtiPlatform}.
     */
    @AllArgsConstructor
    class Request {

        public final LtiPlatform platform;

        public final String url;

        public final JsonObject payload;
    }
}
