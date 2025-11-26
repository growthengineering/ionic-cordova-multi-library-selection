package cordova.plugin.multilibraryselection;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class CustomLibraryLauncher extends CordovaPlugin {
    private static final String LOG_TAG = "CustomLibraryLauncher";
    private int mediaType;
    private CallbackContext callbackContext;
    private static final int PICTURE = 0;
    private static final int VIDEO = 1;
    private static final int PHOTOLIBRARY = 2;
    private static final int PHOTO_PICKER_REQUEST = 3;

    private int srcType;
    private int destType;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MAX_SELECTION_COUNT = 10;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("accessLibrary")) {
            this.srcType = PHOTOLIBRARY; 
            this.mediaType = args.getInt(0);

            // Use Photo Picker on Android 13+ (no permissions needed - Google Play compliant)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.launchPhotoPicker();
            } else {
                // Use custom gallery on Android 12 and below (with permissions)
                if (!hasPermissions()) {
                    requestPermissions();
                    return true;
                }
                this.launchGallery();
            }
            return true;
        }
        return false;
    }

    private boolean hasPermissions() {
        String[] permissions = getPermissions();
        for (String permission : permissions) {
            if (!PermissionHelper.hasPermission(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private String[] getPermissions() {
        // Only need READ_EXTERNAL_STORAGE for Android 12 and below
        // Android 13+ uses Photo Picker which doesn't require permissions
        return new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
    }

    private void requestPermissions() {
        PermissionHelper.requestPermissions(this, PERMISSION_REQUEST_CODE, getPermissions());
    }

    private void launchGallery() {
        Intent intent = new Intent(cordova.getActivity(), CustomImagePickerActivity.class);
        intent.putExtra("mediaType", this.mediaType); // Pass mediaType to activity

        this.cordova.startActivityForResult(this, intent, PHOTOLIBRARY);
    }

    /**
     * Launches Android Photo Picker for Android 13+ (no permissions required)
     */
    private void launchPhotoPicker() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        
        // Set media type filter
        switch (mediaType) {
            case PICTURE:
                intent.setType("image/*");
                break;
            case VIDEO:
                intent.setType("video/*");
                break;
            default:
                // Both images and videos
                intent.setType("*/*");
                String[] mimeTypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                break;
        }

        // Enable multiple selection
        intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, MAX_SELECTION_COUNT);
        
        if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
            this.cordova.startActivityForResult(this, intent, PHOTO_PICKER_REQUEST);
        } else {
            this.callbackContext.error("Photo Picker not available");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PHOTOLIBRARY) {
            // Handle custom gallery result
            if (resultCode == Activity.RESULT_OK && intent != null) {
                ArrayList<Uri> selectedImages = intent.getParcelableArrayListExtra("selectedImages");
                processResultFromGallery(selectedImages);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture("No Image Selected");
            } else {
                this.failPicture("Selection did not complete!");
            }
        } else if (requestCode == PHOTO_PICKER_REQUEST) {
            // Handle Photo Picker result
            if (resultCode == Activity.RESULT_OK && intent != null) {
                ArrayList<Uri> selectedImages = getSelectedUrisFromPhotoPicker(intent);
                processResultFromGallery(selectedImages);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture("No Image Selected");
            } else {
                this.failPicture("Selection did not complete!");
            }
        }
    }

    /**
     * Extracts selected URIs from Photo Picker result
     */
    private ArrayList<Uri> getSelectedUrisFromPhotoPicker(Intent data) {
        ArrayList<Uri> uris = new ArrayList<>();
        
        // Check for multiple selections (ClipData)
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (uri != null) {
                    // Request persistent access to the URI
                    takePersistableUriPermission(uri);
                    uris.add(uri);
                }
            }
        } else {
            // Single selection
            Uri uri = data.getData();
            if (uri != null) {
                // Request persistent access to the URI
                takePersistableUriPermission(uri);
                uris.add(uri);
            }
        }
        
        return uris;
    }

    /**
     * Takes persistable URI permission for long-term access
     */
    private void takePersistableUriPermission(Uri uri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            cordova.getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            Log.w(LOG_TAG, "Could not take persistable permission for URI: " + uri, e);
            // This is not critical - the URI will still work for the current session
        }
    }

    private void processResultFromGallery(ArrayList<Uri> selectedImages) {
        if (selectedImages == null || selectedImages.isEmpty()) {
            this.failPicture("No images selected");
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (Uri uri : selectedImages) {
            jsonArray.put(uri.toString());
        }
        
        this.callbackContext.success(jsonArray);
    }

    private void failPicture(String message) {
        this.callbackContext.error(message);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    this.callbackContext.error("Permission denied");
                    return;
                }
            }
            this.launchGallery();
        }
    }
}