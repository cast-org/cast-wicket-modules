package org.cast.cwm.lti.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import lombok.extern.slf4j.Slf4j;
import org.cast.cwm.service.ISiteService;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class JwtValidationService implements IJwtValidationService {

    @Inject
    private ISiteService siteService;

    private JsonParser parser = new JsonParser();

    @Override
    public Result validate(String token) {
        Result entry = new Result();

        JWTClaimsSet claims;
        try {
            ConfigurableJWTProcessor<Result> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWTClaimsSetAwareJWSKeySelector(new KeySelector());

            claims = jwtProcessor.process(token, entry);
        } catch (Exception e) {
            throw new IllegalStateException("JWT validation failure", e);
        }

        entry.payload = (JsonObject) parser.parse(claims.toPayload().toString());
        return entry;
    }

    private class KeySelector implements JWTClaimsSetAwareJWSKeySelector<Result> {

        private ConcurrentMap<String, JWKSource<SecurityContext>> keyByIssuer = new ConcurrentHashMap<>();

        @Override
        public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, Result entry) throws KeySourceException {

            String issuer = claimsSet.getIssuer();
            String clientId = claimsSet.getAudience().get(0);

            JWKSource<SecurityContext> source = keyByIssuer.computeIfAbsent(issuer + clientId, x -> {
                entry.platform = siteService.getPlatformByIssuerAndClientId(issuer, clientId).getObject();

                String publicJwksUrl = entry.platform.getPublicJwksUrl();

                try {
                    return new RemoteJWKSet<>(new URL(publicJwksUrl));
                } catch (MalformedURLException ex) {
                    throw new IllegalStateException(String.format("public jwks url is invalid ''", publicJwksUrl), ex);
                }
            });

            // note: the matcher selects the correct key based on the kid
            JWKMatcher matcher = JWKMatcher.forJWSHeader(header);
            List<JWK> matches = source.get(new JWKSelector(matcher), entry);
            return KeyConverter.toJavaKeys(matches);
        }
    }
}