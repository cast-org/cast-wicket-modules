/*
 * Copyright 2011-2014 CAST, Inc.
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
package org.cast.cwm.glossary;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.cast.cwm.xml.IXmlPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is intended to be an easy-to-extend base class for a Panel that 
 * displays a glossary entry and controls for navigating through a glossary.
 * It implements some of the tricky parts like sorted lists of words,
 * indicators for current and empty glossary sections, etc.
 * 
 * @author bgoldowsky
 *
 */
public abstract class BaseGlossaryPanel extends Panel {
	private static final long serialVersionUID = 1L;

	final private static Logger log = LoggerFactory.getLogger(BaseGlossaryPanel.class);

	/**
	 * Implementer must define this to return the glossary to be displayed.
	 * @return a Glossary
	 */
	abstract protected Glossary getGlossary();
	
	/**
	 * @return the page class for glossary - expected to be implemented in the
	 * application file
	 */
	abstract protected Class<? extends WebPage> getGlossaryPageClass();
		
	/**
	 * Implementer must define this to create and return an XmlComponent for the given
	 * section model (which will be the glossary definition)
	 * Abstract since applications generally subclass XmlComponent and will want
	 * to construct the component using their subclass.
	 * @param id
	 * @param xmlSection holding the definition's XmlSection
	 * @return an XmlComponent showing the definition.
	 */
	abstract protected Component newXmlComponent(String string, IModel<? extends IXmlPointer> xmlSection);

	
	/**
	 * Instantiate the panel
	 * 
	 * @param id wicket Id
	 * @param mEntry glossary entry of the word to show (may be null)
	 */
	public BaseGlossaryPanel (String id, IModel<? extends IGlossaryEntry> mEntry) {
		super(id, mEntry);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		@SuppressWarnings("unchecked")
		IModel<? extends IGlossaryEntry> mEntry = (IModel<? extends IGlossaryEntry>) getDefaultModel();
		add (newTopNav(mEntry==null ? null : mEntry.getObject()));
		add (newLeftNav(mEntry==null ? null :mEntry.getObject()));
		add (newDefinitionContainer(mEntry==null ? null : mEntry.getObject()));
	}
		
	/**
	 * Create and return a Component showing the top navigation of the glossary--
	 * usually, a list of letters with anchor links.
	 * @param entry Glossary entry; may be null.
	 * @return new Component
	 */
	protected Component newTopNav (IGlossaryEntry entry) {
		RepeatingView topnav = new RepeatingView("topnav");
		for (char ch = 'a'; ch <= 'z'; ch++)
			topnav.add(newTopNavContainer(topnav.newChildId(), ch, entry));
		topnav.add(newTopNavContainer(topnav.newChildId(), '#', entry));
		return topnav;
	}

	/**
	 * Create and return container for a single letter in the top navigation bar.
	 * Subclasses can override for different behavior.
	 * @param id
	 * @param letter the letter to display
	 * @param current Glossary entry; may be null.
	 * @return new Component
	 */
	protected Component newTopNavContainer (String id, Character letter, IGlossaryEntry current) {
		return new TopNavContainer(id, letter, current);
	}
	
	protected class TopNavContainer extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		public TopNavContainer (String id, Character letter, IGlossaryEntry current) {
			super(id);
			List<IModel<? extends IGlossaryEntry>> list = getGlossaryEntriesForLetter(letter);
			if (list == null || list.isEmpty()) {
				// Non-alphabetic container is special: it disappears completely when empty.
				if (letter == '#')
					setVisible(false);
				else
					add(AttributeModifier.replace("class", "empty"));
			} else if (list.contains(current)) {
				add(AttributeModifier.replace("class", "current"));
			}
			
