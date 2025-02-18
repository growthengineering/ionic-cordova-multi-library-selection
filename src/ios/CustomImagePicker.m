#import "CustomImagePicker.h"

@implementation CustomImagePicker

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Hide the navigation bar
    self.navigationController.navigationBarHidden = YES;
    
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];
    [[[UIApplication sharedApplication] keyWindow] endEditing:YES];
    
    
    // Initialize selectedAssets
    self.selectedAssets = [NSMutableArray array];
    
    // Create a header view - adjust frame to account for status bar
     UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 60)];
    headerView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7]; // Set background color with transparency

    // Add Cancel button with user interaction enabled
    UIButton *cancelButton = [UIButton buttonWithType:UIButtonTypeSystem];
    cancelButton.userInteractionEnabled = YES;
    [cancelButton setTitle:@"Cancel" forState:UIControlStateNormal];
    [cancelButton addTarget:self action:@selector(cancelImageSelection:) forControlEvents:UIControlEventTouchUpInside];
    cancelButton.translatesAutoresizingMaskIntoConstraints = NO;
    [headerView addSubview:cancelButton];

    // Add Done button with user interaction enabled
    UIButton *doneButton = [UIButton buttonWithType:UIButtonTypeSystem];
    doneButton.userInteractionEnabled = YES;
    [doneButton setTitle:@"Done" forState:UIControlStateNormal];
    [doneButton addTarget:self action:@selector(returnSelectedImages:) forControlEvents:UIControlEventTouchUpInside];
    doneButton.translatesAutoresizingMaskIntoConstraints = NO;
    [headerView addSubview:doneButton];

    // Set button styles
    [cancelButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    cancelButton.backgroundColor = [UIColor clearColor];
    doneButton.backgroundColor = [UIColor clearColor];

    // Set constraints for the buttons
    [NSLayoutConstraint activateConstraints:@[
        [cancelButton.leadingAnchor constraintEqualToAnchor:headerView.leadingAnchor constant:20],
        [cancelButton.centerYAnchor constraintEqualToAnchor:headerView.centerYAnchor],
        [cancelButton.widthAnchor constraintEqualToConstant:70],
        [cancelButton.heightAnchor constraintEqualToConstant:40],
        
        [doneButton.trailingAnchor constraintEqualToAnchor:headerView.trailingAnchor constant:-20],
        [doneButton.centerYAnchor constraintEqualToAnchor:headerView.centerYAnchor],
        [doneButton.widthAnchor constraintEqualToConstant:70],
        [doneButton.heightAnchor constraintEqualToConstant:40]
    ]];

    // Add the header view to the main view
    [self.view addSubview:headerView];

    // Initialize the collection view layout
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = CGSizeMake(130, 130); // Increased size to reduce black space
    layout.minimumInteritemSpacing = 0; // Adjust spacing between items
    layout.minimumLineSpacing = 0; // Adjust spacing between lines

    self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(headerView.frame), self.view.bounds.size.width, self.view.bounds.size.height - CGRectGetMaxY(headerView.frame)) collectionViewLayout:layout];
    
    // Set the data source and delegate
    self.collectionView.dataSource = self;
    self.collectionView.delegate = self;
    
    // Register a cell class
    [self.collectionView registerClass:[UICollectionViewCell class] forCellWithReuseIdentifier:@"CellIdentifier"];
    
    // Add the collection view to the view hierarchy
    [self.view addSubview:self.collectionView];
    
    // Load images
    [self loadImages];
}
 
 - (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    // Force dismiss keyboard when view is about to appear
    [[UIApplication sharedApplication] sendAction:@selector(resignFirstResponder) to:nil from:nil forEvent:nil];
    [[[UIApplication sharedApplication] keyWindow] endEditing:YES];
}

