package org.cast.cwm.wami;

import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * This is a placeholder for the embedded SWF that is be used by {@link PlayerPanel} and {@link RecorderPanel}.
 * Put it on the page somewhere that it will not be removed or replaced by AJAX updates.
 * It should be a nice visible location, though, so that the settings panel can be seen. 
 *
 * @author bgoldowsky
 *
 */
public class WamiAppletHolder extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	public WamiAppletHolder(String id) {
		super(id);
		setOutputMarkupId(true);
	}

}
