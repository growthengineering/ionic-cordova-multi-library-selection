#import <Cordova/CDV.h>
#import "CustomLibraryLauncher.h"

@interface CustomLibraryLauncher ()
@property (nonatomic, strong) CDVInvokedUrlCommand* latestCommand;
@property (nonatomic, strong) CustomImagePicker* imagePicker;
@end

@implementation CustomLibraryLauncher

- (void)accessLibrary:(CDVInvokedUrlCommand*)command {
    self.latestCommand = command;
    
    // Get media type from arguments
    NSNumber *mediaType = [command.arguments objectAtIndex:0];
    
    // Check photo library permissions
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    if (status == PHAuthorizationStatusNotDetermined) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (status == PHAuthorizationStatusAuthorized) {
                    [self showImagePickerWithMediaType:[mediaType integerValue]];
                } else {
                    [self sendNoPermissionResult:command.callbackId];
                }
            });
        }];
    } else if (status == PHAuthorizationStatusAuthorized) {
        [self showImagePickerWithMediaType:[mediaType integerValue]];
    } else {
        [self sendNoPermissionResult:command.callbackId];
    }
}

- (void)showImagePickerWithMediaType:(NSInteger)mediaType {
    self.imagePicker = [[CustomImagePicker alloc] init];
    self.imagePicker.delegate = self;
    self.imagePicker.mediaType = mediaType;
    
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];
    [[[UIApplication sharedApplication] keyWindow] endEditing:YES];
    

    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:self.imagePicker];
    navigationController.modalPresentationStyle = UIModalPresentationPageSheet;
    
    [self.viewController presentViewController:navigationController animated:YES completion:nil];
}

#pragma mark - CustomImagePickerDelegate

- (UIImage *)resizeImageIfNeeded:(UIImage *)image {
    CGFloat maxDimension = 4480.0;
    CGFloat width = image.size.width;
    CGFloat height = image.size.height;
    
    // Only resize if image exceeds max dimension
    if (width <= maxDimension && height <= maxDimension) {
        return image;
    }
    
    CGFloat scaleFactor;
    if (width > height) {
        scaleFactor = maxDimension / width;
    } else {
        scaleFactor = maxDimension / height;
    }
    
    CGSize newSize = CGSizeMake(width * scaleFactor, height * scaleFactor);
    
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 1.0);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *resizedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return resizedImage;
}

- (void)didSelectImages:(NSArray<UIImage *> *)images {
    NSMutableArray *results = [NSMutableArray array];
    
    for (NSInteger i = 0; i < images.count; i++) {
        UIImage *image = images[i];
        
        // Resize image if needed
        UIImage *resizedImage = [self resizeImageIfNeeded:image];
        
        NSString *tempFileName = [NSString stringWithFormat:@"temp_image_%ld_%d.jpg", (long)i, (int)([[NSDate date] timeIntervalSince1970] * 1000)];
        NSString *tempFilePath = [NSTemporaryDirectory() stringByAppendingPathComponent:tempFileName];
        
        NSData *imageData = UIImageJPEGRepresentation(resizedImage, 0.90);
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

- (void)didSelectVideos:(NSArray<NSString *> *)videoPaths {
    NSLog(@"Received video paths in launcher: %@", videoPaths);
    
    // Verify paths exist and are accessible
    NSMutableArray *validPaths = [NSMutableArray array];
    for (NSString *path in videoPaths) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
            [validPaths addObject:path];
            NSLog(@"Valid video path: %@", path);
        } else {
            NSLog(@"Invalid video path: %@", path);
        }
    }
    
    if (validPaths.count > 0) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:validPaths];
        [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
    } else {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR 
                                                 messageAsString:@"No valid video paths found"];
        [self.commandDelegate sendPluginResult:result callbackId:self.latestCommand.callbackId];
    }
}

@end