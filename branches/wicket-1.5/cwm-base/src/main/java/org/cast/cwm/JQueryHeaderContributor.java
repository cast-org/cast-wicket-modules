package org.cast.cwm;

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.wicketstuff.jslibraries.JSLib;
import org.wicketstuff.jslibraries.Library;
import org.wicketstuff.jslibraries.VersionDescriptor;

/**
 * Loads a version of jQuery that is consistent across all CWM components that need it.
 * Can probably be dropped in Wicket 6 since the framework itself will depend on jQuery.
 * 
 * @author bgoldowsky
 *
 */
public class JQueryHeaderContributor implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	public void renderHead(IHeaderResponse response) {
		JSLib.getHeaderContribution(VersionDescriptor.exactVersion(Library.JQUERY, 1, 7, 1)).renderHead(response);
	}

}
