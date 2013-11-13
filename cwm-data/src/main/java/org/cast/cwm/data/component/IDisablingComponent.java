package org.cast.cwm.data.component;

import java.util.Collection;

import org.apache.wicket.Component;

/**
 * An AJAX component that disables other components while it is doing its AJAX behavior.
 *
 */
public interface IDisablingComponent {

	/**
	 * Returns the components that should be disabled while AJAX is processing.
	 * @return collection of Components.
	 */
	public abstract Collection<? extends Component> getComponents();

}
