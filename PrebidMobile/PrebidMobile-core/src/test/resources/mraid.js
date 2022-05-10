(function() {
    var mraid = window.mraid = {};
    mraid.eventListeners = {};
    mraid.state = "loading";
    mraid.viewable = false;
    mraid.resizePropertiesInitialized = false;
    var logConsole = 1;

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
        inlineVideo: false
    };

    mraid.setCurrentPosition = function(x, y, width, height) {

        console.log('mraid: setCurrentPosition');

        mraid.currentPosition.x = x;
        mraid.currentPosition.y = y;
        mraid.currentPosition.width = width;
        mraid.currentPosition.height = height;

    };

    mraid.setMaxSize = function(width, height) {

        console.log('mraid: setMaxSize');

        mraid.maxSize.width = width;
        mraid.maxSize.height = height;

    };

    mraid.setDefaultPosition = function(x, y, width, height) {

        console.log('mraid: setDefaultPosition');

        mraid.defaultPosition.x = x;
        mraid.defaultPosition.y = y;
        mraid.defaultPosition.width = width;
        mraid.defaultPosition.height = height;

    };

    mraid.setScreenSize = function(width, height) {

        console.log('mraid: @@@@@@@@@ : setDefaultPosition');

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

    var safeString = function(str) {

        console.log(':mraid: @@@@@@@@@ : safeString' + str);
        return str ? str : '';
    };


    var callContainer = function(command) {
        console.log(':mraid: @@@@@@@@@ : callContainer' + command);
        var args = Array.prototype.slice.call(arguments);
        args.shift();

        jsBridge[command].apply(jsBridge, args);

    };

    var log = function(str) {
        if(logConsole) {
            console.log(str);
        }
    };


    mraid.open = function(url) {
        log(': mraid.open(' + url + ')');

        callContainer('open', url);
    };


    mraid.resize = function() {
        console.log(':mraid: @@@@@@@@@ : resize');
        if(mraid.resizePropertiesInitialized) {

            callContainer('resize');

        } else {

            mraid.onError("[width] and [height] parameters must be at least 1px. Set them using setResizeProperties() MRAID method.", "resize");
        }
    };

    mraid.expand = function(url) {
        log(': mraid.expand(' + url + ')');

        if(url) {
            callContainer('expand', url);
        } else {
            callContainer('expand');
        }
    };


    mraid.close = function() {
        console.log(':mraid: @@@@@@@@@ : close');
        callContainer('close');
    };


    mraid.getPlacementType = function() {
        console.log(':mraid: @@@@@@@@@ : getPlacementType');
        return jsBridge.getPlacementType();
    };


    mraid.getState = function() {
        console.log(':mraid: @@@@@@@@@ : getState ' + mraid.state);
        return mraid.state;
    };


    mraid.getVersion = function() {
        console.log(':mraid: @@@@@@@@@ : getVersion');
        return '2.0';
    };


    mraid.isViewable = function() {

        console.log(':mraid: @@@@@@@@@ : isViewable' + mraid.viewable);
        return mraid.viewable;
    };


    mraid.getScreenSize = function() {
        console.log(':mraid: @@@@@@@@@ : getScreenSize');
        return JSON.parse(jsBridge.getScreenSize());
    };


    mraid.getDefaultPosition = function() {
        console.log('mraid: @@@@@@@@@ : getDefaultPosition');
        return JSON.parse(jsBridge.getDefaultPosition());
    };


    mraid.getCurrentPosition = function() {
        console.log('mraid: @@@@@@@@@ : getCurrentPosition');
        return JSON.parse(jsBridge.getCurrentPosition());
    };


    mraid.getMaxSize = function() {
        console.log('mraid: @@@@@@@@@ : getMaxSize');
        return JSON.parse(jsBridge.getMaxSize());
    };


    mraid.supports = function(feature) {
        console.log(':mraid: @@@@@@@@@ : supports: ' + feature);
        console.log(':mraid: @@@@@@@@@ : supports:regular call: ' + mraid.allSupports[feature]);
        return mraid.allSupports[feature];
    };


    mraid.playVideo = function(url) {
        console.log(':mraid: @@@@@@@@@ : playVideo');
        callContainer('playVideo', url);
    };


    mraid.addEventListener = function(event, listener) {
        console.log(':mraid: @@@@@@@@@ : addEventListener: ' + event);
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
        console.log(':mraid: @@@@@@@@@ : removeEventListener: ' + event);
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
        console.log(':mraid: @@@@@@@@@ : setOrientationProperties');
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
        console.log(':mraid: @@@@@@@@@ : getOrientationProperties');
        return mraid.orientationProperties;
    };


    mraid.getResizeProperties = function() {
        console.log(':mraid: @@@@@@@@@ : getResizeProperties');
        return mraid.resizeProperties;
    };


    mraid.getExpandProperties = function() {
        console.log(':mraid: @@@@@@@@@ : getExpandProperties');
        mraid.expandProperties.isModal = true;
        return mraid.expandProperties;
    };


    mraid.setResizeProperties = function(properties) {
        console.log(':mraid: @@@@@@@@@ : setResizeProperties');
        if(properties && properties.width != null && typeof properties.width !== 'undefined' && !isNaN(properties.width)) {
            mraid.resizeProperties.width = properties.width;
        }

        if(properties && properties.height != null && typeof properties.height !== 'undefined' && !isNaN(properties.height)) {
            mraid.resizeProperties.height = properties.height;
        }

        if(properties && properties.customClosePosition != null && typeof properties.customClosePosition !== 'undefined' && isNaN(properties.customClosePosition)) {
            mraid.resizeProperties.customClosePosition = properties.customClosePosition;
        }

        if(properties && properties.offsetX != null && typeof properties.offsetX !== 'undefined' && !isNaN(properties.offsetX)) {
            mraid.resizeProperties.offsetX = properties.offsetX;
        }

        if(properties && properties.offsetY != null && typeof properties.offsetY !== 'undefined' && !isNaN(properties.offsetY)) {
            mraid.resizeProperties.offsetY = properties.offsetY;
        }

        if(properties && properties.allowOffscreen == true || properties.allowOffscreen == false) {
            mraid.resizeProperties.allowOffscreen = properties.allowOffscreen;
        }

        mraid.resizePropertiesInitialized = mraid.resizeProperties.width > 0 && mraid.resizeProperties.height > 0;
    };


    mraid.setExpandProperties = function(properties) {

        console.log('mraid: @@@@@@@@@ : setExpandProperties');

        if(properties && properties.width != null && typeof properties.width !== 'undefined' && !isNaN(properties.width)) {
            mraid.expandProperties.width = properties.width;
        }

        if(properties && properties.height != null && typeof properties.height !== 'undefined' && !isNaN(properties.height)) {
            mraid.expandProperties.height = properties.height;
        }
    };

    mraid.useCustomClose = function(useCustomClose) {
        console.log(':mraid: @@@@@@@@@ : useCustomClose' + useCustomClose);
        if(useCustomClose == true || useCustomClose == false) {
            mraid.expandProperties.useCustomClose = useCustomClose;
            //also call container's
            callContainer('shouldUseCustomClose', useCustomClose);

        }
    };


    mraid.fireEvent = function(event, args) {
        console.log(':mraid: @@@@@@@@@ : fireEvent' + event);
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
            }


        }
    };


    mraid.createCalendarEvent = function(parameters) {
        console.log('mraid: @@@@@@@@@ : createCalendarEvent');
        callContainer('createCalendarEvent', JSON.stringify(parameters));
    };


    mraid.storePicture = function(url) {
        console.log('mraid: @@@@@@@@@ : storePicture');
        callContainer('storePicture', url);
    };

    mraid.onError = function(message, action) {
        console.log(':mraid: @@@@@@@@@ : onError' + message);
        mraid.fireEvent("error", [message, action]);
    };


    mraid.onReady = function() {
        console.log(':mraid: @@@@@@@@@ : ready');

        mraid.onStateChange("default");
        mraid.fireEvent("ready");
    };

    mraid.onReadyExpanded = function() {
        console.log(':mraid: @@@@@@@@@ : onReadyExpanded');
        mraid.onStateChange("expanded");
        mraid.fireEvent("ready");
    };

    mraid.onSizeChange = function(width, height) {
        console.log(':mraid: @@@@@@@@@ : onSizeChange: width' + width + 'height: ' + height);
        mraid.fireEvent("sizeChange", [width, height]);
    };


    mraid.onStateChange = function(state) {
        console.log('mraid: @@@@@@@@@ : onStateChange' + state);
        mraid.state = state;
        mraid.fireEvent("stateChange", mraid.getState());
    };


    mraid.onViewableChange = function(isViewable) {
        console.log('mraid: @@@@@@@@@ : onViewableChange');

        mraid.viewable = isViewable;
        mraid.fireEvent("viewableChange", mraid.isViewable());
    };
}());