package org.cast.cwm.components;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;

/**
 * A TextArea that will automatically resize itself.
 * Uses Chris Bader's Javascript library, pulled from https://github.com/akaihola/jquery-autogrow .
 * 
 * @author bgoldowsky
 *
 * @param <T>  the model type of the TextArea
 */
public class AutoGrowTextArea<T> extends TextArea<T> implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	public AutoGrowTextArea (String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	public AutoGrowTextArea (String id, IModel<T> model) {
		super(id, model);
		this.setOutputMarkupId(true);
	}

	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(new ResourceReference(AutoGrowTextArea.class, "jquery.autogrow.js"));
		response.renderOnDomReadyJavascript(String.format("$('#%s').autogrow();", getMarkupId()));
	}


}
