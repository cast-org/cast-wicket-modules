/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.xml.transform;

import org.apache.wicket.util.time.Time;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A transform that is composed of a chain of other transformers.
 * When applyTransform is called, it will run each of the sub-transforms in turn on the provided DOM.
 *
 * @author borisgoldowsky
 *
 */
public class TransformChain implements IDOMTransformer {
	
	protected List<IDOMTransformer> transforms;
	
	/**
	 * Construct a chain from zero or more specified transformers.
	 * @param newtransformers
	 */
	public TransformChain(IDOMTransformer...newtransformers) {
		transforms = new ArrayList<IDOMTransformer>(Arrays.asList(newtransformers));
	}
	
	/** Add an additional transform to the end of this chain.
	 * 
	 * @param item the new transformer
	 * @return this, for chaining
	 */
	public TransformChain add (IDOMTransformer item) {
		transforms.add(item);
		return this; // for chaining
	}
	
	/**
	 * Add a new transformer at a specified position within the chain.
	 * @param index position at which to insert the item (0=first transformer run)
	 * @param item the new transformer
	 * @return this, for chaining
	 */
	public TransformChain add (int index, IDOMTransformer item) {
		transforms.add(index, item);
		return this;
	}

	/**
	 * Apply the chain of transforms to the given DOM and return the result.
	 */
	@Override
	public Element applyTransform(Element n, TransformParameters params) {
		for (IDOMTransformer trans : transforms) {
			n = trans.applyTransform(n, params);
			if (n == null)
				break; // Some transforms can reduce the Element to null
		}
		return n;
	}

	/**
	 * Last modified date of any transform in the chain.
	 * If all report null, then null will be returned (meaning the entire chain is unmodifiable).
	 */
	@Override
	public Time getLastModified(TransformParameters params) {
		Time lastMod = null;
		for (IDOMTransformer trans : transforms) {
			Time d = trans.getLastModified(params);
			if (d != null) {
				if (lastMod == null || lastMod.before(d))
					lastMod = d;
			}
		}
		return lastMod;
	}

}
