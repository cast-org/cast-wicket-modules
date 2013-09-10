package org.cast.cwm.xml.handler;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.ResourceReference;
import org.cast.cwm.IRelativeLinkSource;
import org.w3c.dom.Element;

public class ImageComponentHandler extends BaseDynamicComponentHandler implements IDynamicComponentHandler {

	public ImageComponentHandler(String prefix) {
		super(prefix);
	}

	public Component getComponent(String wicketId, Element element,
			IRelativeLinkSource linkSource) {
		String src = element.getAttribute("src");
		ResourceReference imgRef = linkSource.getRelativeReference(src);
		return new Image(wicketId, imgRef);
	}

}
