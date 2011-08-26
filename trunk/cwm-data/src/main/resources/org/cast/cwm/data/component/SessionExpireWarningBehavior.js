/**
 * Javascript to accompany SessionExpireWarningBehavior.java
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
    
	/**
	 * Reset the timer
	 */
	reset: function() {		
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
	 * @param {Object} warningCallback - function that is triggered to warn the user of impending session expiration
	 * @param {Object} responseTime - time, in seconds, the user has to respond to the warning
	 * @param {Object} inactiveCallback - function that is triggered if the user does not respond to warning
	 */
	init: function(sessionLength, warningTime, warningCallback, responseTime, inactiveCallback) {
		
		SessionExpireWarning.warnFunction = warningCallback;
		SessionExpireWarning.inactiveFunction = inactiveCallback;
		
		// Timeout must be at least a minute.
		if (sessionLength > warningTime && responseTime < warningTime) {
			SessionExpireWarning.timeoutLength = (sessionLength - warningTime) * 1000; // In milliseconds
			SessionExpireWarning.responseTime = responseTime * 1000; // In milliseconds
			SessionExpireWarning.start();
		} else {
			SessionExpireWarning.log("Invalid Time Parameters");
			return;
		}
		
	},
	
	/**
	 * Start the timer
	 */
	start: function() {
		if (SessionExpireWarning.timeoutLength > 0 ) {
			SessionExpireWarning.warnEvent = setTimeout(SessionExpireWarning.warn, SessionExpireWarning.timeoutLength);
			SessionExpireWarning.log("Starting Timer");
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
		SessionExpireWarning.log("Timer Cleared");
	},		

	/**
	 * Warn user (and trigger inactive timer)
	 */
	warn: function() {
		if (typeof SessionExpireWarning.warnFunction == 'function') {
			SessionExpireWarning.warnFunction();
		} else {
			alert("Warning: Your login session is about to expire.");
		}
		
		if (SessionExpireWarning.responseTime > 0 && typeof SessionExpireWarning.inactiveFunction == 'function' ) {
				SessionExpireWarning.inactiveEvent = setTimeout(SessionExpireWarning.inactiveFunction, SessionExpireWarning.responseTime);
		}
	},

	/**
	 * Log a message to Firebug's console
	 * 
	 * @param {Object} val
	 */
	log: function(val) {
		if (SessionExpireWarning.DEBUG && window.console && window.console.firebug)
			console.log(val);
	}
};