			add(new Label("toplink", letter.toString().toUpperCase())
				.add(AttributeModifier.replace("href", "#"+letter))
			);
		}
	}
	

	/**
	 * Create and return a Component showing the left navigation of the glossary--
	 * usually, the list of glossary words linked to the page for their definition.
	 * @param current Glossary entry; may be null.
	 * @return new Component
	 */
	protected Component newLeftNav (IGlossaryEntry entry) {		
		RepeatingView leftnav = new RepeatingView("glosslist");
		for (char ch = 'a'; ch <= 'z'; ch++)
			leftnav.add(newLeftNavLetterContainer(leftnav.newChildId(), ch, entry));
		leftnav.add(newLeftNavLetterContainer(leftnav.newChildId(), '#', entry));
		return leftnav;
	}

	/**
	 * Return a Component for a single letter's word list in the left navigation.
	 * @param id
	 * @param letter
	 * @param entry the current glossary entry; may be null.
	 * @return
	 */
	protected Component newLeftNavLetterContainer (String id, Character letter, IGlossaryEntry entry) {
		return new LetterContainer (id, letter, entry);
	}
	
	/**
	 * Default implementation of a left-nav word list container.
	 *
	 */
	protected class LetterContainer extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;

		public LetterContainer (String id, Character letter, IGlossaryEntry current) {
			super(id);
			List<IModel<? extends IGlossaryEntry>> list = getGlossaryEntriesForLetter(letter);
			if (list == null || list.isEmpty()) {
				this.setVisible(false);
				return;
			}
			WebMarkupContainer anchor = new WebMarkupContainer("anchor");
			add(anchor);
			anchor.add(AttributeModifier.replace("id", letter.toString()));
			anchor.add (new Label("letter", letter.toString().toUpperCase()));
			add (new LetterListView ("list", letter, current));
		}
	}
	
	/**
	 * A navigation list of words starting with a letter, pulled from the glossary
	 *
	 */
	protected class LetterListView extends RepeatingView {
		private static final long serialVersionUID = 1L;
		
		public LetterListView(final String id, final Character letter, final IGlossaryEntry current) {
			super(id);
			
			List<IModel<? extends IGlossaryEntry>> list = getGlossaryEntriesForLetter(letter);

			for (IModel<? extends IGlossaryEntry> mEntry : list) {
				WebMarkupContainer container = new WebMarkupContainer(this.newChildId());
				add(container);
				
				IGlossaryEntry e = mEntry.getObject();
				
				container.add(newLink(letter, e));

				if (e.equals(current))
					container.add(AttributeModifier.replace("class", getCurrentCssClass()));
				if (getGlossary().getEntryById(e.getIdentifier()) != null) {
					container.add(new WebMarkupContainer("wordCardIcon").setVisible(false));
				} else {
					container.add(new WebMarkupContainer("wordCardIcon"));					
				}
			}
		}		
	}

	/**
	 * Create and return a Component to show the definition of the word.
	 * If no word is being defined, this function will still be called, 
	 * but the argument will be null.
	 * @param id
	 * @param entry Glossary entry; may be null.
	 * @return a Component displaying the definition
	 */
	protected Component newDefinitionContainer (IGlossaryEntry entry) {
		WebMarkupContainer wmc = new WebMarkupContainer("glossword");

		if (entry != null) { 
			// found a word
			IModel<? extends IXmlPointer> xmlSection = entry.getXmlPointer();
			wmc.add(new Label("name", entry.getHeadword()));
			if (xmlSection.getObject() != null) { 
				// this is a glossary word
				wmc.add(newXmlComponent("definition", xmlSection));
			} else { 
				log.error("Could not find glossary item for {}", entry);
				wmc.add(new Label("definition", "Could not find glossary entry for " + entry.getHeadword()));
			}
		} else {  
			// there is no word
			wmc.add(new WebMarkupContainer("name"));
			wmc.add(new WebMarkupContainer("definition"));
		}
		
		return wmc;
	}

	/**
	 * Constuct and return an appropriate Link element for the given glossary entry.
	 * @param e
	 * @return
	 */
	public Link<?> newLink(final Character letter, IGlossaryEntry e) {
		BookmarkablePageLink<WebPage> link = new BookmarkablePageLink<WebPage>("link", getGlossaryPageClass()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected CharSequence appendAnchor(ComponentTag tag, CharSequence url) {
				return url + "#" + letter;
			}
		};
		link.getPageParameters().set("word", e.getIdentifier());
		link.add(new Label("label", e.getHeadword()));
		return link;
	}

	/**
	 * Override this method if you need to  add user entries to the glossary.  
	 * 
	 * @param letter
	 * @return
	 */
	protected List<IModel<? extends IGlossaryEntry>> getGlossaryEntriesForLetter(Character letter) {
		List<IModel<? extends IGlossaryEntry>> list = new ArrayList<IModel<? extends IGlossaryEntry>>();
		Glossary glossary = getGlossary();
		for (String id : glossary.getEntryIdsByFirstChar(letter))
			list.add (glossary.getEntryById(id));
		return list;
	}
	
	protected String getCurrentCssClass() {
		return "current";
	}

}