- (void)loadImages {
    PHFetchOptions *fetchOptions = [[PHFetchOptions alloc] init];
    fetchOptions.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO]];
    
    // Filter based on media type
    if (self.mediaType == 1) { // Video
        fetchOptions.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeVideo];
    } else { // Images
        fetchOptions.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeImage];
    }
    
    PHFetchResult *fetchResult = [PHAsset fetchAssetsWithOptions:fetchOptions];
    
    self.allAssets = [NSMutableArray array];
    for (PHAsset *asset in fetchResult) {
        [self.allAssets addObject:asset];
    }
    
    [self.collectionView reloadData];
}

// Implement UICollectionViewDelegate and UICollectionViewDataSource methods
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.allAssets.count; // Return the number of images
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"CellIdentifier" forIndexPath:indexPath];
    
    PHAsset *asset = self.allAssets[indexPath.item];
    PHImageManager *imageManager = [PHImageManager defaultManager];
    
    // Add image request options for better quality
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    options.deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic; // Use opportunistic to get a quick image first
    options.resizeMode = PHImageRequestOptionsResizeModeFast;
    
    // Calculate size based on screen scale for better quality
    CGFloat scale = [UIScreen mainScreen].scale;
    CGSize targetSize = CGSizeMake(130 * scale, 130 * scale);
    
    // Store the current index path in the cell's tag
    cell.tag = indexPath.item;
    
    [imageManager requestImageForAsset:asset
                          targetSize:targetSize
                         contentMode:PHImageContentModeAspectFill
                             options:options  // Added options
                       resultHandler:^(UIImage *result, NSDictionary *info) {
        // Check if the cell is still displaying the correct asset
        if (cell.tag == indexPath.item) {
            dispatch_async(dispatch_get_main_queue(), ^{
                UIImageView *imageView = [[UIImageView alloc] initWithImage:result];
                imageView.contentMode = UIViewContentModeScaleAspectFill;
                imageView.clipsToBounds = YES;
                cell.backgroundView = imageView;
                
                // Add video duration indicator if it's a video
                if (self.mediaType == 1 && asset.duration > 0) {
                    UILabel *durationLabel = [cell.contentView viewWithTag:200];
                    if (!durationLabel) {
                        durationLabel = [[UILabel alloc] initWithFrame:CGRectMake(5, 5, 50, 20)];
                        durationLabel.tag = 200;
                        durationLabel.textColor = [UIColor whiteColor];
                        durationLabel.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.7];
                        durationLabel.font = [UIFont systemFontOfSize:12];
                        durationLabel.textAlignment = NSTextAlignmentCenter;
                        durationLabel.layer.cornerRadius = 4;
                        durationLabel.layer.masksToBounds = YES;
                        [cell.contentView addSubview:durationLabel];
                    }
                    durationLabel.text = [self formatDuration:asset.duration];
                }
                
                // Remove existing badge if any
                UILabel *existingBadge = [cell.contentView viewWithTag:100];
                if (existingBadge) {
                    [existingBadge removeFromSuperview];
                }

                // Add badge if the asset is selected
                if ([self.selectedAssets containsObject:asset]) {
                    UILabel *badgeLabel = [[UILabel alloc] initWithFrame:CGRectMake(cell.bounds.size.width - 40, cell.bounds.size.height - 40, 30, 30)]; // Bottom-right position
                    badgeLabel.tag = 100; // Set a tag to identify the badge later
                    badgeLabel.text = [NSString stringWithFormat:@"%lu", (unsigned long)[self.selectedAssets indexOfObject:asset] + 1]; // Display the index + 1
                    badgeLabel.textAlignment = NSTextAlignmentCenter;
                    badgeLabel.backgroundColor = [UIColor colorWithRed:179/255.0 green:0/255.0 blue:27/255.0 alpha:1.0]; // Changed to #B3001B
                    badgeLabel.textColor = [UIColor whiteColor];
                    badgeLabel.layer.cornerRadius = 15; // Half of the width/height for a circle
                    badgeLabel.layer.masksToBounds = YES;
                    badgeLabel.font = [UIFont boldSystemFontOfSize:14];
            
                    [cell.contentView addSubview:badgeLabel]; // Add badge to cell
            }
        });
    }];
    
    return cell;
}

