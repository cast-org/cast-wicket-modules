/*
 * Copyright 2011-2019 CAST, Inc.
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
package org.cast.cwm.data.validator;

import lombok.Getter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Given a set of form components, ensure that only a given count
 * have input.  For example, an image upload form that takes an
 * Upload OR a Url, but not both.
 * 
 * @author jbrookover
 *
 */
public class DuplicateInputValidator extends AbstractFormValidator {

	private static final long serialVersionUID = 1L;
	
	/** form components to be checked. */
	private final FormComponent<?>[] components;
	
	/**
	 * The number of components that can be filled in.
	 */
	@Getter
	private final int count;
	
	/**
	 * The number of components that must be filled in.
	 */
	@Getter 
	private int required = 0;

	public DuplicateInputValidator (FormComponent<?>... components) {
		this(1, components);
	}
	
	public DuplicateInputValidator(int count, FormComponent<?>...components ) {

		if (count <= 0)
			throw new IllegalArgumentException("Count must be greater than 0.");

		for (FormComponent<?> c : components) {
			if (c == null)
				throw new IllegalArgumentException("Component cannot be null");
		}
		this.components = components;
		this.count = count;
	}
	
	
	@Override
	public FormComponent<?>[] getDependentFormComponents() {
		return components;
	}

	@Override
	public void validate(Form<?> form) {
		int in = 0;
		List<FormComponent<?>> inputs = new ArrayList<FormComponent<?>>();
		for (FormComponent<?> c : components) {
			if (c.getConvertedInput() != null) {
				in++;
				inputs.add(c);
			}
		}
		
		// Error if we've gone over the limit for duplication
		if (in > count) {
			for (FormComponent<?> c : inputs)
				error(c, "DuplicateInputValidator.count");
		}
		
		// Error if we don't have enough duplication
		if (in < required) {
			for (FormComponent<?> c : components)
				error(c, required == count ? "DuplicateInputValidator.exact" : "DuplicateInputValidator.required");		
		}
	}
	
	public DuplicateInputValidator setRequired(int count) {
		if (count > this.count || count > getDependentFormComponents().length)
			throw new IllegalArgumentException("Cannot require more components than are allowed to be entered");
		this.required = count;
		return this; // for chaining
	}
	
	@Override
	protected Map<String, Object> variablesMap() {
		final Map<String, Object> map = super.variablesMap();
		map.put("minimum", required);
		map.put("maximum", count);
		// Careful - there could be hidden fields used by this validator.
		map.put("total", getDependentFormComponents().length);
		return map;
	}
}
