package org.cast.cwm.components;

import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * A RepeatingView that introduces some plain-text markup between children.
 * By default the separator is simply a space, but it can be set to any string.
 */
public class DelimitedRepeatingView extends RepeatingView {

	private static final long serialVersionUID = 1L;

	private boolean firstChildRendered = false; 

	private String delimiter = " "; 

	public DelimitedRepeatingView(String id) {
		super(id);
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
	
	public String getSeparator() {
		return delimiter;
	}

	public void setDelimiter(String separator) {
		this.delimiter = separator;
	}
	
}