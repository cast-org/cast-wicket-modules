/*
 * Copyright 2011 CAST, Inc.
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

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;


/**
 * basically a copy of bookmarkablePageLink except it doesn't create 
 * new pages so wicket can determine if the page has expired or not
 *
 * Deprecated since I can't find any references to this and wonder what
 * it is used for.
 */
@Deprecated
public class CwmPageLink extends Link<Page> {

  private static final long serialVersionUID = 1L;
  private PageParameters params = new PageParameters();
  private Class<? extends Page> pageClass;

  /**
   * 
   * @param id - the wicket id
   * @param pageClass - the page to link to
   */
  @Deprecated
  public CwmPageLink(String id, Class<? extends Page> pageClass) {
    super(id);
    this.pageClass = pageClass;
  }
  
  /**
   * 
   * @param id - the wicket id
   * @param pageClass - the page to link to
   * @param parameters - the pageParameters
   */
  @Deprecated
  public CwmPageLink(String id, Class<? extends Page> pageClass, PageParameters parameters) {
    super(id);
    this.pageClass = pageClass;
    if(parameters != null)
      this.params = parameters;
  }

  @Override
  public final void onClick() {
    setResponsePage(pageClass, params);
  }
  
  public CwmPageLink setParameter(String key, String value) {
    params.put(key, value);
    return this;
  }
  
  public CwmPageLink setParameter(String key, int value) {
    params.put(key, Integer.toString(value));
    return this;
  }
  
  public CwmPageLink setParameter(String key, long value) {
    params.put(key, Long.toString(value));
    return this;
  }
}
