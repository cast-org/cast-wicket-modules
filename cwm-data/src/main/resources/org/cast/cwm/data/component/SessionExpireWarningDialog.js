/**
 * Javascript to accompany SessionExpireWarningDialog.java
 * 
 */
var SessionExpireWarning = {

	// Parameters that can be adjusted
    warnDelay: 0,         // Time until user receives a warning
	logoutDelay: 0,       // Additional time user has to respond to the warning
    checkPeriod: 10000,   // how often to check (in ms)
    warnFunction: null,   // Callback to warn that the session is about to expire
    logoutFunction: null, // Callback if user does not respond to warning
    DEBUG: false,

    // Internal variables
    timer: null, // Timer for periodic checks
    warned: false, // If true, warning has been popped up
    warnTime: null, // Time when the warning will be displayed
    logoutTime: null, // Time when the user will be declared inactive and logged out

	/**
	 * Reset the timers to their initial state.
	 * This is called at page load, and when the warning dialog is closed by the user.
	 */
	reset: function() {		
		SessionExpireWarning.log("resetting the timers");
		SessionExpireWarning.warned = false;
        SessionExpireWarning.warnTime = new Date().getTime() + SessionExpireWarning.warnDelay;
        SessionExpireWarning.logoutTime = SessionExpireWarning.warnTime + SessionExpireWarning.logoutDelay;
	},

    /**
	 * Reset the timer unless the warning dialog is already open.
	 * This is called by the AJAX listener to monitor "activity".
	 * If the dialog is open, then the user must explicitly indicate activity.
     */
	keepAlive: function() {
		if (!SessionExpireWarning.warned)
			SessionExpireWarning.reset();
	},
	
	/**
	 * Initialize the SessionExpireWarning timer.  The timer
	 * will only function if session timeout is longer than 
	 * one minute.
	 * 
	 * @param {Object} sessionLength - length, in seconds, of the HttpSession
	 * @param {Object} warningTime - time, in seconds, before HttpSession ends to trigger a warning
	 * @param {Object} warningCallbackFunction - function that is triggered to warn the user of impending session expiration
	 * @param {Object} responseTime - time, in seconds, the user has to respond to the warning
	 * @param {Object} inactiveCallbackFunction - function that is triggered if the user does not respond to warning
	 */
	init: function(sessionLength, warningTime, warningCallbackFunction, responseTime, inactiveCallbackFunction) {
		
		SessionExpireWarning.warnFunction = warningCallbackFunction;
		SessionExpireWarning.logoutFunction = inactiveCallbackFunction;
		
		// Timeout must be at least a minute.
		if (sessionLength > warningTime && responseTime < warningTime) {
			SessionExpireWarning.warnDelay = (sessionLength - warningTime) * 1000; // In milliseconds
			SessionExpireWarning.log("Time till Warning Message = " + SessionExpireWarning.warnDelay);
			SessionExpireWarning.logoutDelay = responseTime * 1000; // In milliseconds
			SessionExpireWarning.log("Time to respond to Warning Message = " + SessionExpireWarning.logoutDelay);
			SessionExpireWarning.sessionTimeoutTime = SessionExpireWarning.warnDelay + SessionExpireWarning.logoutDelay + 2000;
			SessionExpireWarning.reset();
			SessionExpireWarning.start();
		} else {
			SessionExpireWarning.log("Invalid Time Parameters");
			return;
		}
		SessionExpireWarning.log("warningCallback =" + warningCallbackFunction);
		
	},
	
	/**
	 * Start the timer
	 */
	start: function() {
		if (SessionExpireWarning.warnDelay > 0 ) {
			// We use an interval timer since one-shot timers might never fire if, say, laptop is closed.
			SessionExpireWarning.timer = setInterval(SessionExpireWarning.check, SessionExpireWarning.checkPeriod);
			SessionExpireWarning.log("Starting Timer = " + SessionExpireWarning.timer);
		}
	},
	
	/**
	 * Stop the timer
	 */
	stop: function() {
		if (SessionExpireWarning.timer !== null) {
			clearInterval(SessionExpireWarning.timer);
			SessionExpireWarning.timer = null;
		}
		SessionExpireWarning.log("Timer Stopped");
	},

    check: function() {
	    var now = new Date().getTime();
	    if (now > SessionExpireWarning.logoutTime) {
            SessionExpireWarning.log("Logging out: current time " + now + " is after " + SessionExpireWarning.logoutTime);
            SessionExpireWarning.sessionTimeout();
        } else if (now > SessionExpireWarning.warnTime) {
	        if (!SessionExpireWarning.warned) {
                SessionExpireWarning.warn();
            } else {
	            SessionExpireWarning.log("Remaining until logout: " + (SessionExpireWarning.logoutTime-now)/1000 + "s");
            }
        } else {
	        SessionExpireWarning.log("Remaining until warning: " + (SessionExpireWarning.warnTime-now)/1000 + "s");
        }
    },

	/**
	 * Warn user (and trigger inactive timer)
	 */
	warn: function() {
		SessionExpireWarning.log("Warning Function Called");
        SessionExpireWarning.warned = true;
		if (typeof SessionExpireWarning.warnFunction === 'function') {
			SessionExpireWarning.warnFunction();
		} else {
			alert("Warning: Your login session is about to expire.");
		}
	},

	/**
	 * Session is inactive and should be forced to home upon re-opening browser window
	 */
	sessionTimeout: function() {
		SessionExpireWarning.logoutFunction();
	},

	/**
	 * Log a message to Firebug's console
	 * 
	 * @param {Object} val
	 */
	log: function(val) {
		if (SessionExpireWarning.DEBUG && window.console && window.console.log)
			window.console.log(val);
	}
};


