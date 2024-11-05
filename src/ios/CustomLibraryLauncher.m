#import <Cordova/CDV.h>
#import "CustomLibraryLauncher.h"

@interface CustomLibraryLauncher ()
@property (nonatomic, strong) CDVInvokedUrlCommand* latestCommand;
@property (nonatomic, strong) CustomImagePicker* imagePicker;
@end

@implementation CustomLibraryLauncher

- (void)accessLibrary:(CDVInvokedUrlCommand*)command {
    self.latestCommand = command;
    
    // Check photo library permissions
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    if (status == PHAuthorizationStatusNotDetermined) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (status == PHAuthorizationStatusAuthorized) {
                    [self showImagePicker];
                } else {
                    [self sendNoPermissionResult:command.callbackId];
                }
            });
        }];
    } else if (status == PHAuthorizationStatusAuthorized) {
        [self showImagePicker];
    } else {
        [self sendNoPermissionResult:command.callbackId];
    }
}

- (void)showImagePicker {
    self.imagePicker = [[CustomImagePicker alloc] init];
    self.imagePicker.delegate = self;
    
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:self.imagePicker];
    navigationController.modalPresentationStyle = UIModalPresentationPageSheet;
    
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
}

#pragma mark - CustomImagePickerDelegate

- (void)didSelectImages:(NSArray<UIImage *> *)images {
    NSMutableArray *results = [NSMutableArray array];
    
    for (NSInteger i = 0; i < images.count; i++) {
        UIImage *image = images[i];
        NSString *tempFileName = [NSString stringWithFormat:@"temp_image_%ld_%d.jpg", (long)i, (int)([[NSDate date] timeIntervalSince1970] * 1000)];
        NSString *tempFilePath = [NSTemporaryDirectory() stringByAppendingPathComponent:tempFileName];
        
        NSData *imageData = UIImageJPEGRepresentation(image, 0.8);
        [imageData writeToFile:tempFilePath atomically:YES];
        
        [results addObject:tempFilePath];
    }
    
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:results];
    [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
}

- (void)didCancelImageSelection {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Selection cancelled"];
    [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
}

- (void)sendNoPermissionResult:(NSString*)callbackId {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No photo library access"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

@end