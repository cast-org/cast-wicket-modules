package org.cast.cwm.components;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

public class AttributeRemover extends Behavior {
	
	private String[] atts;

	private static final long serialVersionUID = 1L;

	public AttributeRemover (String... atts) {
		this.atts = atts;
	}
	
	@Override
	public void onComponentTag(Component component,	ComponentTag tag) {
		for (String at : atts)
			tag.getAttributes().remove(at);
	}
}
