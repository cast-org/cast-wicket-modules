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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.UrlUtils;
import org.cast.cwm.components.ImageUrlCodingStrategy;

/**
 * A simple image component that generates URLs targeted at {@link ImageUrlCodingStrategy} directories.
 * Unlike {@link Image} in that you don't need a package resource or a ResourceReference for the image.
 * 
 * @author bgoldowsky
 *
 */
public class Icon extends WebComponent {

	protected IModel<String> mImagePath;
	
	protected IModel<String> mAltText = null;

	protected IModel<String> mTitleText = null;

	private static final long serialVersionUID = 1L;

	public Icon (String wicketId) {
		super(wicketId);
	}
	
	public Icon (String wicketId, IModel<String> mImagePath) {
		super(wicketId);
		if(mImagePath == null) throw new IllegalArgumentException("image path model must be supplied");
		this.mImagePath = mImagePath;
	}
	
	public Icon (String wicketId, String imagePath) {
		super(wicketId);
		this.mImagePath = new Model<String>(imagePath);
	}
	
	public Icon (String wicketId, IModel<String> mImagePath, IModel<String> mAltText, IModel<String> mTitleText) {
		this(wicketId, mImagePath);
		if(mImagePath == null) throw new IllegalArgumentException("image path model must be supplied");
		this.mAltText = mAltText;
		this.mTitleText = mTitleText;		
	}
	
	public Icon (String wicketId, String imagePath, IModel<String> mAltText, IModel<String> mTitleText) {
		this(wicketId, imagePath);
		this.mAltText = mAltText;
		this.mTitleText = mTitleText;
	}

	public Icon (String wicketId, String imagePath, String altText) {
		this(wicketId, imagePath, altText, null);
	}

	public Icon (String wicketId, String imagePath, String altText, String titleText) {
		this(wicketId, imagePath);
		if (altText != null)
			this.mAltText = new Model<String>(altText);
		if (titleText != null)
			this.mTitleText = new Model<String>(titleText);
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		checkComponentTag(tag, "img");
		if(mImagePath == null) throw new RuntimeException();
		if (mAltText != null)
			tag.put("alt", mAltText.getObject());
		// Title attribute is set to the same as ALT if no other title is specified.
		String title = null;
		if (mTitleText!=null)
			title = mTitleText.getObject();
		if (title == null && mAltText != null)
			title = mAltText.getObject();
		if (title != null)
			tag.put("title", title);
		tag.put("src", UrlUtils.rewriteToContextRelative(mImagePath.getObject(), RequestCycle.get().getRequest()));
		super.onComponentTag(tag);
	}

	
	
	@Override
	protected void onDetach() {
		super.onDetach();
		if (mAltText != null)
			mAltText.detach();
		if (mTitleText != null)
			mTitleText.detach();
		if (mImagePath != null)
			mImagePath.detach();
	}

	public IModel<String> getmImagePath() {
		return mImagePath;
	}

	public void setmImagePath(IModel<String> mImagePath) {
		if(mImagePath == null) throw new RuntimeException();
		this.mImagePath = mImagePath;
	}

	public IModel<String> getmAltText() {
		return mAltText;
	}

	public void setmAltText(IModel<String> mAltText) {
		this.mAltText = mAltText;
	}

	public IModel<String> getmTitleText() {
		return mTitleText;
	}

	public void setmTitleText(IModel<String> mTitleText) {
		this.mTitleText = mTitleText;
	}


}
