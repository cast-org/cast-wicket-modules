/**
 * page-timing.js
 *
 * Tracks two pieces of timing information about pages to be reported to the server:
 * How long the page takes to load
 * How long the page remains loaded before the user leaves the page or its window is closed.
 *
 * An AJAX request cannot be initiated as the page is being unloaded, so instead the information
 * is cached in LocalStorage and then sent to the server by a subsequent page that also loads this file.
 *
 * This file is loaded and its methods called by org.cast.cwm.data.LoggedWebPage.
 */

CwmPageTiming = {};

/**
 * Call to initiate time tracking of this page.
 * @param eventId the ID that we use to identify this page view event to the server.
 * @param url the URL to which to post the timing information
 */
CwmPageTiming.trackPage = function(eventId, url) {
    CwmPageTiming.eventId = eventId;
    var timingSupported = CwmPageTiming.isTimingSupported();
    var storageSupported = CwmPageTiming.isLocalStorageSupported();

    if (!timingSupported && !storageSupported)
        return; // nothing we can do with very old browsers

    var loadTime = null;
    var endPageInfo = "";
    var itemsSent = [];

    if (timingSupported) {
        loadTime = performance.timing.responseEnd - performance.timing.navigationStart;
    }

    if (storageSupported) {
        // Set up so that end time will be recorded
        $(window).on('beforeunload', function () { CwmPageTiming.saveEndTime(eventId); });

        // Save current time - end time will be calculated as an offset to this.
        localStorage['pageStartTime.' + eventId] = Date.now();

        // Gather up any saved end times from previous pages.
        // endPageInfo variable will get zero or more  "id=duration;" items
        for (var i = 0; i < localStorage.length; i++) {
            var key = localStorage.key(i);
            var match = key.match('pageEndTime.(.*)');
            if (match) {
                var id = match[1];
                var endTime = localStorage[key];
                var startTime = localStorage['pageStartTime.' + id];
                if (startTime) {
                    endPageInfo += id + "=" + (endTime - startTime) + ";";
                    itemsSent.push(id);
                } else {
                    console.log("Weird - end time but no start time for event " + id);
                }
            }
        }
    }

    // Send this information
    var data = {
        id : eventId,
        loadTime: loadTime,
        endPageInfo: endPageInfo
    };
    CwmPageTiming.sendData(url, data, itemsSent);
};

// Called in page's onbeforeunload event:
// saves a timestamp to local storage as the page gets unloaded
CwmPageTiming.saveEndTime = function(eventId) {
    localStorage['pageEndTime.' + eventId] = Date.now();
};

// Send the collected data to the server AJAX endpoint
CwmPageTiming.sendData = function (url, data, itemsSent) {
    Wicket.Ajax.post({
        u: url,
        ep: data,
        rt: 10000,           // 10-second timeout
        sh: [function () {   // Success Handler
            console.log("Success sending liveness data");
            // Remove localstorage so that these items are not sent again later.
            for (var i in itemsSent) {
                localStorage.removeItem('pageStartTime.' + itemsSent[i]);
                localStorage.removeItem('pageEndTime.' + itemsSent[i]);
            }
        }],
        fh: [function () {   // Failure Handler
            console.log("Failure sending liveness data");
        }]
    });
};

// Test if browser supports LocalStorage
CwmPageTiming.isLocalStorageSupported = function() {
    try {
        return 'localStorage' in window && window['localStorage'] !== null;
    } catch (e) {
        return false;
    }
};

// Test if browser supports the timing API
CwmPageTiming.isTimingSupported = function() {
    try {
        return 'performance' in window && 'timing' in window.performance;
    } catch (e) {
        return false;
    }
};
