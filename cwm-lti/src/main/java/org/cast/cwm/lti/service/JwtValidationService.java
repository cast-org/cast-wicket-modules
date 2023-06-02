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
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyConverter;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import lombok.extern.slf4j.Slf4j;
import org.cast.cwm.data.LtiPlatform;
import org.cast.cwm.service.ISiteService;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class JwtValidationService implements IJwtValidationService {

    @Inject
    private ISiteService siteService;

    private JsonParser parser = new JsonParser();

    @Override
    public JsonObject validate(String token) {
        JWTClaimsSet claims;
        try {
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(new KeySelector());

            claims = jwtProcessor.process(token, new SimpleSecurityContext());
        } catch (Exception e) {
            throw new IllegalStateException("JWT validation failure", e);
        }

        return (JsonObject) parser.parse(claims.toPayload().toString());
    }

    private class KeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

        private ConcurrentMap<String, JWKSource<SecurityContext>> jwkSetCache = new ConcurrentHashMap<>();

        @Override
        public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context) throws KeySourceException {

            final String issuer = claimsSet.getIssuer();
            final String clientId = claimsSet.getAudience().get(0);

            JWKSource<SecurityContext> source = jwkSetCache.computeIfAbsent(issuer + clientId, x -> {
                String publicJwksUrl = getJwkSetUrl(issuer, clientId);

                try {
                    return new RemoteJWKSet<>(new URL(publicJwksUrl));
                } catch (MalformedURLException ex) {
                    throw new IllegalStateException(String.format("jwks url is invalid '%s'", publicJwksUrl), ex);
                }
            });

            // note: the matcher selects the correct key based on the kid
            JWKMatcher matcher = JWKMatcher.forJWSHeader(header);
            List<JWK> matches = source.get(new JWKSelector(matcher), context);
            return KeyConverter.toJavaKeys(matches);
        }
    }

    private String getJwkSetUrl(String issuer, String clientId) {
        for (LtiPlatform platform : siteService.listPlatforms().getObject()) {
            if (Objects.equals(platform.getIssuer(), issuer) &&
                    Objects.equals(platform.getClientId(), clientId)) {
                // note: multiple platforms could use the same issuer and clientId,
                // while differing in the deploymentId only - we take the first
                // jwkSet that is available (which should be identical for all of them)
                return platform.getPublicJwksUrl();
            }
        }
        // no platform has a jwkSet for the given issuer and clientId
        throw new IllegalStateException(String.format("no jwks url for issuer %s clientId %s", issuer, clientId));
    }
}
