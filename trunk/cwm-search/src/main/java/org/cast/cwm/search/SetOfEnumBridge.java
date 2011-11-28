package org.cast.cwm.search;

import java.util.Collection;

import org.hibernate.search.bridge.builtin.StringBridge;

public class SetOfEnumBridge extends StringBridge {

	@Override
	public String objectToString(Object object) {
		@SuppressWarnings("unchecked")
		Collection<? extends Enum<?>> coll = (Collection<? extends Enum<?>>) object;
		if (coll==null)
			return null;
		StringBuffer buf = new StringBuffer("");
		for (Enum<?> e : coll) {
			if (buf.length()>0)
				buf.append(' ');
			buf.append(e.ordinal());
		}
		return buf.toString();
	}

}
