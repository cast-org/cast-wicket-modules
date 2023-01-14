package org.cast.cwm.lti.component;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.cast.cwm.lti.service.IJwtSigningService;
import org.cast.cwm.lti.service.ILtiService;

import java.util.List;

/**
 * Component to send the response of a deeplinking request.
 */
@Slf4j
public class LinkingResponder extends Panel {

    @Inject
    private ILtiService ltiService;

    @Inject
    private IJwtSigningService signingService;

    private final WebMarkupContainer form;

    private final WebMarkupContainer jwt;
    
    public LinkingResponder(String id) {
        super(id);

        form = new WebMarkupContainer("form");
        form.setOutputMarkupId(true);
        add(form);

        jwt = new WebMarkupContainer("jwt");
        form.add(jwt);
    }

    public void respond(AjaxRequestTarget target, List<?> resources) {

        ILtiService.Response response = ltiService.createDeepLinkingResponse(resources);

        if (log.isInfoEnabled()) {
            log.info(new GsonBuilder().setPrettyPrinting().create().toJson(response.payload));
        }

        this.form.add(new Behavior() {
            @Override
            public boolean isTemporary(Component component) {
                return true;
            }

            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("action", response.url);
            }
        });

        this.jwt.add(new Behavior() {
            @Override
            public boolean isTemporary(Component component) {
                return true;
            }

            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("value", signingService.sign(response.payload));
            }
        });

        target.add(this.form);

        target.appendJavaScript(String.format("Wicket.$('%s').submit();", this.form.getMarkupId()));
    }
}
