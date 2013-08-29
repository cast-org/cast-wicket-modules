var CastRecorder = (function () {
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
        setupRecorder: function (castRecorder) {
            Wami.setup({
                id: castRecorder.id,
                swfUrl: castRecorder.swfUrl
            });
            setAppletId(castRecorder.id);
            setRecordUrl(castRecorder.recordUrl);
            setPlayPrefix(castRecorder.playPrefix);
            setUserContentId(castRecorder.userContentId);
            setBinaryFileId(castRecorder.binaryFileId);

            if (castRecorder.binaryFileId == 0) {
                audioIndicator('aa_0', '');
                audioStatus('aa_0', 'default', 'Ready to Record');
                audioAction('aa_0', 'recording_nt',  'stop');
            } else {
                audioIndicator('aa_0', 'playback', 0);
                audioStatus('aa_0', 'playback', '0', '100');
                audioAction('aa_0', 'playing',  'stop');
            }
        },
        
        // Called by the GUI "record" button
        record: function () {
            CastRecorder.status("RECORD button pressed");
            CastRecorder.status("Recording with url: " + getRecordUrl());
            Wami.startRecording(getRecordUrl(), null, Wami.nameCallback(CastRecorder.recordingComplete), null);
            CastRecorder.recordIntervalStart();
            audioAction('aa_0', 'recording',  'record');
            audioIndicator('aa_0', 'recording', 0);
            audioStatus('aa_0', 'recording', 'Recording<br />0:00');
            isRecording = true;
        },
        recordIntervalStart: function () {
            recordingStart = new Date;
            recordInterval = setInterval(function() {
                var level = Wami.getRecordingLevel();
                var seconds = (new Date - recordingStart) / 1000
                audioStatus('aa_0', 'recording', 'Recording<br />' + CastRecorder.elapsed_time_string(seconds));
                audioVolumeResponsive('aa_0', level);
            }, 200);
        },
        elapsed_time_string: function (total_seconds) {
            function padTime(num) {
                return ( num < 10 ? "0" : "" ) + num;
            }
            var hours = Math.floor(total_seconds / 3600);
            total_seconds = total_seconds % 3600;

            var minutes = Math.floor(total_seconds / 60);
            total_seconds = total_seconds % 60;

            var seconds = Math.floor(total_seconds);
            return padTime(minutes) + ":" + padTime(seconds);
        },
        recordIntervalStop: function () {
            if ( recordInterval ) {
                clearInterval(recordInterval);
                recordInterval = null;
            }
        },
        
        // Called by the GUI "Stop" button
        stop: function () {
            CastRecorder.status("STOP button pressed");
            Wami.stopRecording();
            Wami.stopPlaying();
            CastRecorder.recordIntervalStop();
            CastRecorder.playIntervalStop();
            CastRecorder.playPercentCompleteStop();
            if ( isRecording ) {
                audioIndicator('aa_0', 'loading');
                audioStatus('aa_0', 'default', 'Saving...');
                audioAction('aa_0', 'loading',  '');
            } else {
                audioIndicator('aa_0', 'playback', 0);
                audioStatus('aa_0', 'playback', '0', '100');
                audioAction('aa_0', 'playing',  'stop');
                audioSlider('aa_0',0);
            }
        },
        
        // Called by the GUI "Play" button
        play: function (button) {
        	var applet = $(button).parent('.audio_applet').find('object').get();
            CastRecorder.status("PLAY button pressed: " + applet);
            if ( isPaused ) {
                CastRecorder.togglePause();
            } else {
                CastRecorder.status("Playing with url: " + getPlayUrl());
                Wami.startPlaying(getPlayUrl(), null, Wami.nameCallback(CastRecorder.playComplete), null, Wami.nameCallback(CastRecorder.paused), Wami.nameCallback(CastRecorder.resumed));
                CastRecorder.playIntervalStart();
                CastRecorder.playPercentCompleteStart();
                audioIndicator('aa_0', 'loading');
                audioStatus('aa_0', 'default', 'Loading...');
                audioAction('aa_0', 'loading',  '');
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
                    audioAction('aa_0', 'playback',  'play');
                    audioIndicator('aa_0', 'playback', 0);
                    audioStatus('aa_0', 'playback', '0', '100');
                }
                if ( !isLoadingPlay ) {
                    audioSlider('aa_0',complete);
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
            CastRecorder.status("PAUSE button pressed");
            Wami.togglePause();
        },
        paused: function (data) {
            CastRecorder.playIntervalStop();
            CastRecorder.playPercentCompleteStop();
            isPaused = true;
            audioAction('aa_0', 'playback',  'pause');
        },
        resumed: function (data) {
            CastRecorder.playIntervalStart();
            CastRecorder.playPercentCompleteStart();
            audioAction('aa_0', 'playback',  'play');
            isPaused = false;
        },
        
        playComplete: function () {
            CastRecorder.playIntervalStop();
            CastRecorder.playPercentCompleteStop();
            audioIndicator('aa_0', 'playback', 0);
            audioStatus('aa_0', 'playback', '0', '100');
            audioAction('aa_0', 'playing',  'stop');
            audioSlider('aa_0',0);
        },
        recordingComplete: function (data) {
            CastRecorder.status("Recording Complete response: " + data[0]);
            audioIndicator('aa_0', 'playback', 0);
            audioStatus('aa_0', 'playback', '0', '100');
            audioAction('aa_0', 'playing',  'stop');
            audioSlider('aa_0',0);
            var response = $.parseJSON(data[0]);
            setUserContentId(response.userContentId);
            setBinaryFileId(response.binaryFileId);
            CastRecorder.recordIntervalStop();
            isRecording = false;
            CastRecorder.statusId();
        },
        recordingError: function () {
            CastRecorder.status("error recording");
        },
        status: function (msg) {
        	if (typeof console.log == 'function')
        		console.log(msg);
        },
        statusId: function () {
            CastRecorder.status("UserContentId: " + getUserContentId() + "  BinaryFileId: " + getBinaryFileId());
        }

    };
})();
