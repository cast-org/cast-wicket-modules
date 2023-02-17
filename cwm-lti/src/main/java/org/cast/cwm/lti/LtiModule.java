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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import lombok.RequiredArgsConstructor;
import org.cast.cwm.lti.service.*;

/**
 * Module for all services related to LTI.
 * <p>
 * Has to be configured for the application with a {@link ILtiResourceProvider} via:
 * <pre>
 *   modules.add(new LtiModule(resourceHandler));
 * </pre>
 */
@RequiredArgsConstructor
public class LtiModule implements Module {

    private final ILtiResourceProvider provider;

    @Override
    public void configure(Binder binder) {
        binder.bind(ILtiResourceProvider.class).toProvider(() -> provider).in(Scopes.SINGLETON);

        binder.bind(ILtiService.class).to(LtiService.class).in(Scopes.SINGLETON);
        binder.bind(IJwtValidationService.class).to(JwtValidationService.class).in(Scopes.SINGLETON);
        binder.bind(IJwtSigningService.class).to(JwtSigningService.class).in(Scopes.SINGLETON);
        binder.bind(ILtiSender.class).to(LtiSender.class).in(Scopes.SINGLETON);
    }
}
