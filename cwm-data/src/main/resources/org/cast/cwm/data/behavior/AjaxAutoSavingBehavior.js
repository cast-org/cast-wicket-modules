/**
 * Javascript to accompany AjaxAutoSavingBehavior.java
 * 
 * Uses Wicket's native wicketSubmitFormById() javascript function to submit the form via ajax.  Each form with this behavior 
 * is registered with the AutoSaver object so that it knows the Wicket Callback URL and starting values.
 *
 */
var AutoSaver = {
		
    autoSaveInterval: 30000,   // time between autosaves, in ms
    onBeforeSaveCallBacks: [], // Collection of callbacks to run before checking if the form needs to be saved.
    formsInProgress: [], // Collection of forms that are being saved

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
            if (newValues !== oldValues) {
                log.debug("AutoSave: form ", $(this).attr('id'), " changed");
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
                log.error("Removing failing callback: ", value, "cause:", err);
                AutoSaver.onBeforeSaveCallBacks.splice(key, 1) /* Probably a stale callback; remove it. */
            }
        });

        log.debug("Running AutoSave Processing...");
        AutoSaver.indicateInProgress();

        var statusObject = { hasErrors: false, callbackDone: false }; // this will collect any errors

        $("form.ajaxAutoSave").each(function() {

                var newValues = $(this).serialize();

                if (newValues === $(this).data('autosaveOrigValues')) {
                    log.debug("AutoSave: form ", $(this).attr('id'), " not changed");
                } else {
                    AutoSaver.addFormToProcessing($(this).attr('id'));
                    var form = $(this);
                    var id = form.attr('id');
                    var callback = form.data('ajaxCallbackUrl');
                    if (callback == null) {
                        log.warn("AutoSave: form ", id, " has no callback URL set");
                    } else {
                        log.debug("AutoSave: saving form ", id, " to ", callback);
                        Wicket.Ajax.post({ u: callback,
                            f: form.attr('id'),
                            ep: {autosave: 'true'},
                            sh: [function() { // Success Handler
                                form.data('autosaveOrigValues', newValues);
                            }],
                            fh: [function(attrs, jqXHR, errorMessage, textStatus) { // Failure Handler
                                log.error("Autosave Failed for form ", id)
                                AutoSaver.indicateError();
                                statusObject.hasErrors = true;
                            }],
                            coh: [function() { // Complete handler
                                AutoSaver.removeFormFromProcessing(id);
                                AutoSaver.runIfNoneProcessing(statusObject, onSuccessCallBack);
                            }]
                        });

                    }
                }
        });

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
        log.debug("AutoSaving form: ", id, "; now in progress: ", AutoSaver.formsInProgress.length);
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
            log.debug("Finished AutoSaving form: ", id, "; in progress: ",  AutoSaver.formsInProgress.length);
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
        if ($.isFunction(fn) && AutoSaver.formsInProgress.length === 0) {
            if (!statusObj.hasErrors && !statusObj.callbackDone) {
                fn.call();
                log.debug("AutoSave Complete, callback run.");
                statusObj.callbackDone = true;
            }
        }
    },

    /**
     * Recursive timed loop on itself.  Calls autoSaveMaybeSave(), updates display messages,
     * and then sets another timeout on itself.
     *
     */
    recurringAutoSave: function() {
        AutoSaver.autoSaveMaybeSave(function() {
            AutoSaver.indicateSuccess();
        });
        window.setTimeout(AutoSaver.recurringAutoSave, AutoSaver.autoSaveInterval);
    },

    /**
     * onClick handler for 'exiting' links that calls autoSaveMaybeSave()
     *
     */
    autoSaveOnLink: function(event) {
        AutoSaver.autoSaveMaybeSave(function() {
            log.info("Page Exit Autosave Success; redirecting to " + $(event.currentTarget).attr("href"));
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
                && url.indexOf('#') !== 0
                && url.indexOf("javascript") !== 0
                && url.indexOf(".mp3") !== (url.length - 4)
                && url.indexOf(base + "#") !== 0);
    },

    /**
     * Bind an autosave callback to all <a> and <area> links that exit the page.
     */
    makeLinksSafe: function() {
        var base = window.location.href;
        $('a, area').each(function() {
            if ($(this).data("ParsedAutoSaveLink") !== true && !this.onclick && !this.target
                     && AutoSaver.needsHandler($(this).attr('href'), base)) {
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
     * @param {Number} updateInterval interval between save checks in ms.
     */
    setup: function(updateInterval) {
         AutoSaver.autoSaveInterval = updateInterval;
         window.setTimeout(AutoSaver.recurringAutoSave, AutoSaver.autoSaveInterval);
    },

    /**
     * Add a form to this AutoSaver.
     *
     * @param {Object} formId the 'id' of the form
     * @param {Object} ajaxCallbackUrl the Wicket-generated callback URL for this AjaxSubmit
     */
    addForm: function(formId, ajaxCallbackUrl) {
        var form = $('#' + formId);
        form.data('autosaveOrigValues', form.serialize()); // Original Form Values to check for changes
        form.data('ajaxCallbackUrl', ajaxCallbackUrl);
        log.info("AutoSave: Registering Form ", formId, " with URL ", form.data('ajaxCallbackUrl'));
    },

    indicateInProgress: function() {
        $('.autosave_info').html('Auto Saving...');
    },

    indicateError: function() {
        $('.autosave_info').html('<strong class="text-danger">Error: could not save!</strong>');
    },

    indicateSuccess: function() {
        $('.autosave_info').html('<strong>Last Saved:</strong> ' + new Date().toLocaleTimeString());
    }

};



