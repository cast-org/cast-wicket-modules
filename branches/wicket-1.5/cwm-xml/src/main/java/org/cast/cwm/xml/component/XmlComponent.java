/*
 * Copyright 2011-2013 CAST, Inc.
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
package org.cast.cwm.xml.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;
import org.cast.cwm.xml.TransformResult;
import org.cast.cwm.xml.XmlSection;
import org.cast.cwm.xml.service.IXmlService;
import org.cast.cwm.xml.transform.TransformParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

/**
 * A Wicket Component that displays HTML generated from a section of an XML file.
 * @author bgoldowsky
 *
 */
public class XmlComponent extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {
	
	private static final Logger log = LoggerFactory.getLogger(XmlComponent.class);
	private static final long serialVersionUID = 1L;
	private static final String WICKET_NS = "http://wicket.apache.org";
	
	private boolean previouslyPopulated; // Has this been rendered yet?
	
	@Getter @Setter
	private boolean updateable = false;  // Will this XmlComponent ever be redrawn via Ajax?
	private String transformName;
	
	@Getter @Setter
	private TransformParameters transformParameters = null;
	
	@Inject
	protected IXmlService xmlService;
	
	public XmlComponent(String id, ICacheableModel<? extends IXmlPointer> secMod, String transformName) {
		super(id, secMod);
		this.transformName = transformName;
		this.setEscapeModelStrings(false);
	}
	
	@SuppressWarnings("unchecked")
	public ICacheableModel<? extends IXmlPointer> getModel() {
		return (ICacheableModel<? extends IXmlPointer>) getDefaultModel();
	}

	@Override
	protected void onBeforeRender() {
		if (updateable || !previouslyPopulated) {
			addDynamicComponents();
			previouslyPopulated = true;
		}
		super.onBeforeRender();
	}
	
	public void setTransformParameter(String key, Object value) {
		if (transformParameters == null)
			transformParameters = new TransformParameters();
		transformParameters.put(key, value);
	}
	
	public boolean isEmpty () {
		TransformResult res = xmlService.getTransformed(getModel(), transformName, transformParameters);
		return res.getElement() == null;
	}

	/** 
	 * <p>
	 * Find all elements in this component's markup that have a wicket:id, and attach a dynamic component for them.
	 * This supports nested Wicket Components.  A container can add components to itself elsewhere and those components 
	 * will not be overridden/replaced by this method.  
	 * 
	 */
	protected void addDynamicComponents() {
		Element dom = xmlService.getTransformed(getModel(), transformName, transformParameters).getElement();
		NodeList componentNodes = xmlService.getWicketNodes(dom, true);
		Map<Element,Component> componentMap = new HashMap<Element,Component>(); // Mapping of Nodes to Components; used for nesting
		final Set<String> wicketIds = new HashSet<String>();
		
		for (int i=0; i<componentNodes.getLength(); i++) {
		
			Element e = (Element)componentNodes.item(i);

			String id = e.getAttributeNS(WICKET_NS, "id");
			
			// Traverse up through this node's parents.  If a parent
			// maps to a wicket component, then that must be a MarkupContainer
			// for the current component.
			MarkupContainer container = null;
			Node parent = e.getParentNode();
			while (container == null && parent != null) {
				container = (MarkupContainer) componentMap.get(parent);
				parent = parent.getParentNode();
			}
			
			// Check to see if a component is a direct child and if
			// one already exists with that id. If so, no need to 
			// regenerate, but mark that it's valid.
			if (updateable && container == null && get(id) != null) {
				wicketIds.add(id);
				continue;
			}

			// If no container, add directly to the document. If container exists, add 
			// the component only if one does not already exist with that wicket:id.  This allows
			// panels, subclasses, etc to add components to this markup and not be overridden
			// or duplicated.
			if (container == null) {
				log.trace("Adding Dynamic Component to Root Panel {}: {}.", id, e);
				wicketIds.add(id); // Valid child, for removing stale children later.
				Component c = getDynamicComponent(id,e);
				add(c);
				componentMap.put(e, c);

			} else {
				Component c = container.get(id);
				if (c == null) {
					c = getDynamicComponent(id, e);
					log.trace("Adding Dynamic Component ({}) to Container ({}).", id, container.getId());
					container.add(c);
					componentMap.put(e, c);
				} else {
					componentMap.put(e, c);
				}
			}
		}
		
		// Remove any stale components that might have been added during previous renders.
		List<Component> removeList = new ArrayList<Component>(); // To be removed
		if (updateable) {
			for (int i = 0; i < size(); i++) {
				if (!wicketIds.contains(get(i).getId()))
					removeList.add(get(i));
			}
			for(Component c: removeList)
				c.remove();
		}
		
	}
	
