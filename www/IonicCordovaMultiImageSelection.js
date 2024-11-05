var exec = require('cordova/exec');

var IonicCordovaLibraryMultiSelection = {};

IonicCordovaLibraryMultiSelection.accessLibrary = function (successCallback, errorCallback, options) {
    var args = [];
    
    // Define default options
    var defaultOptions = {
        quality: 50,
        destinationType: 1, // FILE_URI
        sourceType: 0,     // PHOTOLIBRARY
        targetWidth: -1,   // No resize
        targetHeight: -1,  // No resize
        encodingType: 0,   // JPEG
        mediaType: 0,      // PICTURE
        allowEdit: false,
        correctOrientation: true,
        saveToPhotoAlbum: false
    };

    // Merge provided options with defaults
    options = Object.assign({}, defaultOptions, options || {});

    // Build args array in the expected order
    args.push(options.quality);
    args.push(options.destinationType);
    args.push(options.sourceType);
    args.push(options.targetWidth);
    args.push(options.targetHeight);
    args.push(options.encodingType);
    args.push(options.mediaType);
    args.push(options.allowEdit);
    args.push(options.correctOrientation);
    args.push(options.saveToPhotoAlbum);

    exec(successCallback, errorCallback, 'CustomLibraryLauncher', 'accessLibrary', args);
};

module.exports = IonicCordovaLibraryMultiSelection;