package org.cast.cwm.data;

import lombok.*;
import org.cast.cwm.db.data.PersistedObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
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
     * Identifier of the specific deployment within the clientId.
     */
    private String deploymentId;
};
