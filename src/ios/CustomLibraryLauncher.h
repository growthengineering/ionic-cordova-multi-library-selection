#import <Cordova/CDV.h>
#import "CustomImagePicker.h"

@interface CustomLibraryLauncher : CDVPlugin <CustomImagePickerDelegate>
- (void)accessLibrary:(CDVInvokedUrlCommand*)command;
@end