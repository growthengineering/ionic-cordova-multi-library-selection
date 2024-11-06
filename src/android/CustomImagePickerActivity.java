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

import com.growthengineering.dev1.R;
import androidx.annotation.NonNull;
import com.growthengineering.dev1.R;
import com.bumptech.glide.Glide;

public class CustomImagePickerActivity extends Activity {
  final LinkedHashMap<Uri, Integer> selectedImageMap = new LinkedHashMap<>();

  final List<Uri> selectedImageUris = new ArrayList<>();
  private RecyclerView recyclerView;
  private ImageAdapter imageAdapter;
  private List<Uri> imageUris = new ArrayList<>();
  private boolean isLoading = false;
  private LinearLayout imageContainer; // Add this line


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_custom_image_picker);
    imageContainer = findViewById(R.id.imageContainer); // Initialize it

    recyclerView = findViewById(R.id.recyclerView);
    imageAdapter = new ImageAdapter(this, imageUris);
    recyclerView.setAdapter(imageAdapter);
    recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
    recyclerView.setClickable(true);

    loadImages();

    // Set up the Cancel button
    Button btnCancel = findViewById(R.id.btnCancel);
    btnCancel.setOnClickListener(v -> finish()); // Close the activity

    // Set up the Done button
    Button btnDone = findViewById(R.id.btnDone);
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
    String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
    ContentResolver contentResolver = getContentResolver();

    Cursor cursor = contentResolver.query(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      projection,
      null,
      null,
      MediaStore.Images.Media.DATE_TAKEN + " DESC"
    );

    if (cursor != null) {
      while (cursor.moveToNext()) {
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        long id = cursor.getLong(idColumn);
        Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        imageUris.add(imageUri); // Add the image URI to the list
      }
      cursor.close();
    }
    imageAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
  }

  private void loadMoreImages() {
    isLoading = true;
    // Load more images logic here
    // After loading, update the adapter and set isLoading to false
    isLoading = false;
  }

  private void addImageView(Uri imageUri) {
    View itemView = LayoutInflater.from(this).inflate(R.layout.image_selection_item, null);
    ImageView imageView = itemView.findViewById(R.id.imageView);
    TextView selectionIndicator = itemView.findViewById(R.id.selectionIndicator);

    // Load image using Glide
    Glide.with(this)
      .load(imageUri)
      .into(imageView);

    imageContainer.addView(itemView);
  }

  // New method to update selection indicators
  void updateSelectionIndicators(int unselectedIndex) {
    // Loop through the selected image URIs starting from the unselected index
    for (int i = unselectedIndex; i < selectedImageUris.size(); i++) {
      Uri selectedUri = selectedImageUris.get(i);

      // Find the corresponding view in the RecyclerView
      int position = imageUris.indexOf(selectedUri);
      if (position != -1) {
        // Get the ViewHolder for the selected item
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
          // Update the selection indicator
          TextView selectionIndicator = viewHolder.itemView.findViewById(R.id.selectionIndicator);
          if (selectionIndicator != null) {
            selectionIndicator.setText(String.valueOf(i + 1)); // Update the text to reflect the new order
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
    resultIntent.putParcelableArrayListExtra("selectedImages", orderedUris);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
  }
}
