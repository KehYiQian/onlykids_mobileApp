package com.example.onlykids_mobile_application;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<Video> videoList;
    private Set<String> reportedVideoIds;
    private Consumer<String> showNotification;

    public VideoAdapter(Context context, List<Video> videoList, Set<String> reportedVideoIds, Consumer<String> showNotification) {
        this.context = context;
        this.videoList = videoList;
        this.reportedVideoIds = reportedVideoIds;
        this.showNotification = showNotification;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videoList.get(position);
        holder.tvVideoTitle.setText(video.getTitle());
        Picasso.get().load(video.getThumbnailUrl()).into(holder.ivThumbnail);

        // Ensure the report icon is visible and enabled for all videos initially
        holder.ivReport.setVisibility(View.VISIBLE);
        holder.ivReport.setEnabled(true);

        holder.ivReport.setOnClickListener(v -> {
            // Add video ID to reported set
            reportedVideoIds.add(video.getVideoId());
            // Remove the video from the list
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                videoList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            }
            // Show notification
            showNotification.accept("Video reported successfully!");
            // Disable the report icon
            holder.ivReport.setEnabled(false);
            holder.ivReport.setVisibility(View.GONE); // Hide the icon after reporting
        });

        // Handle video click to open in YouTube
        holder.itemView.setOnClickListener(v -> {
            String videoId = video.getVideoId();
            // Try to open in YouTube app using the vnd.youtube URI scheme
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            try {
                context.startActivity(appIntent);
            } catch (Exception e) {
                // Fallback to browser if YouTube app is not installed
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
                context.startActivity(webIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateVideos(List<Video> newVideos) {
        videoList.clear();
        videoList.addAll(newVideos);
        notifyDataSetChanged();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvVideoTitle;
        ImageView ivReport;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvVideoTitle = itemView.findViewById(R.id.tv_video_title);
            ivReport = itemView.findViewById(R.id.iv_report);
        }
    }
}