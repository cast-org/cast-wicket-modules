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

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.cast.cwm.lti.service.IJwtSigningService;

/**
 * A mapper of LTI requests.
 * <p>
 * Has to be mounted on the application via:
 * <pre>
 *     mount(new LtiRequestMapper());
 * </pre>
 */
@Slf4j
public class LtiRequestMapper extends AbstractMapper {

    @Inject
    private IJwtSigningService jwtService;

    public LtiRequestMapper() {
        Injector.get().inject(this);
    }

    @Override
    public IRequestHandler mapRequest(Request request) {
        if (urlStartsWith(request.getUrl(), "lti", "initiate")) {
            return new LtiInitiation();
        } else if (urlStartsWith(request.getUrl(), "lti", "launch")) {
            return new LtiLaunch();
        }
        return null;
    }

    @Override
    public int getCompatibilityScore(Request request) {
        return urlStartsWith(request.getUrl(), "lti") ? Integer.MAX_VALUE : 0;
    }

    @Override
    public Url mapHandler(IRequestHandler requestHandler) {
        if (requestHandler instanceof LtiInitiation) {
            return Url.parse("lti/initiate");
        } else if (requestHandler instanceof LtiLaunch) {
            return Url.parse("lti/launch");
        }
        return null;
    }
}
