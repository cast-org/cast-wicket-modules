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
package org.cast.cwm.data.component;

import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.string.Strings;

import java.util.*;
import java.util.Map.Entry;

/**
 * This is a re-creation of {@link MultiFileUploadField}.  It references a similar javascript
 * file but has more flexible display options.
 * 
 * Notice that this component clears its model at the end of the request, so the uploaded files MUST
 * be processed within the request they were uploaded.
 * 
 * Uses a modified javascript implementation from
 * http://the-stickman.com/web-development/javascript/upload-multiple
 * -files-with-a-single-file-element/
 * 
 * For customizing caption text see {@link #RESOURCE_LIMITED} and {@link #RESOURCE_UNLIMITED}
 * 
 * For an example of styling using CSS see the upload example in wicket-examples
 * 
 * @author jbrookover
 * @author Igor Vaynberg (ivaynberg)
 */
public class MultipleFileUploadField extends FormComponentPanel<Collection<FileUpload>> implements IHeaderContributor {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Represents an unlimited max count of uploads
	 */
	public static final int UNLIMITED = -1;

	/**
	 * Resource key used to retrieve caption message for when a limit on the number of uploads is
	 * specified. The limit is represented via ${max} variable.
	 * 
	 * Example: org.apache.wicket.mfu.caption.limited=Files (maximum ${max}):
	 */
	public static final String RESOURCE_LIMITED = "org.apache.wicket.mfu.caption.limited";

	/**
	 * Resource key used to retrieve caption message for when no limit on the number of uploads is
	 * specified.
	 * 
	 * Example: org.apache.wicket.mfu.caption.unlimited=Files:
	 */
	public static final String RESOURCE_UNLIMITED = "org.apache.wicket.mfu.caption.unlimited";

	private static final String NAME_ATTR = "name";

	private static final String MAGIC_SEPARATOR = "_mf_";


	private static final ResourceReference JS = new PackageResourceReference(MultipleFileUploadField.class, "MultipleFileUploadField.js");

	private final WebComponent upload;
	private final WebMarkupContainer container;

	private final int max;

	private transient String[] inputArrayCache = null;

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public MultipleFileUploadField(String id)
	{
		this(id, null, UNLIMITED);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param max
	 *            max number of files a user can upload
	 */
	public MultipleFileUploadField(String id, int max)
	{
		this(id, null, max);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public MultipleFileUploadField(String id, IModel<? extends Collection<FileUpload>> model)
	{
		this(id, model, UNLIMITED);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @param max
	 *            max number of files a user can upload
	 * 
	 */
    @SuppressWarnings("unchecked")
	public MultipleFileUploadField(String id, IModel<? extends Collection<FileUpload>> model, int max)
	{
		super(id, (IModel<Collection<FileUpload>>)model);

		this.max = max;

		upload = new WebComponent("upload");
		upload.setOutputMarkupId(true);
		add(upload);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);

	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponentPanel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// remove the name attribute added by the FormComponent
		if (tag.getAttributes().containsKey(NAME_ATTR))
		{
			tag.getAttributes().remove(NAME_ATTR);
		}
	}

	/**
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		// auto toggle form's multipart property
		Form<?> form = findParent(Form.class);
		if (form == null)
		{
			// woops
			throw new IllegalStateException("Component " + getClass().getName() + " must have a " +
				Form.class.getName() + " component above in the hierarchy");
		}
	}

	@Override
	public boolean isMultiPart()
	{
		return true;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		// initialize the (custom) javascript library
		response.render(JavaScriptHeaderItem.forReference(JS));
		
		StringBuffer js = new StringBuffer();
		
		// Javascript Variable reference for setting properties
		String jsVarName = upload.getMarkupId() + "_ms";

		// Create MultiSelector Object
		js.append("var " + jsVarName + " = new MultiSelector(");
		js.append("'" + getInputName() + "',");
		js.append("document.getElementById('" + container.getMarkupId() + "'),");
		js.append(max + ");");
		
		// Set Delete Button Properties
		js.append(jsVarName + ".setDeleteElementLabel('" + getString("org.apache.wicket.mfu.delete") + "');");
		js.append(jsVarName + ".setDeleteElementType('a');");
		js.append(jsVarName + ".setDeleteElementClass('small');");
		
		// Register initial upload element
		js.append(jsVarName + ".addElement(document.getElementById('" + upload.getMarkupId() + "'));");

		// Render
		response.render(OnDomReadyHeaderItem.forScript(js.toString()));
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#getInputAsArray()
	 */
	@Override
	public String[] getInputAsArray() {
		// fake the input array as if it contained an array of all uploaded file
		// names

		if (inputArrayCache == null) {
			// this array will aggregate all input names
			ArrayList<String> names = null;

			Request request = getRequest();
			if (request instanceof IMultipartWebRequest) {
				// retrieve the filename->FileItem map from request
				Map<String, List<FileItem>> itemNameToItem = ((IMultipartWebRequest)request).getFiles();
				Iterator<Entry<String, List<FileItem>>> it = itemNameToItem.entrySet().iterator();
				while (it.hasNext()) {
					// iterate over the map and build the list of filenames
					Entry<String, List<FileItem>> entry = it.next();
					String name = entry.getKey();
					for (FileItem item : entry.getValue()) {
						if (!Strings.isEmpty(name) &&
								name.startsWith(getInputName() + MAGIC_SEPARATOR) &&
								!Strings.isEmpty(item.getName()))
						{
							// make sure the fileitem belongs to this component and is not empty
							names = (names != null) ? names : new ArrayList<String>();
							names.add(name);
						}
					}
				}
			}
			if (names != null) {
				inputArrayCache = names.toArray(new String[names.size()]);
			}
		}
		return inputArrayCache;

	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#convertValue(java.lang.String[])
	 */
	@Override
	protected Collection<FileUpload> convertValue(String[] value) throws ConversionException
	{
		// convert the array of filenames into a collection of FileItems
		Collection<FileUpload> uploads = null;
		String[] filenames = getInputAsArray();

		if (filenames != null) {
			IMultipartWebRequest request = (IMultipartWebRequest)getRequest();
			uploads = new ArrayList<FileUpload>(filenames.length);
			for (int i = 0; i < filenames.length; i++) {
				for (FileItem item : request.getFile(filenames[i]))
					uploads.add(new FileUpload(item));
			}
		}
		return uploads;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.FormComponent#updateModel()
	 */
	@Override
	public void updateModel()
	{
		final Collection<FileUpload> collection = getModelObject();

		// figure out if there is an existing model object collection for us to
		// reuse
		if (collection == null)
		{
			// no existing collection, push the one we created
			setDefaultModelObject(getConvertedInput());
		}
		else
		{
			// refresh the existing collection
			collection.clear();
			if (getConvertedInput() != null)
			{
				collection.addAll(getConvertedInput());
			}

			// push the collection in case the model is listening to
			// setobject calls
			setDefaultModelObject(collection);
		}
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		// cleanup any opened filestreams
		Collection<FileUpload> uploads = getConvertedInput();
		if (uploads != null)
		{
			Iterator<FileUpload> it = uploads.iterator();
			while (it.hasNext())
			{
				final FileUpload upload = it.next();
				upload.closeStreams();
			}
		}

		// cleanup any caches
		inputArrayCache = null;

		// clean up the model because we don't want FileUpload objects in session
		Collection<FileUpload> modelObject = getModelObject();
		if (modelObject != null)
		{
			modelObject.clear();
		}

		super.onDetach();
	}
}
