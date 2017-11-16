/**
 * Javascript to accompany SessionExpireWarningDialog.java
 * 
 */
var SessionExpireWarning = {

    dialogId: null,                 // Markup ID of the warning dialog
    tickPeriod: 10000,              // how often to wake up and check the time (in ms)
    nextCheckTime: null,            // Time when we should next check with server for status
    checkEventName: null,           // Event that will be triggered to check with server
    refreshEventName: null,         // Event that will be triggered when the warning dialog is closed by the user
    homePage: null,                 // URL to redirect to on logout
    timer: null,                    // Timer for periodic checks
    ignoreDialogClose: false,       // Set temporarily to true when we're programmatically closing the dialog
    debug: false,                   // whether debugging information should be logged to the console.

    /**
     * Initialize the SessionExpireWarning timer.
     *
     * @param dialogId
     * @param secondsUntilCheck
     * @param checkEventName
     * @param refreshEventName
     * @param homePage
     * @param debug
     */
	init: function(dialogId, secondsUntilCheck, checkEventName, refreshEventName, homePage, debug) {
	    SessionExpireWarning.dialogId = dialogId;
        SessionExpireWarning.setNextCheck(secondsUntilCheck);
        SessionExpireWarning.checkEventName = checkEventName;
        SessionExpireWarning.refreshEventName = refreshEventName;
        SessionExpireWarning.homePage = homePage;
        SessionExpireWarning.debug = debug;
        SessionExpireWarning.startTimer();

        $('#' + dialogId).on('afterHide.cfw.modal', SessionExpireWarning.dialogClosed);
	},
	
	/**
	 * Start the timer
	 */
	startTimer: function() {
		// We use an interval timer since one-shot timers might never fire if, say, laptop is closed for a while.
		SessionExpireWarning.timer = setInterval(SessionExpireWarning.tick, SessionExpireWarning.tickPeriod);
		SessionExpireWarning.log("Starting Timer = " + SessionExpireWarning.timer);
	},
	
	/**
	 * Stop the timer (not normally used)
	 */
	stopTimer: function() {
		if (SessionExpireWarning.timer !== null) {
			clearInterval(SessionExpireWarning.timer);
			SessionExpireWarning.timer = null;
		}
		SessionExpireWarning.log("Timer Stopped");
	},

    /**
     * Called by timer periodically; if it is time to check in with the server this will trigger the appropriate event.
     */
    tick: function() {
        // Is it time to check with the server?
        if (SessionExpireWarning.nextCheckTime !== null) {
            var now = new Date().getTime();
            if (now > SessionExpireWarning.nextCheckTime) {
                SessionExpireWarning.log("Checking...");
                SessionExpireWarning.nextCheckTime = null;
                $('#' + SessionExpireWarning.dialogId).trigger(SessionExpireWarning.checkEventName);
            } else {
                SessionExpireWarning.log("Remaining until check: " +
                    Math.ceil((SessionExpireWarning.nextCheckTime - now)/1000) + "s");
            }
        } else {
            window.console.log("SessionExpireWarning error - no next check time");
        }
    },

    /**
     * Called by the server to request that the client should check in after a given period of time.
     *
     * @param secondsUntilNextCheck
     */
    setNextCheck: function(secondsUntilNextCheck) {
        SessionExpireWarning.log("Next check time: " + secondsUntilNextCheck);
        SessionExpireWarning.nextCheckTime = new Date().getTime() + secondsUntilNextCheck*1000;
    },

    /**
     * Called by the server when we're in the warning zone.
     */
    warning: function() {
        // Open the dialog if it is not open already
        if ($('#' + SessionExpireWarning.dialogId + ":visible").length === 0) {
            $('#' + SessionExpireWarning.dialogId).CFW_Modal('show');
            if (CwmPageTiming)
                CwmPageTiming.blocked(true);
        }
    },

    /**
     * Called by the server when check result is that the session is alive and well.
     */
    clearWarning: function() {
        // Close the warning dialog if it is open.
        if ($('#' + SessionExpireWarning.dialogId + ":visible").length) {
            // Set this flag so that we'll ignore the afterHide.cfw.modal callback.
            SessionExpireWarning.ignoreDialogClose = true;
            var dialog = $('#' + SessionExpireWarning.dialogId);
            dialog.CFW_Modal('hide');
        }
    },

    /**
     * Callback function invoked when warning dialog is closed.
     */
    dialogClosed: function() {
        // The dialog box may be closed in two ways:
        // 1. Programmatically closed via clearWarning() method due to activity in another window.
        // 2. Closed by the user clicking on it, in which case we need to notify the server of new activity.
        if (SessionExpireWarning.ignoreDialogClose) {
            SessionExpireWarning.log("Dialog closed programmatically, not notifying server");
            SessionExpireWarning.ignoreDialogClose = false;
        } else {
            SessionExpireWarning.log("User closed the warning dialog, notifying server of activity");
            $('#' + SessionExpireWarning.dialogId).trigger(SessionExpireWarning.refreshEventName);
        }
        if (CwmPageTiming)
            CwmPageTiming.blocked(false);
    },

    expired: function() {
        SessionExpireWarning.log("Session expired");
        $(window).off('beforeunload');
        window.location = SessionExpireWarning.homePage;
    },

    checkFailed: function(attrs, xhr, message, failType) {
        SessionExpireWarning.log("Check failure: attrs=" + attrs + "; type=" + failType + "; message=" + message);
    },

    /**
	 * Log a message to Firebug's console if debugging is turned on.
	 */
	log: function(message) {
		if (SessionExpireWarning.debug && window.console && window.console.log)
			window.console.log(message);
	}

};


