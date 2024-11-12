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
    Uri mediaUri = imageUris.get(position);
        CustomImagePickerActivity activity = (CustomImagePickerActivity) context;

        // Load the media (image or video thumbnail)
        if (activity.getMediaType() == 1) { // VIDEO
            Glide.with(context)
                .load(mediaUri)
                .thumbnail(0.1f)
                .into(holder.imageView);
            
            // Show video duration
            String duration = getVideoDuration(mediaUri);
            holder.durationLabel.setText(duration);
            holder.durationLabel.setVisibility(View.VISIBLE);
        } else {
            Glide.with(context)
                .load(mediaUri)
                .into(holder.imageView);
            holder.durationLabel.setVisibility(View.GONE);
        }

          // Update selection indicator
          updateSelectionIndicator(holder, mediaUri, activity);

          // Handle click events
          holder.itemView.setOnClickListener(v -> {
            handleImageSelection(holder, mediaUri, activity);
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

  private String getVideoDuration(Uri videoUri) {
    try {
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);
        String time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        long duration = timeInMillisec / 1000;
        long minutes = duration / 60;
        long seconds = duration % 60;
        retriever.release();
        return String.format("%d:%02d", minutes, seconds);
    } catch (Exception e) {
        return "";
    }
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
    TextView durationLabel;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.imageView);
      cancelButton = itemView.findViewById(R.id.btnCancel);
      doneButton = itemView.findViewById(R.id.btnDone);
      selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
      durationLabel = itemView.findViewById(R.id.durationLabel);
    }
  }
}
