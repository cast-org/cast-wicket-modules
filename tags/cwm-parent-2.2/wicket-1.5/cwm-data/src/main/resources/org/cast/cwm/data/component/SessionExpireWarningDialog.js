/**
 * Javascript to accompany SessionExpireWarningDialog.java
 * 
 */
var SessionExpireWarning = {

    DEBUG: false, // If true, will print logging messages in Firebug
    timeoutLength: 0, // Time until user receives a warning
	responseTime: 0,  // Time user has to respond to the warning
    warnEvent: null, // Timer event for warnFunction
	inactiveEvent: null, // Timer event for inactiveFunction
	warnFunction: null, // Callback to warn that the session is about to expire
	inactiveFunction: null, // Callback if user does not respond to warning
	sessionTimeoutEvent: null, // Timer after timeout has occurred
	sessionTimeoutTime: 0, // Time until user is forced to home page
	sessionTimeoutIntervalEvent: null, // Interval Timer to force user to home page
	sessionTimeoutIntervalTime: 10000, // how often to check if the user is now active after timing out
    
	/**
	 * Reset the timer
	 */
	reset: function() {		
		SessionExpireWarning.log("resetting the timers");
		SessionExpireWarning.clear();
		SessionExpireWarning.start();
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
		SessionExpireWarning.inactiveFunction = inactiveCallbackFunction;
		
		// Timeout must be at least a minute.
		if (sessionLength > warningTime && responseTime < warningTime) {
			SessionExpireWarning.timeoutLength = (sessionLength - warningTime) * 1000; // In milliseconds
			SessionExpireWarning.log("Time till Warning Message = " + SessionExpireWarning.timeoutLength);
			SessionExpireWarning.responseTime = responseTime * 1000; // In milliseconds
			SessionExpireWarning.log("Time to respond to Warning Message = " + SessionExpireWarning.responseTime);
			SessionExpireWarning.sessionTimeoutTime = SessionExpireWarning.timeoutLength + SessionExpireWarning.responseTime + 2000;
			SessionExpireWarning.log("Time to close session = " + SessionExpireWarning.sessionTimeoutTime);
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
		if (SessionExpireWarning.timeoutLength > 0 ) {
			// execute the warning function at timeoutLength
			SessionExpireWarning.warnEvent = setTimeout(SessionExpireWarning.warn, SessionExpireWarning.timeoutLength);
			SessionExpireWarning.log("Starting Warning Timer = " + SessionExpireWarning.timeoutLength);
			
			// if the browser window is closed or asleep during inactive warning/timeout - keep checking after the
			// timeout has occurred
			SessionExpireWarning.sessionTimeoutEvent = setTimeout(SessionExpireWarning.sessionTimeout, SessionExpireWarning.sessionTimeoutTime);
			SessionExpireWarning.log("Starting Timeout Timer = " + SessionExpireWarning.sessionTimeoutTime);
		}
	},
	
	/**
	 * Clear all timeout events
	 */
	clear: function() {
		if (SessionExpireWarning.warnEvent != null) {
			clearTimeout(SessionExpireWarning.warnEvent);
		}
		if (SessionExpireWarning.inactiveEvent != null) {
			clearTimeout(SessionExpireWarning.inactiveEvent);
		}
		if (SessionExpireWarning.sessionTimeoutEvent != null) {
			clearTimeout(SessionExpireWarning.sessionTimeoutEvent);
		}
		if (SessionExpireWarning.sessionTimeoutIntervalEvent != null) {
			clearInterval(SessionExpireWarning.sessionTimeoutIntervalEvent);
		}
		SessionExpireWarning.log("Timers Cleared");
	},		

	/**
	 * Warn user (and trigger inactive timer)
	 */
	warn: function() {
		SessionExpireWarning.log("Warning Function Called");
		if (typeof SessionExpireWarning.warnFunction == 'function') {
			SessionExpireWarning.warnFunction();
		} else {
			alert("Warning: Your login session is about to expire.");
		}
		SessionExpireWarning.log("Setting inactive timer to " + SessionExpireWarning.responseTime);
		if (SessionExpireWarning.responseTime > 0 && typeof SessionExpireWarning.inactiveFunction == 'function' ) {
				// execute the inactiveFunction at responseTime
				SessionExpireWarning.inactiveEvent = setTimeout(SessionExpireWarning.inactiveFunction, SessionExpireWarning.responseTime);
		}
	},

	/**
	 * Session is inactive and should be forced to home upon re-opening browser window
	 */
	sessionTimeout: function() {
		SessionExpireWarning.log("Session Timed Out - forcing home page ");
		if (SessionExpireWarning.sessionTimeoutTime > 0 && typeof SessionExpireWarning.inactiveFunction == 'function' ) {
				// execute the inactiveFunction repeatedly
				SessionExpireWarning.sessionTimeoutIntervalEvent = setInterval(SessionExpireWarning.inactiveFunction, SessionExpireWarning.sessionTimeoutIntervalTime);
		}
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


