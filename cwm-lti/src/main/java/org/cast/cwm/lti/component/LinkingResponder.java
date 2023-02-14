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
package org.cast.cwm.lti.component;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.cast.cwm.lti.LtiLaunch;
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

        String launchUrl = getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(urlFor(new LtiLaunch())));

        ILtiService.Request request = ltiService.respondDeepLinking(launchUrl, resources);

        if (log.isInfoEnabled()) {
            log.info(new GsonBuilder().setPrettyPrinting().create().toJson(request.payload));
        }

        this.form.add(new Behavior() {
            @Override
            public boolean isTemporary(Component component) {
                return true;
            }

            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("action", request.url);
            }
        });

        this.jwt.add(new Behavior() {
            @Override
            public boolean isTemporary(Component component) {
                return true;
            }

            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("value", signingService.sign(request.payload));
            }
        });

        target.add(this.form);

        target.appendJavaScript(String.format("Wicket.$('%s').submit();", this.form.getMarkupId()));
    }
}
