var exec = require('cordova/exec');

var IonicCordovaLibraryMultiSelection = {};

// media type
IonicCordovaLibraryMultiSelection.MEDIA_TYPE = {
    PICTURE: 0,
    VIDEO: 1
};


IonicCordovaLibraryMultiSelection.accessLibrary = function (successCallback, errorCallback, options) {
    options = options || {};
    var mediaType = options.mediaType || this.MEDIA_TYPE.PICTURE; // default picture
    
    exec(successCallback, errorCallback, 'CustomLibraryLauncher', 'accessLibrary', [mediaType]);
};

module.exports = IonicCordovaLibraryMultiSelection;