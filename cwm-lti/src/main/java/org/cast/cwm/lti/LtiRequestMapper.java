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
        return null;
    }
}