- (NSString *)formatDuration:(NSTimeInterval)duration {
    NSInteger minutes = (NSInteger)duration / 60;
    NSInteger seconds = (NSInteger)duration % 60;
    return [NSString stringWithFormat:@"%02ld:%02ld", (long)minutes, (long)seconds];
}

- (void)returnSelectedImages:(UIButton *)sender {
    if (self.mediaType == 1) {
        [self returnSelectedVideos];
    } else {
        [self returnSelectedImagesOriginal];
    }
}

// Rename existing image return method
- (void)returnSelectedImagesOriginal {

    // Create an overlay view to block user interaction
    UIView *overlayView = [[UIView alloc] initWithFrame:self.view.bounds];
    overlayView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.5];
    overlayView.userInteractionEnabled = YES; // Ensure it blocks touches
    [self.view addSubview:overlayView];

    // Create and configure the activity indicator
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleLarge];
    spinner.center = self.view.center;
    spinner.hidesWhenStopped = YES;
    [self.view addSubview:spinner];
    [spinner startAnimating];

    // Create an array to store images in the correct order
    NSMutableArray *selectedImages = [NSMutableArray arrayWithCapacity:self.selectedAssets.count];
    // Pre-fill array with nil values to maintain order
    for (NSInteger i = 0; i < self.selectedAssets.count; i++) {
        [selectedImages addObject:[NSNull null]];
    }

    dispatch_group_t group = dispatch_group_create();
    
    // Process each asset while maintaining its selection order
    [self.selectedAssets enumerateObjectsUsingBlock:^(PHAsset *asset, NSUInteger idx, BOOL *stop) {
        dispatch_group_enter(group);
        PHImageManager *imageManager = [PHImageManager defaultManager];
        
        PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
        options.synchronous = NO;
        options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
        options.resizeMode = PHImageRequestOptionsResizeModeNone;
        
        [imageManager requestImageForAsset:asset
                              targetSize:PHImageManagerMaximumSize
                             contentMode:PHImageContentModeDefault
                                 options:options
                           resultHandler:^(UIImage *result, NSDictionary *info) {
            if (result) {
                UIImage *fixedImage = [self fixOrientation:result withOrientation:result.imageOrientation];
                selectedImages[idx] = fixedImage; // Place image at correct index
            }
            dispatch_group_leave(group);
        }];
    }];
    
    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        [spinner stopAnimating]; // Stop and hide the spinner
        [overlayView removeFromSuperview]; // Remove the overlay

        // Remove any potential null values
        NSMutableArray *finalImages = [NSMutableArray array];
        for (id obj in selectedImages) {
            if (![obj isEqual:[NSNull null]]) {
                [finalImages addObject:obj];
            }
        }
        
        if ([self.delegate respondsToSelector:@selector(didSelectImages:)]) {
            [self.delegate didSelectImages:finalImages];
        }
        [self dismissViewControllerAnimated:YES completion:nil];
    });
}

// Add new method for video return
- (void)returnSelectedVideos {
    NSMutableArray *selectedVideoPaths = [NSMutableArray arrayWithCapacity:self.selectedAssets.count];
    dispatch_group_t group = dispatch_group_create();
    
    [self.selectedAssets enumerateObjectsUsingBlock:^(PHAsset *asset, NSUInteger idx, BOOL *stop) {
        dispatch_group_enter(group);
        
        PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
        options.version = PHVideoRequestOptionsVersionOriginal;
        options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
        options.networkAccessAllowed = YES; // Add this to allow iCloud videos
        
        [[PHImageManager defaultManager] requestAVAssetForVideo:asset
                                                      options:options
                                                resultHandler:^(AVAsset *avAsset, AVAudioMix *audioMix, NSDictionary *info) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([avAsset isKindOfClass:[AVURLAsset class]]) {
                    AVURLAsset *urlAsset = (AVURLAsset *)avAsset;
                    NSString *tempFileName = [NSString stringWithFormat:@"temp_video_%lu_%d.mp4", (unsigned long)idx, (int)([[NSDate date] timeIntervalSince1970] * 1000)];
                    NSString *tempFilePath = [NSTemporaryDirectory() stringByAppendingPathComponent:tempFileName];
                    
                    // Remove existing file if it exists
                    if ([[NSFileManager defaultManager] fileExistsAtPath:tempFilePath]) {
                        [[NSFileManager defaultManager] removeItemAtPath:tempFilePath error:nil];
                    }
                    
                    NSError *error;
                    if ([[NSFileManager defaultManager] copyItemAtURL:urlAsset.URL toURL:[NSURL fileURLWithPath:tempFilePath] error:&error]) {
                        [selectedVideoPaths addObject:tempFilePath];
                        NSLog(@"Video saved successfully at: %@", tempFilePath);
                    } else {
                        NSLog(@"Error saving video: %@", error);
                    }
                } else {
                    NSLog(@"Asset is not a URL asset");
                }
                dispatch_group_leave(group);
            });
        }];
    }];
    
    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        NSLog(@"Selected video paths: %@", selectedVideoPaths);
        if ([self.delegate respondsToSelector:@selector(didSelectVideos:)]) {
            [self.delegate didSelectVideos:selectedVideoPaths];
        }
        [self dismissViewControllerAnimated:YES completion:nil];
    });
}

