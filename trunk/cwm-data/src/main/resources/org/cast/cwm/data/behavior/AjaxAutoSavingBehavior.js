/**
 * Javascript to accompany AjaxAutoSavingBehavior.java
 * 
 * Uses Wicket's native wicketSubmitFormById() javascript function to submit the form via ajax.  Each form with this behavior 
 * is registered with the AutoSaver object so that it knows the Wicket Callback URL and starting values.
 * 
 * TODO: Create per-form Autosave messages and callbacks.
 * 
 */
var AutoSaver = {
		
	    DEBUG: false, // If true, will print logging messages in Firebug
	    autoSaveInterval: 30000,  // time between autosaves, in ms
	    onBeforeSaveCallBacks: new Array(), // Collection of callbacks to run before checking if the form needs to be saved.
	    formsInProgress: new Array(), // Collection of forms that are being saved
	    
		/**
		 * Check to see if any forms need saving.
		 * 
		 */
		autoSaveCheck: function() {

	        $.each(AutoSaver.onBeforeSaveCallBacks, function(key, value) {
				try {
					value.call();
				} catch (err) {
					AutoSaver.onBeforeSaveCallBacks.splice(key, 1) /* Probably a stale callback; remove it. */
				}
	        });

			var needsave = false;

			$("form.ajaxAutoSave").each(function() {
                var newValues = $(this).serialize();
				var oldValues = $(this).data('autosaveOrigValues');
                if (newValues != oldValues) {
                    AutoSaver.logger("AutoSave: form " + $(this).attr('id') + " changed");
                    AutoSaver.logger("Old: " + oldValues);
                    AutoSaver.logger("New: " + newValues);
                    needsave = true;
                }
	        });
			return needsave;
		},

		/**
		 * Check to see if any forms need saving and save them.
		 * 
		 * @param {Object} onSuccessCallBack - callback run after all forms are processed
		 */
		autoSaveMaybeSave: function(onSuccessCallBack) {
			
	        $.each(AutoSaver.onBeforeSaveCallBacks, function(key, value) {
				try {
					value.call();
				} catch (err) {
					AutoSaver.logger("Removing failing callback: ", value, "cause:", err);
					AutoSaver.onBeforeSaveCallBacks.splice(key, 1) /* Probably a stale callback; remove it. */
				}
	        });
			
			AutoSaver.logger("Running AutoSave Processing...");
			
			$('.autosave_info').each(function() {	
            	$(this).html('<strong>Auto Saving...</strong>');
            });

			var statusObject = { hasErrors: false, callbackDone: false }; // this will collect any errors
			
	        $("form.ajaxAutoSave").each(function() {

	                var newValues = $(this).serialize();
					
	                if (newValues == $(this).data('autosaveOrigValues')) {
	                    AutoSaver.logger("AutoSave: form " + $(this).attr('id') + " not changed");
	                } else {
	                    AutoSaver.addFormToProcessing($(this).attr('id'));
						var form = $(this);
						var id = form.attr('id');
						var callback = form.data('ajaxCallbackUrl');
						if (callback == null) {
							AutoSaver.logger("AutoSave: form " + id + " has no callback URL set");
						} else {
							AutoSaver.logger("AutoSave: saving form " +id + " to " + callback);
							Wicket.Ajax.post({ u: callback, 
								f: form.attr('id'), 
								ep: {autosave: 'true'},
								sh: [function() { // Success Handler
									form.data('autosaveOrigValues', newValues);
								}],
								fh: [function() { // Failure Handler
									alert ("Autosave Failed for form: " + id);
									AutoSaver.logger("Autosave Failed for form ", id);
									statusObject.hasErrors = true;
								}],
								coh: [function() { // Complete handler
									AutoSaver.removeFormFromProcessing(id);
									AutoSaver.runIfNoneProcessing(statusObject, onSuccessCallBack);
								}],
							});	

						}
	                }
	        });
			
			AutoSaver.logger("AutoSave AJAX calls complete...");
			
			// Run callback in case there were no changes that needed to be saved.
			AutoSaver.runIfNoneProcessing(statusObject, onSuccessCallBack);
	    },
		
		/**
		 * Register a form that is processing.
		 * 
		 * see: AutoSaver.runIfNoneProcessing(status, fn)
		 * 
		 * @param {Object} id
		 */
		addFormToProcessing: function(id) {
			AutoSaver.formsInProgress.push(id);
			AutoSaver.logger("AutoSaving form: ", id, "; now in progress: ", AutoSaver.formsInProgress.length);
		},
		
		/**
		 * Indicate that a form is done processing.
		 * 
		 * see: AutoSaver.runIfNoneProcessing(status, fn)
		 * 
		 * @param {Object} id - id of the form
		 */
		removeFormFromProcessing: function(id) {
			var index = $.inArray(id, AutoSaver.formsInProgress);
			if (index > -1) {
				AutoSaver.formsInProgress.splice(index, 1);
				AutoSaver.logger("Finished AutoSaving form: ", id, "; in progress: ",  AutoSaver.formsInProgress.length);
			}
		},
		
		/**
		 * Will run the callback function if no forms are being processed.
		 * 
		 * see: AutoSaver.addFormToProcessing(id)
		 * see: AutoSaver.removeFormFromProcessing(id)
		 * 
		 * @param {Object} statusObj - holds status information booleans
		 * @param {Object} fn
		 */
		runIfNoneProcessing: function(statusObj, fn) {
			if ($.isFunction(fn) && AutoSaver.formsInProgress.length == 0) {
				if (!statusObj.hasErrors && !statusObj.callbackDone) {
					fn.call();
					AutoSaver.logger("AutoSave Complete, callback run.");
					statusObj.callbackDone = true;
				}
			}
		},
		
		/**
		 * Get a friendly Clock Time for display.
		 */
		getClockTime: function() {
	    	var now    = new Date();
	        var hour   = now.getHours();
	        var minute = now.getMinutes();
	        var second = now.getSeconds();
	        var ap = "AM";
	        if (hour   > 11) { ap = "PM";             }
	        if (hour   > 12) { hour = hour - 12;      }
	        if (hour   == 0) { hour = 12;             }
	        if (hour   < 10) { hour   = "0" + hour;   }
	        if (minute < 10) { minute = "0" + minute; }
	        if (second < 10) { second = "0" + second; }
	        var timeString = hour + ':' + minute + ':' + second + " " + ap;
	        return timeString;
	    },
		
		/**
		 * Recursive timed loop on itself.  Calls autoSaveMaybeSave(), updates display messages,
		 * and then sets another timeout on itself.
		 * 
		 */
	    recurringAutoSave: function() {
	    	AutoSaver.autoSaveMaybeSave(function() {
       			$('.autosave_info').each(function() {	
                        $(this).html('<strong>Last Saved:</strong> ' + AutoSaver.getClockTime());
                });
       		});
	    	window.setTimeout(AutoSaver.recurringAutoSave, AutoSaver.autoSaveInterval);
	    },
	    
	    /**
	     * onClick handler for 'exiting' links that calls autoSaveMaybeSave()
	     * 
	     */
	    autoSaveOnLink: function(event) {
	        AutoSaver.autoSaveMaybeSave(function() {
				AutoSaver.logger("Page Exit Autosave Success; redirecting to " + $(event.currentTarget).attr("href"));
				window.location = $(event.currentTarget).attr("href");
			});
			return false; // Prevent propagation
	    },

		/**
		 * Determines whether a link leaves the page and, therefore, needs an autosave handler.
		 * 
		 * @param {Object} url the link
		 * @param {Object} base the base page (to eliminate in-page links)
		 */
	    needsHandler: function(url, base) {
	        return (url
	                && url.indexOf('#') != 0 
	                && url.indexOf("javascript") != 0 
	                && url.indexOf(".mp3") != (url.length - 4) 
	                && url.indexOf(base + "#") != 0); 
	    },

		/**
		 * Bind an autosave callback to all <a> and <area> links that exit the page.
		 */
	    makeLinksSafe: function() {
	        var base = window.location.href;
	        $('a, area').each(function() {
	        	if ($(this).data("ParsedAutoSaveLink") != true && !this.onclick && !this.target && AutoSaver.needsHandler($(this).attr('href'), base)) {
        			$(this).bind("click", AutoSaver.autoSaveOnLink);
					$(this).data("ParsedAutoSaveLink", true);
        		}
	        });
	    },
	    
		/**
		 * Add a function that will be called before a save is attempted.  Useful for converting
		 * input and storing in a form in cases like TinyMCE and SVG-Edit.
		 * 
		 * @param {Object} fn
		 */
	    addOnBeforeSaveCallBack: function(fn) {
	    	if ($.isFunction(fn)) {
	    		AutoSaver.onBeforeSaveCallBacks.push(fn);
	    		return true;
	    	}
	    	return false;
	    },
		
		/**
		 * Start the timer on the auto-save.
		 */
		setup: function(updateInterval) {
			 AutoSaver.autoSaveInterval = updateInterval;
	         window.setTimeout(AutoSaver.recurringAutoSave, AutoSaver.autoSaveInterval);
	    },
		
		/**
		 * Add a form to this AutoSaver.
		 * 
		 * @param {Object} saveId the 'id' of the link
		 * @param {Object} formId the 'id' of the form
		 * @param {Object} callbackUrl the Wicket-generated callback URL for this AjaxSubmit
		 */
		addForm: function(formId, ajaxCallbackUrl) {
			$('#' + formId).data('autosaveOrigValues', $('#' + formId).serialize()); // Original Form Values to check for changes
			$('#' + formId).data('ajaxCallbackUrl', ajaxCallbackUrl);
			AutoSaver.logger("AutoSave: Registering Form " + formId + " with URL " + $('#' + formId).data('ajaxCallbackUrl'));
		},
		
		// No-op by default, but will be Firebug's console.log if DEBUG is set and console.log is available.
		logger: function() { }
};

if (AutoSaver.DEBUG && window.console) {
	AutoSaver.logger = console.log;
}


