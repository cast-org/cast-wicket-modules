var castRecorderInstances = new Object();

// Create a new CAST Audio Recorder object, suitable as the backend of a new set of GUI controls.
// The ID must be the ID of some HTML element wrapping the controls, so that the buttons and indicators
// can be found and manipulated by the Javascript.
function createCastRecorder (id, configuration) {
	var instance = castRecorderBuilder();
	instance.setupRecorder(id, configuration);
	castRecorderInstances[id] = instance;
	console.log("Created CastRecorder " + id);
}

// Retrive a recorder object by the name that was given when it was set up.
function getCastRecorder (name) {
	return castRecorderInstances[name];
}

// Retrieve a recorder object for a DOM object, for example a button in the GUI.
// This works based on the assumption that some parent of the button will have an ID which matches a recorder's name.
function getEnclosingCastRecorder (domObject) {
	while (!domObject.id && domObject.parentNode)
		domObject = domObject.parentNode;
	console.log ("parent search led to: " + domObject.id);
	return getCastRecorder(domObject.id);
}

function elapsed_time_string (total_seconds) {
    function padTime(num) {
        return ( num < 10 ? "0" : "" ) + num;
    }
    var hours = Math.floor(total_seconds / 3600);
    total_seconds = total_seconds % 3600;

    var minutes = Math.floor(total_seconds / 60);
    total_seconds = total_seconds % 60;

    var seconds = Math.floor(total_seconds);
    return padTime(minutes) + ":" + padTime(seconds);
};