	/**
	 * Determine what Wicket Component to insert into the XmlComponent based on the ID and XML Element.
	 * Override this method to create the proper dynamic behavior for your application.
	 * 
	 * @param wicket:id of the sub-component
	 * @param Post-transform XML Element that bears the wicket:id
	 * @return a Component to add as a child of the XmlComponent
	 */
	public Component getDynamicComponent(String wicketId, Element elt) {
		
		boolean isContainer = false;
		
		// Check to see if this element has any Wicket Children.
		NodeList childrenComponents = xmlService.getWicketNodes(elt, false);
		if (childrenComponents.getLength() > 0) {
			isContainer = true;
		}

		// If we found a child, return a container with debugging style.  Otherwise, a generic label.
		if (isContainer) {
			return new WebMarkupContainer(wicketId).add(AttributeModifier.replace("style", "border: 3px solid red"));
		} else {
			return new Label(wicketId, "[[[Dynamic component with ID " + wicketId + "]]]").add(AttributeModifier.replace("style", "border: 3px solid red"));
		}
	}

	/**
	 * Get the String representation of the content, as HTML with Wicket IDs, from the XML Section.
	 * @return
	 */
	protected String getTransformedMarkup() {
		String content = xmlService.getTransformed(getModel(), transformName, transformParameters).getString();
		if (content == null)
			content = "";
		return "<wicket:panel>" + content + "</wicket:panel>";
	}
	
	/**
	 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.setType(TagType.OPEN); // Ensure open/close tags.  Turns <span /> into <span></span>
	}
	
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
		return new StringResourceStream(getTransformedMarkup());
	}

	public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
		return null;
	}
	
	/**
	 * This class no longer assumes that the source object is an {@link XmlSection}.  Instead,
	 * it uses an {@link IXmlPointer}, of which XmlSection is a subclass.
	 * @return
	 */
	@Deprecated
	public XmlSection getXmlSection() {
		Object obj = getDefaultModelObject();
		if (obj instanceof XmlSection)
			return (XmlSection) getDefaultModelObject();
		else
			return null;
	}

	
	/**
	 * Will confirm that the given element has a valid wicket:id structure.  That 
	 * is, each node that represents a wicket component must be unique to its wicket.
	 * siblings.  This function will process all descendents of the given element.
	 * 
	 * @param elt
	 */
//	public static void confirmUniqueWicketIds(Element elt) {
//		
//		List<String> wicketIds = new ArrayList<String>();
//		NodeList wicketChildren = getWicketNodes(elt, false);
//		
//		for (int i=0; i < wicketChildren.getLength(); i++) {
//			Element child = (Element) wicketChildren.item(i);
//			String id = child.getAttributeNS(WICKET_NS, "id");
//			if (!wicketIds.contains(id)) {
//				wicketIds.add(id);
//			} else {
//				int count = 0;
//				String newId = id + "_" + count;
//				while(wicketIds.contains(newId))
//					newId = id + "_" + count++;
//				wicketIds.add(newId);
//				child.setAttributeNS(WICKET_NS, "id", newId);
//				log.trace("Found Duplicate Wicket Id ({}) when processing DOM Tree.  Generated new Id ({})", id, newId);
//			}
//			confirmUniqueWicketIds(child);
//		}
//	}
//	
	public static class AttributeRemover extends AbstractBehavior {
		
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
	
	protected int getWidth(Element elt, int defaultValue) {
		try {
			return Integer.valueOf(elt.getAttributeNS(null, "width"));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	protected int getHeight(Element elt, int defaultValue) {
		try {
			return Integer.valueOf(elt.getAttributeNS(null, "height"));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
