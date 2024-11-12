![Ionic Cordova Multi Library Selection Plugin Header Image](https://github.com/user-attachments/assets/c8cd6dc6-12a2-4cdb-b1ab-9cc3e6d2543a)

# Ionic Cordova Multi Library Selection Plugin

This Cordova plugin allows users to select multiple images and videos from the device's library for both iOS and Android platforms within an Ionic Cordova application.

![License](https://img.shields.io/github/license/your-repo/ionic-cordova-multi-library-selection)

## 💡 Features

- 📷 **Select Multiple Images:** Choose multiple images from the device's photo library.
- 🎥 **Select Multiple Videos:** Choose multiple videos from the device's video library.
- 🔄 **Toggle Between Media Types:** Easily switch between selecting images and videos.
- 📏 **Video Duration Display:** Display video duration for easy selection.

## 📦 Installation

To install the plugin, simply add it to your Ionic project using the following command:

```bash
cordova plugin add https://github.com/growthengineering/
ionic-cordova-multi-library-selection
```

## 🔗 Dependencies

- **iOS Photos Framework**: From iOS Core Library (iOS Target SDK)
- **Android Glide**: `v4.12.0`

## 🛠 Usage

Access the Cordova package in your Ionic app via the window property:

```javascript
const ionicMultiLibrarySelection = (<any>window).cordova.plugin.ionicMultiLibrarySelection;
```

### 📷 Image and Video Selection

To select multiple images or videos, use the `accessLibrary` method and specify the media type:

```javascript
// To select images
ionicMultiLibrarySelection.accessLibrary(successCallback, errorCallback, { mediaType: ionicMultiLibrarySelection.MEDIA_TYPE.PICTURE });

// To select videos
ionicMultiLibrarySelection.accessLibrary(successCallback, errorCallback, { mediaType: ionicMultiLibrarySelection.MEDIA_TYPE.VIDEO });
```

### Example Usage

```javascript
// Success callback
function successCallback(results) {
    console.log('Selected media:', results);
}

// Error callback
function errorCallback(error) {
    console.error('Error selecting media:', error);
}

// Select images
ionicMultiLibrarySelection.accessLibrary(successCallback, errorCallback, { mediaType: ionicMultiLibrarySelection.MEDIA_TYPE.PICTURE });

// Select videos
ionicMultiLibrarySelection.accessLibrary(successCallback, errorCallback, { mediaType: ionicMultiLibrarySelection.MEDIA_TYPE.VIDEO });
```

## 📱 Support

This plugin supports:
- Android 7.0 (Nougat) and above
- iOS 12 and above
- Cordova Android 10.0.1 and above
- Cordova iOS 6.2.0 and above

## 🔒 License

MIT License

Copyright (c) 2024 Growth Engineering

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
