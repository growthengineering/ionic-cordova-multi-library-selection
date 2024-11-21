package cordova.plugin.multilibraryselection;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;

public class CustomImagePickerActivity extends Activity {
  final LinkedHashMap<Uri, Integer> selectedImageMap = new LinkedHashMap<>();

  final List<Uri> selectedImageUris = new ArrayList<>();
  private RecyclerView recyclerView;
  private ImageAdapter imageAdapter;
  private List<Uri> imageUris = new ArrayList<>();
  private boolean isLoading = false;
  private LinearLayout imageContainer; // Add this line
  private int mediaType;

  private int getResourceId(String resourceName, String resourceType) {
    return getResources().getIdentifier(resourceName, resourceType, getPackageName());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getResourceId("activity_custom_image_picker", "layout"));
    imageContainer = findViewById(getResourceId("imageContainer", "id"));
    mediaType = getIntent().getIntExtra("mediaType", 0);

    recyclerView = findViewById(getResourceId("recyclerView", "id"));
    imageAdapter = new ImageAdapter(this, imageUris);
    recyclerView.setAdapter(imageAdapter);
    recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    recyclerView.setClickable(true);

    loadImages();

    // Set up the Cancel button
    Button btnCancel = findViewById(getResourceId("btnCancel", "id"));
    btnCancel.setOnClickListener(v -> finish());

    // Set up the Done button
    Button btnDone = findViewById(getResourceId("btnDone", "id"));
    btnDone.setOnClickListener(v -> returnSelectedImages());

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (!recyclerView.canScrollVertically(1) && !isLoading) {
          loadMoreImages();
        }
      }
    });
  }

  private void loadImages() {
      Uri contentUri;
        String[] projection;
        String sortOrder;
    ContentResolver contentResolver = getContentResolver();

    if (mediaType == 1) { // VIDEO 
        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        projection = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA
        }; 
        sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";

    } else { // PICTURE (default)
        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        };
        sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
    }


     Cursor cursor = contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            sortOrder
        );


   if (cursor != null) {
            while (cursor.moveToNext()) {
                int idColumn = cursor.getColumnIndexOrThrow(mediaType == 1 ? 
                    MediaStore.Video.Media._ID : 
                    MediaStore.Images.Media._ID);
                long id = cursor.getLong(idColumn);
                Uri mediaUri = Uri.withAppendedPath(contentUri, String.valueOf(id));
                imageUris.add(mediaUri);
            }
            cursor.close();
        }
        imageAdapter.notifyDataSetChanged();
  }

  private void loadMoreImages() {
    isLoading = true;
    // Load more images logic here
    // After loading, update the adapter and set isLoading to false
    isLoading = false;
  }

  private void addImageView(Uri imageUri) {
    View itemView = LayoutInflater.from(this).inflate(
        getResourceId("image_selection_item", "layout"), 
        null
    );
    ImageView imageView = itemView.findViewById(getResourceId("imageView", "id"));
    TextView selectionIndicator = itemView.findViewById(getResourceId("selectionIndicator", "id"));

    Glide.with(this)
        .load(imageUri)
        .into(imageView);

    imageContainer.addView(itemView);
  }

  void updateSelectionIndicators(int unselectedIndex) {
    for (int i = unselectedIndex; i < selectedImageUris.size(); i++) {
        Uri selectedUri = selectedImageUris.get(i);
        int position = imageUris.indexOf(selectedUri);
        if (position != -1) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                TextView selectionIndicator = viewHolder.itemView.findViewById(
                    getResourceId("selectionIndicator", "id")
                );
                if (selectionIndicator != null) {
                    selectionIndicator.setText(String.valueOf(i + 1));
                }
            }
        }
    }
  }

  private Bitmap loadBitmapFromUri(Uri uri) {
    try {
      return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
    } catch (Exception e) {
      return null;
    }
  }

  // When done selecting images, return the result
  private void returnSelectedImages() {

    if (selectedImageUris.isEmpty()) {
      setResult(Activity.RESULT_CANCELED);
      finish();
      return;
    }

    // Create ordered list based on selection order
    ArrayList<Uri> orderedUris = new ArrayList<>(selectedImageUris);

    Intent resultIntent = new Intent();
    resultIntent.putExtra("mediaType", mediaType); // Add mediaType to result

    resultIntent.putParcelableArrayListExtra("selectedImages", orderedUris);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
  }

  // Add getter method
    public int getMediaType() {
        return mediaType;
    }
}
