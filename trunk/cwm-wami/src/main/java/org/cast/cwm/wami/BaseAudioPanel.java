package org.cast.cwm.wami;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * Base class to reduce redundancy between the different recorder panels.
 * 
 * @author bgoldowsky
 *
 * @param <T> model object type
 */
public abstract class BaseAudioPanel<T> extends GenericPanel<T> {

	private static final long serialVersionUID = 1L;

	public BaseAudioPanel(String id, IModel<T> model) {
		super(id, model);
	}

}
