package edu.neu.madcourse.urban_trails;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.neu.madcourse.urban_trails.models.Trail;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.RviewHolder> {
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

    public RvAdapter(ArrayList<Trail> trails) {
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
    public void onBindViewHolder(@NonNull RviewHolder holder, int position) {
        Trail currentItem = trails.get(position);
        holder.trailImage.setImageResource(R.drawable.ic_camera);
//        holder.trailImage.setImageBitmap(currentItem.convertBase64ImageToBitmap());
//        holder.trailImage.setImageResource(currentItem.getTrailImageBase64());
        holder.trailName.setText(currentItem.getName());
        holder.trailDescription.setText(currentItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return trails.size();
    }

}
