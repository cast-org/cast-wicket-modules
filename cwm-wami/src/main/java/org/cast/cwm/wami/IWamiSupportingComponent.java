package org.cast.cwm.wami;

/**
 * Indicates a component that supports the wami audio recorder by having a WamiAppletHolder on the page.
 * Use of this interface is not required may be convenient for early detection of cases where the holder has
 * been omitted.
 *  
 * @author bgoldowsky
 *
 */
public interface IWamiSupportingComponent {

	public WamiAppletHolder getWamiAppletHolder();
	
}
