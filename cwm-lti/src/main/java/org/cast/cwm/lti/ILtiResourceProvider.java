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

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;

/**
 * Provider for application-specific resources to an LTI platform.
 *
 * @param <T> type of resource
 */
public interface ILtiResourceProvider<T> {

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
     * Deep Linking was requested.
     *
     * @return url to deep linking page
     */
    String onDeepLinkingRequested();

    /**
     * Configure a deep-link response for a single resource:
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

    /**
     * Get a score of a resource.
     *
     * @param resource
     * @return score
     */
    default Score getScore(T resource) {
        return new Score(1.0f, "", ActivityProgress.Initialized, GradingProgress.NotReady);
    }

    @AllArgsConstructor
    class Score {

        public final float scoreGiven;
        public String comment;
        public final ActivityProgress activityProgress;
        public final GradingProgress gradingProgress;
    }

    enum ActivityProgress {
        Initialized,
        Started,
        InProgress,
        Submitted,
        Completed
    }

    enum GradingProgress {
        FullyGraded,
        Pending,
        PendingManual,
        Failed,
        NotReady
    }
}