#import "CustomImagePickerActivity.h"
#import <AVFoundation/AVFoundation.h>
#import <ImageIO/CGImageProperties.h>
#import <Photos/Photos.h>
#import <Cordova/CDVPlugin.h>

@interface CDVCustomImagePicker ()
@property (nonatomic, strong) CDVInvokedUrlCommand* latestCommand;
@end

@implementation CDVCustomImagePicker

- (BOOL)popoverSupported {
    return YES;
}

- (BOOL)usesGeolocation {
    return NO;
}

- (void)takePicture:(CDVInvokedUrlCommand*)command
{
    self.latestCommand = command;
    __weak CDVCustomImagePicker* weakSelf = self;

    [self.commandDelegate runInBackground:^{
        // Initialize your custom image picker
        dispatch_async(dispatch_get_main_queue(), ^{
            CustomImagePicker *imagePicker = [[CustomImagePicker alloc] init];
            imagePicker.delegate = self;
            [self.viewController presentViewController:imagePicker animated:YES completion:nil];
        });
    }];
}

#pragma mark - CustomImagePickerDelegate

- (void)didSelectImages:(NSArray<UIImage *> *)images {
    // Handle the selected images
    if (images.count > 0) {
        // Convert images to base64 or handle them as needed
        NSMutableArray *results = [NSMutableArray array];
        
        for (UIImage *image in images) {
            NSData *imageData = UIImageJPEGRepresentation(image, 0.8);
            NSString *base64String = [imageData base64EncodedStringWithOptions:0];
            [results addObject:base64String];
        }
        
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
        [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
    } else {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No images selected"];
        [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
    }
}

- (void)didCancelImageSelection {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Selection cancelled"];
    [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
}

- (void)sendNoPermissionResult:(NSString*)callbackId
{
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"has no access to camera"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    self.hasPendingOperation = NO;
    self.pickerController = nil;
}

@end