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
package org.cast.cwm.data;

import lombok.*;
import org.cast.cwm.db.data.PersistedObject;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * A Learning Management System or other platform that can connect to our application via LTI requests.
 * This will end up mapping to a single Site in our database; the LMS is allowed to create and manage
 * Periods and Users within that Site.
 *
 * All of the values stored here should be provided by the platform as part of the setup process.
 *
 * Sample platforms for testing can be created at:
 * <a href="https://lti-ri.imsglobal.org/lti/tools/">IMS Global LTI Test Tool</a>
 *
 * There's some helpful documentation of these values in the
 * <a href="https://docs.blackboard.com/lti/tutorials/implementation-guide">Blackboard LTI Implementation Guide</a>
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@GenericGenerator(name="lti_platform_generator", strategy = "org.cast.cwm.CwmIdGenerator")
public class LtiPlatform extends PersistedObject implements Serializable {

    @Id
    @GeneratedValue(generator = "lti_platform_generator")
    @Getter
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToOne(optional=false)
    private Site site;

    /**
     * The organization responsible for assigning this platform's client ID.
     * May be something like 'https://canvas.instructure.com'
     */
    private String issuer;

    /**
     * The ID that the platform provides to identify itself.
     * This is unique in combination with the issuer.
     * Can be any arbitrary string value.
     */
    private String clientId;

    /**
     * URL we will use to validate incoming LTI message signatures.
     */
    private String publicJwksUrl;

    /**
     * The URL we will redirect back to after a successful login.
     */
    private String oidcAuthRequestUrl;

    /**
     * The URL of the platform's OAuth 2 token issuer, used for LTI service calls.
     */
    private String oAuth2TokenUrl;

    /**
     * Identifier of the specific deployment within the clientId.
     */
    private String deploymentId;

    public LtiPlatform(Site site) {
        this.site = site;
    }
}