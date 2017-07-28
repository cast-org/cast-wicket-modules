/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.figuration.component;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.cast.cwm.figuration.Direction;

import java.util.Map;

/**
 * Base class for Tooltips built with Figuration.
 *
 * Seldom used, since tooltips are very simple to create just with HTML markup,
 * but included here for completeness.  You may want to use this in case the configuration
 * of the tooltip needs to be set dynamically.
 *
 * @author bgoldowsky
 */
public class FigurationTooltip<T> extends FigurationHideable<T> {

	/**
	 * Set placement to one of the four directions to position the popover with respect to its trigger.
	 */
	@Getter
	@Setter
	protected Direction placement = null;

	/**
	 * If true, and a {@link #placement} is set, then Figuration is allowed to flip
	 * the placement to the other side in order to fit the popover on the screen.
	 */
	@Getter @Setter
	protected boolean placementAuto = false;

	public FigurationTooltip(String id) {
		this(id, null);
	}

	public FigurationTooltip(String id, IModel<T> model) {
		super(id, model);
	}

	/**
	 * Adds the given Panel as the body of this tooltip.
	 *
	 * @param bodyPanel panel to use as the body
	 * @return this, for chaining
	 */
	public FigurationTooltip<T> withBody(Panel bodyPanel) {
		if (!bodyPanel.getId().equals(BODY_ID))
			throw new IllegalArgumentException("Body panel must have id " + BODY_ID);
		addOrReplace(bodyPanel);
		return this;
	}

	@Override
	public Map<String, String> getInitializeParameters() {
		Map<String, String> map = super.getInitializeParameters();
		if (placement != null) {
			map.put("placement", placement.name().toLowerCase() + (placementAuto ? " auto" : ""));
		}
		return map;
	}

	@Override
	public String getInitializationFunctionName() {
		return "CFW_Tooltip";
	}

	@Override
	public String getClassAttribute() {
		return null; // Not applicable for tooltips
	}

}
