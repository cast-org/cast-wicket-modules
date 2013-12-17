var castRecorderInstances = new Object();

var castRecorderLatest = null; // name of the one that is currently active

var castRecorderSwfHolderId = "castRecorderSwfHolder"; // ID of element that will hold the SWF.

// Create a new CAST Audio Recorder object, suitable as the backend of a new set of GUI controls.
// The ID must be the ID of some HTML element wrapping the controls, so that the buttons and indicators
// can be found and manipulated by the Javascript.
function createCastRecorder (id, configuration) {
	var instance = castRecorderBuilder();
	castRecorderInstances[id] = instance;
	castRecorderLatest = id;
	instance.setupRecorder(id, configuration);
}

// Retrive a recorder object by the name that was given when it was set up.
function getCastRecorder (name) {
	if(!castRecorderInstances[name])
		console.log("getCastRecorder lookup failed, no such name " + name);
	return castRecorderInstances[name];
}

// Retrieve a recorder object for a DOM object, for example a button in the GUI.
// This works based on the assumption that some parent of the button will have an ID which matches a recorder's name.
function getEnclosingCastRecorder (domObject) {
	while (!domObject.id && domObject.parentNode)
		domObject = domObject.parentNode;
	//console.log ("parent search led to: " + domObject.id);
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

// Moves and sizes the SWF to match the castRecorderLatest GUI
function beforeSecurityCallback (swfId) {
	var guiId = castRecorderLatest;
	var gui = $("#"+guiId).find(".audio_applet").get(0);
	var guiO = $(gui).offset();

	var swf = $("#"+swfId); 
	swf.css({
	    "position": "absolute",
	    "top": guiO.top + "px",
	    "left": guiO.left + "px"
	});

	var app = swf.find("object").get(0);
	$(app).attr("width", "225px");
	$(app).attr("height", "145px");
	// console.log ("Before security.  SWF ID is " + swfId + "; GUI ID is " + guiId);
}

function afterSecurityCallback (swfId, perm) {
	// alert ("After security.  SWF ID is " + swfId + "; GUI ID is " + guiId + "; permission=" + perm);
}

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
    var state = "LOADAPP";  // should always be one of LOADAPP READY LOADPLAY PLAY PLAYPAUSE LOADREC REC SAVE ERROR
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
            setAppletId(id);
            setRecordUrl(config.recordUrl);
            setPlayPrefix(config.playPrefix);
            setUserContentId(config.userContentId);
            setBinaryFileId(config.binaryFileId);
            this.createHolderIfNeeded();
            Wami.setup({
                id:castRecorderSwfHolderId,
                swfUrl: config.swfUrl,
                onReady: function() { getCastRecorder(getAppletId()).ready(); },
                onBeforeSecurityShown: function() { beforeSecurityCallback(castRecorderSwfHolderId); },
                onAfterSecurityShown: function() { afterSecurityCallback(castRecorderSwfHolderId, Wami.getSettings().microphone.granted); }
            });
        },
        
        createHolderIfNeeded: function() {
        	if($('#'+castRecorderSwfHolderId).length==0) {
        		this.status("Creating SWF Holder");
        		$('body').append('<div id="' + castRecorderSwfHolderId + '"></div>');
        	}
        },
        
        ready: function () {
        	state = "READY";
        	this.status("Recorder is READY");
            if (getBinaryFileId == 0) {
                audioIndicator(appletId, '');
                audioStatus(appletId, 'default', 'Ready to Record');
                audioAction(appletId, 'recording_nt',  'stop');
            } else {
                audioIndicator(appletId, 'playback');
                audioStatus(appletId, 'playback', 'Ready');
                audioTimestamp(appletId, '0', '100');
                audioAction(appletId, 'playing',  'stop');
            }        	
        },
        
        // Called by the GUI "settings" button
        settings: function() {
        	// TODO: set "SETTINGS" state?  Can we reset it when settings is closed?
        	this.status("SETTINGS button pressed");
        	castRecorderLatest = appletId;
        	Wami.showSecurity('privacy');
        },
        
        // Called by the GUI "record" button
        record: function () {
            this.status("RECORD button pressed");
            castRecorderLatest = appletId;
            if (Wami.getSettings().microphone.granted) {
                this.status("Recording to url: " + getRecordUrl());
            	Wami.startRecording(getRecordUrl(), 
            		Wami.nameCallback(function() { getCastRecorder(appletId).recordingStart(); }),
            		Wami.nameCallback(function(data) { getCastRecorder(appletId).recordingComplete(data); }),
            		null);
            } else {
            	this.status("No mic permission, opening settings");
            	Wami.showSecurity('privacy');
            }
        },
        recordingStart: function() {
            state = "REC";            			
        	this.status("recordingStart callback called");
            this.recordIntervalStart();
            audioAction(appletId, 'recording',  'record');
            audioIndicator(appletId, 'recording');
            audioStatus(appletId, 'recording', 'Recording: 0:00');
        },
        recordIntervalStart: function () {
            recordingStart = new Date;
            recordInterval = setInterval(function() {
                var level = Wami.getRecordingLevel();
                var seconds = (new Date - recordingStart) / 1000
                audioStatus(appletId, 'recording', 'Recording: ' + elapsed_time_string(seconds));
                audioVolume(appletId, level);
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
            if ( state == "REC" ) {
                audioIndicator(appletId, 'loading');
                audioStatus(appletId, 'default', 'Saving...');
                audioAction(appletId, 'loading',  '');
                state = "SAVE";
            } else {
                audioIndicator(appletId, 'playback');
                audioStatus(appletId, 'playback', 'Ready');
                audioTimestamp(appletId, '0', '100');
                audioAction(appletId, 'playing',  'stop');
                audioSlider(appletId, 0);
                state = "READY";
            }
        },
        
        // Called by the GUI "Play" button
        play: function (button) {
        	var applet = $(button).parent('.audio_applet').find('object').get();
        	this.status("PLAY button pressed: " + applet);
            if ( state=="PLAYPAUSE" ) {
            	this.togglePause();
            } else if (state == "READY" ){
            	this.status("Playing with url: " + getPlayUrl());
                Wami.startPlaying(getPlayUrl(), 
                		null,  // start playing callback
                		Wami.nameCallback(function() { getCastRecorder(appletId).playComplete(); }), 
                		null,  // play error callback
                		Wami.nameCallback(function() { getCastRecorder(appletId).paused(); }), 
                		Wami.nameCallback(function() { getCastRecorder(appletId).resumed() }));
                this.playIntervalStart();
                this.playPercentCompleteStart();
                audioIndicator(appletId, 'loading');
                audioStatus(appletId, 'default', 'Loading...');
                audioAction(appletId, 'loading',  '');
                state = "LOADPLAY";
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
                if ( complete > 0 && state == "LOADPLAY" ) {
                    state = "PLAY";
                    audioAction(appletId, 'playback',  'play');
                    audioIndicator(appletId, 'playback');
                    audioStatus(appletId, 'playback', 'Playing');
                    audioTimestamp(appletId, '0', '100');
                }
                if ( state == 'PLAY' ) {
                    audioSlider(appletId,complete);
                    audioTimestamp(appletId, complete, '100');
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
        	if (state == "PLAY" || state == "PLAYPAUSE") {
        		Wami.togglePause();
        	}
        },
        paused: function (data) {
        	this.playIntervalStop();
        	this.playPercentCompleteStop();
            state = "PLAYPAUSE";
            audioAction(appletId, 'playback',  'pause');
        },
        resumed: function (data) {
        	this.playIntervalStart();
        	this.playPercentCompleteStart();
            audioAction(appletId, 'playback',  'play');
            state = "PLAY";
        },
        
        playComplete: function () {
        	this.playIntervalStop();
        	this.playPercentCompleteStop();
            audioIndicator(appletId, 'playback');
            audioStatus(appletId, 'playback', 'Ready');
            audioTimestamp(appletId, '0', '100');
            audioAction(appletId, 'playing',  'stop');
            audioSlider(appletId,0);
            state = "READY";
        },
        recordingComplete: function (data) {
        	this.status("Recording Complete response: " + data[0]);
            audioIndicator(appletId, 'playback');
            audioStatus(appletId, 'playback', 'Ready');
            audioAction(appletId, 'playing',  'stop');
            audioSlider(appletId, 0);
            var response = $.parseJSON(data[0]);
            setUserContentId(response.userContentId);
            setBinaryFileId(response.binaryFileId);
            this.recordIntervalStop();
            state = "READY";
            this.statusId();
        },
        recordingError: function () {
        	this.status("error recording");
        	state = "ERROR";
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
