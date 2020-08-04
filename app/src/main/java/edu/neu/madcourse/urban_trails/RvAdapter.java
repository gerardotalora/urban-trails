package edu.neu.madcourse.urban_trails;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import edu.neu.madcourse.urban_trails.fragments.HomeFragment;
import edu.neu.madcourse.urban_trails.models.Trail;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.RviewHolder> {
    private final Context context;
    private ArrayList<Trail> trails;
    private ItemClickListener listener;

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public static class RviewHolder extends RecyclerView.ViewHolder {
        public ImageView trailImage;
        public TextView trailName;
        public TextView trailDescription;

        public RviewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);
            trailImage = itemView.findViewById(R.id.trail_image);
            trailName = itemView.findViewById(R.id.trail_name);
            trailDescription = itemView.findViewById(R.id.trail_description);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getLayoutPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    public RvAdapter(Context context, ArrayList<Trail> trails) {
        this.context = context;
        this.trails = trails;
    }

    @NonNull
    @Override
    public RviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card_item, parent, false);
        return new RviewHolder(view, listener);
    }

    //TODO Get trail image and data
    @Override
    public void onBindViewHolder(@NonNull final RviewHolder holder, int position) {
        Trail currentItem = trails.get(position);
        holder.trailImage.setImageResource(R.drawable.ic_camera);
        if (currentItem.getTrailImageFilename() != null) {
            Utils.displayThumbnail(context, holder.trailImage, currentItem.getTrailImageFilename(), null);
        }
        holder.trailName.setText(currentItem.getName());
        holder.trailDescription.setText(currentItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return trails.size();
    }

}
