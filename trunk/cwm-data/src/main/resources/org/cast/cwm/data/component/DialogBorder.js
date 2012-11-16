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
				
		if (storeButton && !$dialog.data("DialogTrigger")) {
			// store trigger data with the dialog and at the global level for chaining
			var $trigger = $(document.activeElement);
			DialogBorder.log("Storing button : " + $trigger.attr("id"));
			$dialog.data("DialogTrigger", $trigger.attr("id"));
			$document.data("DialogTrigger", $trigger.attr("id"));
		}
		
		$dialog.get(0).focus();		
	},
	
	/**
	 * Return focus to a previously stored button.  If there is no previously stored button,
	 * or the button no longer exists on the page, you can specify a fallback jQuery selector.
	 * 
	 * Failsafe behavior does nothing.
	 * 
	 * @param {Object} focusOverride
	 */
	focusButton: function(dialogId, focusOverride) {
		
		var $dialog = $('#' + dialogId);
		var $document = $(document);
		DialogBorder.log("Dialog id: " + $dialog.attr("id"));
		DialogBorder.log("focusOverride: " + focusOverride);
		
		// return focus here if specified
		if (focusOverride) {
			var $fallback = $(focusOverride);
			if ($fallback.get(0)) {
				$fallback.get(0).focus();
				DialogBorder.log("Focusing on button: " + $fallback.attr("id"));
				return;
			}
		}

		// fallback to data stored at dialog creation time
		if ($dialog.data("DialogTrigger")) {
			var $trigger = $("#" + $dialog.data("DialogTrigger"));
			if ($trigger.get(0)) {
				$trigger.get(0).focus();
				DialogBorder.log("Focusing on button: " + $trigger.attr("id"));
				return;
			}
		}

		// fallback to data stored at global level - used for chaining modals
		if ($document.data("DialogTrigger")) {
			var $trigger = $("#" + $document.data("DialogTrigger"));
			if ($trigger.get(0)) {
				$trigger.get(0).focus();
				DialogBorder.log("Focusing on button: " + $trigger.attr("id"));
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

/*=========================================================*/
/*  Modal Close vis Esc key	                               */
/*=========================================================*/
function modalEscClose() {
    $(document.body).delegate(".modalBody", "keyup", function(event) {
            var code=event.charCode || event.keyCode;
            if(code && code == 27) {// if ESC is pressed
                // Click the close button
                $(this).find(".modalClose").eq(0).click();
        }
    });
}

$(window).ready(function() {
    modalEscClose();
});