// Method to fix the orientation of the image
- (UIImage *)fixOrientation:(UIImage *)image withOrientation:(UIImageOrientation)orientation {
    if (orientation == UIImageOrientationUp) return image; // No need to fix

    UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
    [image drawInRect:(CGRect){0, 0, image.size}];
    UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return normalizedImage;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    PHAsset *asset = self.allAssets[indexPath.item];
    
    if ([self.selectedAssets containsObject:asset]) {
        // Deselect the asset
        NSInteger unselectedIndex = [self.selectedAssets indexOfObject:asset];
        [self.selectedAssets removeObject:asset];
        
        // Update badge labels for remaining selected assets
        [self updateBadgeLabelsAfterUnselectionAtIndex:unselectedIndex];
    } else {
        // Select the asset
        [self.selectedAssets addObject:asset];
    }
    
    // Reload the cell to update its appearance
    [collectionView reloadItemsAtIndexPaths:@[indexPath]];
}

// Method to update badge labels after an image is unselected
- (void)updateBadgeLabelsAfterUnselectionAtIndex:(NSInteger)unselectedIndex {
    for (NSInteger i = unselectedIndex; i < self.selectedAssets.count; i++) {
        PHAsset *asset = self.selectedAssets[i];
        UICollectionViewCell *cell = [self.collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:[self.allAssets indexOfObject:asset] inSection:0]];
        
        if (cell) {
            UILabel *badgeLabel = [cell.contentView viewWithTag:100]; // Assuming tag 100 is used for the badge
            if (badgeLabel) {
                badgeLabel.text = [NSString stringWithFormat:@"%lu", (unsigned long)(i + 1)]; // Update badge text
            }
        }
    }
}

- (void)cancelImageSelection:(UIButton *)sender  {
    if ([self.delegate respondsToSelector:@selector(didCancelImageSelection)]) {
        [self.delegate didCancelImageSelection];
    }
    
    // Make sure we're on the main thread when dismissing
    dispatch_async(dispatch_get_main_queue(), ^{
        [self dismissViewControllerAnimated:YES completion:nil];
    });
}

- (void)verifyAndExportVideo:(AVURLAsset *)urlAsset toPath:(NSString *)tempFilePath completion:(void (^)(BOOL success))completion {
    // First check if we can access the video URL
    if ([[NSFileManager defaultManager] isReadableFileAtPath:urlAsset.URL.path]) {
        NSError *error;
        if ([[NSFileManager defaultManager] copyItemAtURL:urlAsset.URL toURL:[NSURL fileURLWithPath:tempFilePath] error:&error]) {
            NSLog(@"Video exported successfully to: %@", tempFilePath);
            completion(YES);
        } else {
            NSLog(@"Failed to export video: %@", error);
            completion(NO);
        }
    } else {
        NSLog(@"Cannot access video at path: %@", urlAsset.URL.path);
        completion(NO);
    }
}

@end
