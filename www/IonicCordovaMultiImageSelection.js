var exec = require('cordova/exec');

var IonicCordovaLibraryMultiSelection = {};

IonicCordovaLibraryMultiSelection.accessLibrary = function (successCallback, errorCallback, options) {
    exec(successCallback, errorCallback, 'Camera', 'takePicture', args);
};


module.exports = IonicCordovaLibraryMultiSelection;
