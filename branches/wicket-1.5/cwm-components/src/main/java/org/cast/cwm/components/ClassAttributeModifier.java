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
package org.cast.cwm.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A behavior that makes it easy to add or remove 'class'
 * attributes from HTML elements.
 *
 */
public class ClassAttributeModifier extends AttributeModifier {

  private static final long serialVersionUID = 1L;
  private boolean remove;

  /**
   * Constructor. Adds the class specified.
   * 
   * @param value - the class to add
   */
  public ClassAttributeModifier(String value) {
    this(value, false);
  }
  
  /**
   * adds or removes the given class
   * @param value - the class
   * @param remove - true to remove this class, false to add
   */
  public ClassAttributeModifier(String value, boolean remove) {
	  this(Model.of(value), remove);
  }
  
  public ClassAttributeModifier(IModel<?> value, boolean remove) {
	  super("class", true, value);
	  this.remove = remove;
  }
  
  @Override
  protected String newValue(final String currentValue, final String replacementValue) {
    if(replacementValue == null || replacementValue.equals(""))
      return currentValue;
    if(currentValue == null || currentValue.equals("")) {
      if(remove)
        return currentValue;
      return replacementValue;
    }
    if(currentValue.contains(replacementValue) && remove)
      return currentValue.replace(replacementValue, "");
    if(!remove)
      return currentValue + " " + replacementValue;
    return currentValue;
  }
}
