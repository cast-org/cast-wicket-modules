package org.cast.cwm.lti;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import lombok.RequiredArgsConstructor;
import org.cast.cwm.lti.service.*;

/**
 * Module for all services related to LTI.
 * <p>
 * Has to be configured for the application with a {@link ILtiResourceHandler}  via:
 * <pre>
 *   modules.add(new LtiModule(resourceHandler));
 * </pre>
 */
@RequiredArgsConstructor
public class LtiModule implements Module {

    private final ILtiResourceHandler handler;

    @Override
    public void configure(Binder binder) {
        binder.bind(ILtiResourceHandler.class).toProvider(() -> handler).in(Scopes.SINGLETON);

        binder.bind(ILtiService.class).to(LtiService.class).in(Scopes.SINGLETON);
        binder.bind(IJwtValidationService.class).to(JwtValidationService.class).in(Scopes.SINGLETON);
        binder.bind(IJwtSigningService.class).to(JwtSigningService.class).in(Scopes.SINGLETON);
    }
}
