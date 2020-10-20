package com.sonu.drawingapp.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonu.drawingapp.R;

public class AllDrawingsViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public AllDrawingsViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.drawing_image);
    }
}
