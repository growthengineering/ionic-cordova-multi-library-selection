#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreLocation/CLLocationManager.h>
#import <Cordova/CDVPlugin.h>
#import <UIKit/UIKit.h>
#import "CustomImagePicker.h"

// Remove CDVPictureOptions definition as it's already defined in CDVCamera

// Remove CDVCustomImagePickerPicker definition

@interface CDVCustomImagePicker : CDVPlugin <UIImagePickerControllerDelegate,
                                           UINavigationControllerDelegate,
                                           UIPopoverControllerDelegate,
                                           CLLocationManagerDelegate,
                                           CustomImagePickerDelegate>

@property (strong) UIImagePickerController* pickerController;
@property (strong) NSMutableDictionary *metadata;
@property (strong, nonatomic) CLLocationManager *locationManager;
@property (strong) NSData* data;
@property (assign) BOOL hasPendingOperation;

- (void)takePicture:(CDVInvokedUrlCommand*)command;

@end