package cordova.plugin.multilibraryselection;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.growthengineering.dev1.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
  private List<Uri> imageUris;
  private Context context;

  public ImageAdapter(Context context, List<Uri> imageUris) {
    this.context = context;
    this.imageUris = imageUris;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.image_selection_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Uri imageUri = imageUris.get(position);
    CustomImagePickerActivity activity = (CustomImagePickerActivity) context;

    // Load the image
    Glide.with(context)
      .load(imageUri)
      .into(holder.imageView);

    // Update selection indicator
    updateSelectionIndicator(holder, imageUri, activity);

    // Handle click events
    holder.itemView.setOnClickListener(v -> {
      handleImageSelection(holder, imageUri, activity);
    });

  }

  private void updateSelectionIndicator(@NonNull ViewHolder holder, Uri imageUri, CustomImagePickerActivity activity) {
    if (activity.selectedImageUris.contains(imageUri)) {
            int selectionIndex = activity.selectedImageUris.indexOf(imageUri);
            Integer order = activity.selectedImageMap.get(imageUri);
            holder.selectionIndicator.setText(String.valueOf(order));
            holder.selectionIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.selectionIndicator.setVisibility(View.GONE);
        }
  }

  private void handleImageSelection(@NonNull ViewHolder holder, Uri imageUri, CustomImagePickerActivity activity) {
    if (!activity.selectedImageUris.contains(imageUri)) {
            // Add new selection
            activity.selectedImageUris.add(imageUri);
            int selectionOrder = activity.selectedImageUris.size();
            activity.selectedImageMap.put(imageUri, selectionOrder);
        } else {
            // Remove selection and update subsequent indicators
            int unselectedIndex = activity.selectedImageUris.indexOf(imageUri);
            activity.selectedImageUris.remove(imageUri);
            activity.selectedImageMap.remove(imageUri);
            
            // Update the order for remaining selections
            for (int i = unselectedIndex; i < activity.selectedImageUris.size(); i++) {
                Uri uri = activity.selectedImageUris.get(i);
                activity.selectedImageMap.put(uri, i + 1);
            }
        }

        notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return imageUris.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    Button cancelButton;
    Button doneButton;
    TextView selectionIndicator;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageView);
      cancelButton = itemView.findViewById(R.id.btnCancel);
      doneButton = itemView.findViewById(R.id.btnDone);
      selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
    }
  }
}
