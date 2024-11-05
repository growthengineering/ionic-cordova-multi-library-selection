package cordova.plugin.multilibraryselection;

import android.Manifest;
import android.app.Activity;
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
    
    // Constants for type of media to select
    private static final int PHOTOLIBRARY = 0;
    private static final int PICTURE = 0;
    private static final int VIDEO = 1;
    private static final int ALLMEDIA = 2;
    
    private static final int DATA_URL = 0;
    private static final int FILE_URI = 1;
    
    private int destType;
    private int srcType;
    private int mediaType;
    private CallbackContext callbackContext;
    
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("accessLibrary")) {
            this.srcType = PHOTOLIBRARY;
            this.destType = args.getInt(1);
            this.mediaType = args.getInt(6);

            if (!hasPermissions()) {
                requestPermissions();
                return true;
            }

            this.launchGallery();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            switch (mediaType) {
                case PICTURE:
                    return new String[]{ Manifest.permission.READ_MEDIA_IMAGES };
                case VIDEO:
                    return new String[]{ Manifest.permission.READ_MEDIA_VIDEO };
                default:
                    return new String[]{ 
                        Manifest.permission.READ_MEDIA_IMAGES, 
                        Manifest.permission.READ_MEDIA_VIDEO 
                    };
            }
        } else {
            return new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
        }
    }

    private void requestPermissions() {
        PermissionHelper.requestPermissions(this, PERMISSION_REQUEST_CODE, getPermissions());
    }

    private void launchGallery() {
        Intent intent = new Intent(cordova.getActivity(), CustomImagePickerActivity.class);
        this.cordova.startActivityForResult(this, intent, PHOTOLIBRARY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            ArrayList<Uri> selectedImages = intent.getParcelableArrayListExtra("selectedImages");
            processResultFromGallery(selectedImages);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            this.failPicture("No Image Selected");
        } else {
            this.failPicture("Selection did not complete!");
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