package org.cast.cwm.components;

import org.apache.wicket.markup.html.border.Border;

/**
 * A Border that adds extended-browser-information gathering capability to a (stateless) Form.
 * Additional form fields that you actually want can be added inside the Border.
 * Use {#getForm()} to get access to the actual Form object inside the Border,
 * and override {#onSubmit()} to add your own submit behavior.
 *  
 * @author bgoldowsky
 *
 * @param <T> model type for the Form
 */
public class BrowserInfoGatheringFormBorder<T> extends Border {

	private static final long serialVersionUID = 1L;

	BrowserInfoGatheringForm<T> form;
	
	public BrowserInfoGatheringFormBorder(String id) {
		super(id);
		form = new BrowserInfoGatheringForm<T>("postback") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit()	{
				super.onSubmit();
				BrowserInfoGatheringFormBorder.this.onSubmit();
			}
		};
		add(form);
		
		// The border's body is inside the form.
		form.add(getBodyContainer());
	}
	
	/**
	 * Called when the enclosed Form is submitted, after processing the browser info.
	 * Override this, and call super.onSubmit(), to add submit behavior to the Border's Form.
	 */
	protected void onSubmit() {
	}

	BrowserInfoGatheringForm<T> getForm() {
		return form;
	}
	
}