// Function that will return a recorder instance.
// You must then call the setupRecorder method on that instance with a configuration JSON object.
function castRecorderBuilder () {
    //Private variables
	var appletId;
    var recordUrl;
    var playPrefix;
    var userContentId;
    var binaryFileId;
    var playInterval;
    var recordInterval;
    var playPercentComplete;
    var isPaused = false;
    var isRecording = false;
    var isLoadingPlay = false;
    var recordingStart;
    
    var setAppletId = function (id) {
    	appletId = id;
    };
    var getAppletId = function() {
    	return appletId;
    };
    var setUserContentId = function (id) {
        userContentId = id;
    };
    var getUserContentId = function () {
        return userContentId;
    };
    var setBinaryFileId = function (id) {
        binaryFileId = id;
    };
    var getBinaryFileId = function () {
        return binaryFileId;
    };
    var setRecordUrl = function (url) {
        recordUrl = url;
    };
    var getRecordUrl = function () {
        return recordUrl;
    };
    var setPlayPrefix = function (prefix) {
        playPrefix = prefix;
    };
    var getPlayPrefix = function () {
        return playPrefix;
    };
    var getPlayUrl = function () {
        return playPrefix + binaryFileId;
    };

    // Public
    // todo: change from params to object for setup options
    return {
    	
    	// setupRecorder is called at page load time.
        setupRecorder: function (id, config) {
            Wami.setup({
                id:id,
                swfUrl: config.swfUrl
            });
            setAppletId(id);
            setRecordUrl(config.recordUrl);
            setPlayPrefix(config.playPrefix);
            setUserContentId(config.userContentId);
            setBinaryFileId(config.binaryFileId);

            if (config.binaryFileId == 0) {
                audioIndicator(appletId, '');
                audioStatus(appletId, 'default', 'Ready to Record');
                audioAction(appletId, 'recording_nt',  'stop');
            } else {
                audioIndicator(appletId, 'playback', 0);
                audioStatus(appletId, 'playback', '0', '100');
                audioAction(appletId, 'playing',  'stop');
            }
        },
        
        // Called by the GUI "record" button
        record: function () {
            this.status("RECORD button pressed");
            this.status("Recording with url: " + getRecordUrl());
            Wami.startRecording(getRecordUrl(), 
            		null, 
            		Wami.nameCallback(function(data) { getCastRecorder(appletId).recordingComplete(data); }), 
            		null);
            this.recordIntervalStart();
            audioAction(appletId, 'recording',  'record');
            audioIndicator(appletId, 'recording', 0);
            audioStatus(appletId, 'recording', 'Recording<br />0:00');
            isRecording = true;
        },
        recordIntervalStart: function () {
            recordingStart = new Date;
            recordInterval = setInterval(function() {
                var level = Wami.getRecordingLevel();
                var seconds = (new Date - recordingStart) / 1000
                audioStatus(appletId, 'recording', 'Recording<br />' + elapsed_time_string(seconds));
                audioVolumeResponsive(appletId, level);
            }, 200);
        },
        recordIntervalStop: function () {
            if ( recordInterval ) {
                clearInterval(recordInterval);
                recordInterval = null;
            }
        },
        
        // Called by the GUI "Stop" button
        stop: function () {
        	this.status("STOP button pressed");
            Wami.stopRecording();
            Wami.stopPlaying();
            this.recordIntervalStop();
            this.playIntervalStop();
            this.playPercentCompleteStop();
            if ( isRecording ) {
                audioIndicator(appletId, 'loading');
                audioStatus(appletId, 'default', 'Saving...');
                audioAction(appletId, 'loading',  '');
            } else {
                audioIndicator(appletId, 'playback', 0);
                audioStatus(appletId, 'playback', '0', '100');
                audioAction(appletId, 'playing',  'stop');
                audioSlider(appletId,0);
            }
        },
        
        // Called by the GUI "Play" button
        play: function (button) {
        	var applet = $(button).parent('.audio_applet').find('object').get();
        	this.status("PLAY button pressed: " + applet);
            if ( isPaused ) {
            	this.togglePause();
            } else {
            	this.status("Playing with url: " + getPlayUrl());
                Wami.startPlaying(getPlayUrl(), 
                		null, 
                		Wami.nameCallback(function() { getCastRecorder(appletId).playComplete(); }), 
                		null, 
                		Wami.nameCallback(function() { getCastRecorder(appletId).paused(); }), 
                		Wami.nameCallback(function() { getCastRecorder(appletId).resumed() }));
                this.playIntervalStart();
                this.playPercentCompleteStart();
                audioIndicator(appletId, 'loading');
                audioStatus(appletId, 'default', 'Loading...');
                audioAction(appletId, 'loading',  '');
                isLoadingPlay = true;
                isPaused = false;
            }
        },
        playIntervalStart: function () {
            playInterval = setInterval(function() {
                var level = Wami.getPlayingLevel();
            }, 200);
        },
        playPercentCompleteStart: function () {
            playPercentComplete = setInterval(function() {
                var complete = Wami.getPlayingPercentComplete();
                if ( complete > 0 && isLoadingPlay ) {
                    isLoadingPlay = false;
                    audioAction(appletId, 'playback',  'play');
                    audioIndicator(appletId, 'playback', 0);
                    audioStatus(appletId, 'playback', '0', '100');
                }
                if ( !isLoadingPlay ) {
                    audioSlider(appletId,complete);
                }
            }, 200);
        },
        playIntervalStop: function () {
            if ( playInterval ) {
                clearInterval(playInterval);
                playInterval = null;
            }
        },
        playPercentCompleteStop: function () {
            if ( playPercentComplete ) {
                clearInterval(playPercentComplete);
                playPercentComplete = null;
            }
        },
        
        // Called by the GUI "Pause/Resume" button
        togglePause: function () {
        	this.status("PAUSE button pressed");
            Wami.togglePause();
        },
        paused: function (data) {
        	this.playIntervalStop();
        	this.playPercentCompleteStop();
            isPaused = true;
            audioAction(appletId, 'playback',  'pause');
        },
        resumed: function (data) {
        	this.playIntervalStart();
        	this.playPercentCompleteStart();
            audioAction(appletId, 'playback',  'play');
            isPaused = false;
        },
        
        playComplete: function () {
        	this.playIntervalStop();
        	this.playPercentCompleteStop();
            audioIndicator(appletId, 'playback', 0);
            audioStatus(appletId, 'playback', '0', '100');
            audioAction(appletId, 'playing',  'stop');
            audioSlider(appletId,0);
        },
        recordingComplete: function (data) {
        	this.status("Recording Complete response: " + data[0]);
            audioIndicator(appletId, 'playback', 0);
            audioStatus(appletId, 'playback', '0', '100');
            audioAction(appletId, 'playing',  'stop');
            audioSlider(appletId,0);
            var response = $.parseJSON(data[0]);
            setUserContentId(response.userContentId);
            setBinaryFileId(response.binaryFileId);
            this.recordIntervalStop();
            isRecording = false;
            this.statusId();
        },
        recordingError: function () {
        	this.status("error recording");
        },
        status: function (msg) {
        	if (typeof console.log == 'function')
        		console.log('['+appletId+'] ' + msg);
        },
        statusId: function () {
        	this.status("UserContentId: " + getUserContentId() + "  BinaryFileId: " + getBinaryFileId());
        }

    };
};
