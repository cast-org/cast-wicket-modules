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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cast.cwm.IAppConfiguration;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;

@Slf4j
public class JwtSigningService implements IJwtSigningService {

    private static final String KEY_ID = "stemfolio";

    private RSASSASigner signer;

    @Inject
    public JwtSigningService(IAppConfiguration appConfiguration) {

        File file = appConfiguration.getOptionalFile("jwt.signing.privateKey");
        if (file == null) {
            log.info("JWT signing disabled, since no private key is configured");
        } else {
            try {
                String privateKey = FileUtils.readFileToString(file, Charset.defaultCharset());

                JWK jwk = JWK.parseFromPEMEncodedObjects(privateKey);

                signer = new RSASSASigner(jwk.toRSAKey());
            } catch (Exception ex) {
                throw new Error("JWT private key invalid", ex);
            }

            log.info("JWT signing enabled");
        }
    }

    @Override
    public String sign(JsonObject payload) {
        if (signer == null) {
            throw new IllegalStateException("JWT signing is disabled");
        }

        String json = new GsonBuilder().create().toJson(payload);

        JWSObject signedJWT;
        try {
            signedJWT = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(),
                    new Payload(json));

            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT signing failure", e);
        }

        return signedJWT.serialize();
    }

    /**
     * Generate a random RSA public/private key pair.
     * <p>
     * Writes the private key, the public key and a <em>JWT Key Set</em> containing
     * the public key to standard-out.
     */
    public static void main(String[] args) throws Exception {
        RSAKey jwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(KEY_ID)
                .generate();

        System.out.println("-----BEGIN PRIVATE KEY-----");
        System.out.println(Base64.getEncoder().encodeToString(jwk.toRSAPrivateKey().getEncoded()));
        System.out.println("-----END PRIVATE KEY-----");
        System.out.println();
        System.out.println("-----BEGIN PUBLIC KEY-----");
        System.out.println(Base64.getEncoder().encodeToString(jwk.toRSAPublicKey().getEncoded()));
        System.out.println("-----END PUBLIC KEY-----");
        System.out.println();

        JsonParser parser = new JsonParser();
        JsonObject key = (JsonObject) parser.parse(jwk.toPublicJWK().toString());
        JsonArray keys = new JsonArray();
        keys.add(key);
        JsonObject root = new JsonObject();
        root.add("keys", keys);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }
}
