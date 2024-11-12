#import <UIKit/UIKit.h>
#import <Photos/Photos.h>

@protocol CustomImagePickerDelegate <NSObject>
- (void)didSelectImages:(NSArray<UIImage *> *)images;
- (void)didSelectVideos:(NSArray<NSString *> *)videoPaths;
- (void)didCancelImageSelection;
@end

@interface CustomImagePicker : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource>

@property (nonatomic, weak) id<CustomImagePickerDelegate> delegate;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSMutableArray<PHAsset *> *selectedAssets;
@property (nonatomic, strong) NSMutableArray<PHAsset *> *allAssets;
@property (nonatomic, assign) NSInteger mediaType; // 0 for images, 1 for videos

@end
