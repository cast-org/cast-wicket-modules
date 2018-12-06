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
package org.cast.cwm.components;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;

/**
 * A RefreshingView that introduces some plain-text markup between children.
 * By default the separator is simply a space, but it can be set to any string.
 */
public abstract class DelimitedRefreshingView<T> extends RefreshingView<T> {

    private boolean firstChildRendered = false;

    @Getter @Setter
    private String delimiter = " ";

    public DelimitedRefreshingView(String id) {
        super(id);
    }

    public DelimitedRefreshingView(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected void onBeforeRender() {
        firstChildRendered = false;
        super.onBeforeRender();
    }

    @Override
    protected void renderChild(Component child) {

        boolean childVisible = child.isVisible();

        if (firstChildRendered && childVisible && delimiter != null) {
            getResponse().write(delimiter);
        }

        super.renderChild(child);

        if (childVisible) {
            firstChildRendered = true;
        }
    }

}
