var printDebug = function(messageBuilder) {};

(function() {

    const LOG_TO_CONSOLE_ENABLE = false;
    const TAG = "PREBID_MRAID_JS";

    var mraid = window.mraid = {};
    mraid.eventListeners = {};
    mraid.state = "loading";
    mraid.viewable = false;
    mraid.resizePropertiesInitialized = false;
    mraid.volumePercentage = null;

    var nativeCallQueue = [];
    var nativeCallInFlight = false;

    mraid.currentPosition = {
        x: 0,
        y: 0,
        width: 0,
        height: 0
    };

    mraid.maxSize = {
        width: 0,
        height: 0
    };

    mraid.defaultPosition = {
        x: 0,
        y: 0,
        width: 0,
        height: 0
    };

    mraid.screenSize = {
        width: 0,
        height: 0
    };

    mraid.allSupports = {
        sms: false,
        tel: false,
        calendar: false,
        storePicture: false,
        inlineVideo: false,
        vpaid: false
    };

    if (LOG_TO_CONSOLE_ENABLE) {
        printDebug = function(messageBuilder) {
            var message = messageBuilder();
            console.log(TAG + ': ' + message);
        }
    }

    mraid.setCurrentPosition = function(x, y, width, height) {

        printDebug(() => 'setCurrentPosition');

        mraid.currentPosition.x = x;
        mraid.currentPosition.y = y;
        mraid.currentPosition.width = width;
        mraid.currentPosition.height = height;

    };

    mraid.setMaxSize = function(width, height) {

        printDebug(() => 'setMaxSize');

        mraid.maxSize.width = width;
        mraid.maxSize.height = height;

    };

    mraid.setDefaultPosition = function(x, y, width, height) {

        printDebug(() => 'setDefaultPosition');

        mraid.defaultPosition.x = x;
        mraid.defaultPosition.y = y;
        mraid.defaultPosition.width = width;
        mraid.defaultPosition.height = height;

    };

    mraid.setScreenSize = function(width, height) {

        printDebug(() => 'setScreenSize');

        mraid.screenSize.width = width;
        mraid.screenSize.height = height;

    };

    mraid.orientationProperties = {
        allowOrientationChange: true,
        forceOrientation: "none"
    };

    mraid.expandProperties = {
        width: 0,
        height: 0,
        useCustomClose: false
    };

    mraid.resizeProperties = {
        width: 0,
        height: 0,
        customClosePosition: "top-right",
        offsetX: 0,
        offsetY: 0,
        allowOffscreen: true
    };

     mraid.lastExposure = {
         exposedPercentage: 100.0,
         visibleRectangle: {
            x: 0.0,
            y: 0.0,
            width: 0.0,
            height: 0.0
         },
         occlusionRectangles: null
     };

    var safeString = function(str) {

        printDebug(() => 'safeString' + str);
        return str ? str : '';
    };


    var callContainer = function(command) {
        printDebug(() => 'callContainer ' + command);
        var args = Array.prototype.slice.call(arguments);
        args.shift();

        if (nativeCallInFlight) {
            var nextCommand = {
              "command" : command,
              "args" : args
            };
            printDebug(() => 'callContainer nextCommand in queue ' + nextCommand);
            nativeCallQueue.push(nextCommand);
        } else {
            nativeCallInFlight = true;
            printDebug(() => 'callContainer executing immediately.');
            jsBridge[command].apply(jsBridge, args);
        }
    };

    mraid.open = function(url) {
        printDebug(() => 'open(' + url + ')');

        callContainer('open', url);
    };


    mraid.resize = function() {
        printDebug(() => 'resize');
        if(mraid.resizePropertiesInitialized) {

            callContainer('resize');

        } else {

            mraid.onError("[width] and [height] parameters must be at least 1px. Set them using setResizeProperties() MRAID method.", "resize");
        }
    };

    mraid.expand = function(url) {
        printDebug(() => 'mraid.expand(' + url + ')');

        if(url) {
            callContainer('expand', url);
        } else {
            callContainer('expand');
        }
    };


    mraid.close = function() {
        printDebug(() => 'close');
        callContainer('close');
    };


    mraid.getPlacementType = function() {
        printDebug(() => 'getPlacementType');
        return jsBridge.getPlacementType();
    };


    mraid.getState = function() {
        printDebug(() => 'getState ' + mraid.state);
        return mraid.state;
    };


    mraid.getVersion = function() {
        printDebug(() => 'getVersion');
        return '3.0';
    };


    mraid.isViewable = function() {

        printDebug(() => 'isViewable ' + mraid.viewable);
        return mraid.viewable;
    };


    mraid.getScreenSize = function() {
        printDebug(() => 'getScreenSize');
        return JSON.parse(jsBridge.getScreenSize());
    };


    mraid.getDefaultPosition = function() {
        printDebug(() => 'getDefaultPosition');
        return JSON.parse(jsBridge.getDefaultPosition());
    };


    mraid.getCurrentPosition = function() {
        printDebug(() => 'getCurrentPosition');
        return JSON.parse(jsBridge.getCurrentPosition());
    };


    mraid.getMaxSize = function() {
        printDebug(() => 'getMaxSize');
        return JSON.parse(jsBridge.getMaxSize());
    };


    mraid.supports = function(feature) {
        printDebug(() => 'supports: ' + feature);
        printDebug(() => 'supports:regular call: ' + mraid.allSupports[feature]);
        return mraid.allSupports[feature];
    };


    mraid.playVideo = function(url) {
        printDebug(() => 'playVideo');
        callContainer('playVideo', url);
    };


    mraid.addEventListener = function(event, listener) {
        printDebug(() => 'addEventListener: ' + event);
        var handlers = mraid.eventListeners[event];
        if(handlers == null) {
            handlers = mraid.eventListeners[event] = [];
        }


        for(var handler = 0; handler < handlers.length; handler++) {
            if(listener == handlers[handler]) {
                return;
            }
        }


        handlers.push(listener);
    };


    mraid.removeEventListener = function(event, listener) {
        printDebug(() => 'removeEventListener: ' + event);
        var handlers = mraid.eventListeners[event];
        if(handlers != null) {
            for(var handler = 0; handler < handlers.length; handler++) {
                if(handlers[handler] == listener) {
                    handlers.splice(handler, 1);
                    break;
                }
            }
        }
    };


    mraid.setOrientationProperties = function(properties) {
        printDebug(() => 'setOrientationProperties');
        if(!properties)
            return;
        var aoc = properties.allowOrientationChange;
        if(aoc === true || aoc === false) {
            mraid.orientationProperties.allowOrientationChange = aoc;
        }

        var fo = properties.forceOrientation;
        if(fo == 'landscape' || fo == 'portrait' || fo == 'none') {
            mraid.orientationProperties.forceOrientation = fo;
        }

        callContainer('onOrientationPropertiesChanged', JSON.stringify(mraid.getOrientationProperties()));
    };


    mraid.getOrientationProperties = function() {
        printDebug(() => 'getOrientationProperties');
        return mraid.orientationProperties;
    };


    mraid.getResizeProperties = function() {
        printDebug(() => 'getResizeProperties');
        return mraid.resizeProperties;
    };


    mraid.getExpandProperties = function() {
        printDebug(() => 'getExpandProperties');
        mraid.expandProperties.isModal = true;
        return mraid.expandProperties;
    };


    mraid.setResizeProperties = function(properties) {
            printDebug(() => 'setResizeProperties ');
   		    mraid.resizePropertiesInitialized = false;

    		if (!properties) {
    			mraid.onError("properties is null", "setResizeProperties");
    			return;
    		}

    		//Allow Offscreen
    		if (typeof(properties.allowOffscreen) !== "boolean") {
    			mraid.onError("allowOffscreen param of [" + properties.allowOffscreen + "] is unusable.", "setResizeProperties");
    			return;
    		}

    		var allowOffscreen = properties.allowOffscreen

    		//Get max size
    		var maxSize = mraid.getMaxSize();
    		if (!maxSize || !maxSize.width || !maxSize.height) {
    			mraid.onError("Unable to use maxSize of [" + JSON.stringify(maxSize) + "]", "setResizeProperties");
    			return;
    		}

    		//Width
    		if (properties.width == null || typeof properties.width === 'undefined' || isNaN(properties.width)) {
    			mraid.onError("width param of [" + properties.width + "] is unusable.", "setResizeProperties");
    			return;
    		}

    		if (properties.width < 50 || (properties.width > maxSize.width && !allowOffscreen)) {
    			mraid.onError("width param of [" + properties.width + "] outside of acceptable range of 50 to " + maxSize.width, "setResizeProperties");
    			return;
    		}

    		//Height
    		if (properties.height == null || typeof properties.height === 'undefined' || isNaN(properties.height)) {
    			mraid.onError("height param of [" + properties.height + "] is unusable.", "setResizeProperties");
    			return;
    		}

    		if (properties.height < 50 || (properties.height > maxSize.height && !allowOffscreen)) {
    			mraid.onError("height param of [" + properties.height + "] outside of acceptable range of 50 to " + maxSize.height, "setResizeProperties");
    			return;
    		}

    		//Offset
    		if (properties.offsetX == null || typeof properties.offsetX === 'undefined' || isNaN(properties.offsetX)) {
    			mraid.onError("offsetX param of [" + properties.offsetX + "] is unusable.", "setResizeProperties");
    			return;
    		}

    		if (properties.offsetY == null || typeof properties.offsetY === 'undefined' || isNaN(properties.offsetY)) {
    			mraid.onError("offsetY param of [" + properties.offsetY + "] is unusable.", "setResizeProperties");
    			return;
    		}


    		mraid.resizeProperties.width = properties.width;
    		mraid.resizeProperties.height = properties.height;
    		mraid.resizeProperties.customClosePosition = properties.customClosePosition;
    		mraid.resizeProperties.offsetX = properties.offsetX;
    		mraid.resizeProperties.offsetY = properties.offsetY;
    		mraid.resizeProperties.allowOffscreen = properties.allowOffscreen;

    		mraid.resizePropertiesInitialized = true;
    	};


    mraid.setExpandProperties = function(properties) {

        printDebug(() => 'setExpandProperties');

        if(properties && properties.width != null && typeof properties.width !== 'undefined' && !isNaN(properties.width)) {
            mraid.expandProperties.width = properties.width;
        }

        if(properties && properties.height != null && typeof properties.height !== 'undefined' && !isNaN(properties.height)) {
            mraid.expandProperties.height = properties.height;
        }
    };

    mraid.useCustomClose = function(useCustomClose) {
        printDebug(() => 'DEPRECATED useCustomClose' + useCustomClose);
        if(useCustomClose == true || useCustomClose == false) {
            mraid.expandProperties.useCustomClose = useCustomClose;
            //also call container's
            callContainer('shouldUseCustomClose', useCustomClose);
        }
    };


    mraid.fireEvent = function(event, args) {
        printDebug(() => 'fireEvent ' + event);
        var handlers = mraid.eventListeners[event];
        if(handlers == null) {
            return;
        }


        for(var handler = 0; handler < handlers.length; handler++) {
            if(event == 'ready') {
                handlers[handler]();
            } else if(event == 'error') {
                handlers[handler](args[0], args[1]);
            } else if(event == 'stateChange') {
                handlers[handler](args);
            } else if(event == 'viewableChange') {
                handlers[handler](args);
            } else if(event == 'sizeChange') {
                handlers[handler](args[0], args[1]);
            } else if(event == 'audioVolumeChange') {
                handlers[handler](args);
            } else if(event == 'exposureChange') {
                handlers[handler](mraid.lastExposure.exposedPercentage,
                 mraid.lastExposure.visibleRectangle,
                 mraid.lastExposure.occlusionRectangles);
            }
        }
    };


    mraid.createCalendarEvent = function(parameters) {
        printDebug(() => 'createCalendarEvent');
        callContainer('createCalendarEvent', JSON.stringify(parameters));
    };


    mraid.storePicture = function(url) {
        printDebug(() => 'storePicture');
        callContainer('storePicture', url);
    };

    mraid.onError = function(message, action) {
        printDebug(() => 'onError' + message);
        mraid.fireEvent("error", [message, action]);
    };


    mraid.onReady = function() {
        printDebug(() => 'ready');

        mraid.fireEvent("ready");
    };

    mraid.onReadyExpanded = function() {
        printDebug(() => 'onReadyExpanded');
        mraid.fireEvent("ready");
    };

    mraid.onSizeChange = function(width, height) {
        printDebug(() => 'onSizeChange: width ' + width + 'height: ' + height);
        mraid.fireEvent("sizeChange", [width, height]);
    };


    mraid.onStateChange = function(state) {
        printDebug(() => 'onStateChange' + state);
        mraid.state = state;
        mraid.fireEvent("stateChange", mraid.getState());
    };


    /**
    * @deprecated Since MRAID 3. Use onExposureChange instead.
    */
    mraid.onViewableChange = function(isViewable) {
        printDebug(() => 'onViewableChange');

        mraid.viewable = isViewable;
        mraid.fireEvent("viewableChange", mraid.isViewable());
    };

    mraid.getLocation = function() {
        printDebug(() => 'getLocation');
        return JSON.parse(jsBridge.getLocation());
    };

    mraid.getCurrentAppOrientation = function() {
        printDebug(() => 'getCurrentAppOrientation');
        return JSON.parse(jsBridge.getCurrentAppOrientation());
    };

    mraid.unload = function() {
        printDebug(() => 'unload');
        callContainer('unload');
    };

    mraid.onExposureChange = function(viewExposureString) {
        printDebug(() => 'onExposureChange ' + viewExposureString);
        var viewExposure = JSON.parse(viewExposureString);
        mraid.lastExposure = viewExposure;
        mraid.fireEvent("exposureChange");
    };

    /*
        This event fires on changing of the audio volume.
        See https://www.iab.com/wp-content/uploads/2017/07/MRAID_3.0_FINAL.pdf , par. 7.6

        @volumePercentage -  percentage of maximum audio playback
                             volume, a floating-point number between 0.0 and 100.0, or 0.0
                             if playback is not allowed, or null if volume can’t be
                             determined
    */
    mraid.onAudioVolumeChange = function(newVolumePercentage) {
            printDebug(() => 'onAudioVolumeChange(' + newVolumePercentage + ')');
            mraid.volumePercentage = newVolumePercentage;
            mraid.fireEvent("audioVolumeChange", newVolumePercentage);
    };

    mraid.nativeCallComplete = function() {
      if (nativeCallQueue.length === 0) {
        nativeCallInFlight = false;
        return;
     }

      var nextCall = nativeCallQueue.shift();
      printDebug(() => 'nativeCallComplete nextCall: ' + nextCall.command + " arg " + nextCall.args);

      jsBridge[nextCall.command].apply(jsBridge, nextCall.args);
    };

}());