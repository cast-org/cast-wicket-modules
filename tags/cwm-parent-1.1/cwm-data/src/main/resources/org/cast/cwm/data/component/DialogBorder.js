/**
 * Javascript to accompany DialogBorder.java
 */

/**
 * Adds $(selector).center() extension to jQuery.
 */
jQuery.fn.center = function() {
	this.css("position","absolute");
	this.css("top",(jQuery(window).height()-this.height())/2+jQuery(window).scrollTop()+"px");
	this.css("left",(jQuery(window).width()-this.width())/2+jQuery(window).scrollLeft()+"px");
	return this
};

/**
 * Methods for focusing on Dialogs and calling buttons.
 */
var DialogBorder = {

    DEBUG: false, // If true, will print logging messages in Firebug
    
	/**
	 * Set keyboard focus on the given dialog.  If the dialog is not a focusable element,
	 * e.g. &lt;a&gt; or &lt;input&gt;, then it must have a tabindex attribute.
	 * 
	 * You can specify whether this is the 'first' dialog and should therefore store
	 * a reference to the button in the page which opened this dialog.  In a series of
	 * dialogs, this is unnecessary since each dialog will set it's own focus.
	 * 
	 * @param {Object} dialogId - markup ID of the dialog
	 * @param {Object} storeButton - true if this is the first dialog to be opened; false otherwise
	 */
	focusDialog: function(dialogId, storeButton) {
		var $dialog = $('#' + dialogId);
		var $document = $(document);
				
		if (storeButton && !$document.data("DialogTrigger")) {
			var $trigger = $(document.activeElement);
			$document.data("DialogTrigger", $trigger.attr("id"));
			DialogBorder.log("Storing button: " + $trigger.attr("id"));
		}
		
		$dialog.get(0).focus();		
	},
	
	/**
	 * Return focus to a previously stored button.  If there is no previously stored button,
	 * or the button no longer exists on the page, you can specify a fallback jQuery selector.
	 * 
	 * Failsafe behavior does nothing.
	 * 
	 * @param {Object} fallbackSelector
	 */
	focusButton: function(fallbackSelector) {
		
		var $document = $(document);
		
		if ($document.data("DialogTrigger")) {
			var $trigger = $("#" + $document.data("DialogTrigger"));
			$document.data("DialogTrigger", null);
			if ($trigger.get(0)) {
				$trigger.get(0).focus();
				DialogBorder.log("Focusing on button: " + $trigger.attr("id"));
				return;
			}
		}
		
		if (fallbackSelector) {
			var $fallback = $(fallbackSelector);
			if ($fallback.get(0)) {
				$fallback.get(0).focus();
				DialogBorder.log("Focusing on button: " + $fallback.attr("id"));
				return;
			}
		}
		
		DialogBorder.log("Failed to focus on a button");
	},
	
	/**
	 * Log a message to Firebug's console
	 * 
	 * @param {Object} val - message to be logged
	 */
	log: function(val) {
		if (DialogBorder.DEBUG && window.console && window.console.firebug)
			console.log(val);
	}
};


