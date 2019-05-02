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
package org.cast.cwm.figuration.form;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;

/**
 * A glamorized TextField with Figuration-style label and error notification.
 * Any feedback messages generated will be presented immediately under the input field.
 *
 * The field is decorated with a label, optional placeholder text inside the field, and
 * optional instructions.
 *
 * The label is required.  This can be provided via setLabel(String); otherwise it is
 * expected that an appropriate label is set in a properties file, eg, if this component is
 * added with wicket id "inpitem", in a form with wicket id "form", then include something like:
 * <code><pre>form.inpitem.label = Label for the field</pre></code>
 *
 * Similarly placeholder can be provided via a <code><pre>form.inpitem.placeholder</pre></code>
 * property, or you can supply a model with {@link #setPlaceholderModel(IModel)}.  If there is
 * neither, no placeholder attribute will be generated.
 *
 * Similarly an <pre>instructions</pre> property can be provided, or a model supplied with
 * {@link #setInstructionsModel(IModel)}.
 *
 * Required fields will have the "required" HTML attribute set, to allow for automatic
 * client-side validation by modern web browsers.
 * You can add the "novalidate" attribute to the form if you do not want this.
 *
 * You may also want to set appropriate error messages in your properties file, eg
 * <code><pre>inpitem.Required = ${label} is required</pre></code>
 *
 * @author bgoldowsky
 */
@Slf4j
public class FigurationTextField<T> extends FormComponentPanel<T> {

    @Getter @Setter
    private boolean validationIcon = true;

    @Getter
    private IModel<String> placeholderModel;

    @Getter
    private IModel<String> instructionsModel;

    private final TextField<T> input;

    public FigurationTextField(String wicketId, IModel<T> model) {
        super(wicketId, model);

        input = new TextField<T>("input", model) {

            @Override
            protected String[] getInputTypes()
            {
                return FigurationTextField.this.getInputTypes();
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                String placeholder = placeholderModel.getObject();
                if (!Strings.isEmpty(placeholder))
                    tag.put("placeholder", placeholder);
                if (!FigurationTextField.this.isValid())
                    appendToClass(tag, "is-invalid");
                if (validationIcon)
                    appendToClass(tag, "has-validation-icon");
                if (isRequired())
                    tag.put("required", true);
            }
        };
        add(input);

        // By default, label, placeholder, and instructions use resource-based models,
        // but each can be overridden with setter methods.
        setLabel(new ResourceModel("label"));
        setPlaceholderModel(new ResourceModel("placeholder", ""));
        setInstructionsModel(new ResourceModel("instructions", ""));

        // The lambdas here make sure that child components are aware of future changes made
        // via setLabel or setInstructionsModel.
        FormComponentLabel label = new FormComponentLabel("label", input);
        label.add(new Label("text", ()->this.getLabel().getObject()));
        add(label);

        // Instructions are empty, and thus hidden, by default.
        Label instructions = new Label("instructions", ()->this.getInstructionsModel().getObject()) {
            @Override
            protected void onConfigure() {
                setVisible(!Strings.isEmpty(this.getDefaultModelObjectAsString()));
                super.onConfigure();
            }
        };
        add(instructions);

        add(new ComponentFeedbackPanel("feedback", this));
    }

    @Override
    protected void onConfigure() {
        input.setRequired(isRequired());
        super.onConfigure();
    }

    /**
     * Subclass should override this method if this textfield is mapped on a different input type as
     * text. Like PasswordTextField or HiddenField.
     *
     * @return The input type of this textfield, default is null
     *
     * @see TextField getInputTypes()
     */
    protected String[] getInputTypes() {
        return null;
    }

    @Override
    public void convertInput() {
        setConvertedInput(input.getConvertedInput());
    }

    public void setInstructionsModel(IModel<String> model) {
        instructionsModel = wrap(model);
    }

    public void setPlaceholderModel(IModel<String> model) {
        placeholderModel = wrap(model);
    }

    protected void appendToClass(ComponentTag tag, String newClass) {
        String oldClass = tag.getAttribute("class")==null ? "" : tag.getAttribute("class")+" ";
        tag.put("class", oldClass+newClass);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (instructionsModel != null)
            instructionsModel.detach();
        if (placeholderModel != null)
            placeholderModel.detach();
    }

}
