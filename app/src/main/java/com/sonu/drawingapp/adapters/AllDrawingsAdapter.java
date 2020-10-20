package com.sonu.drawingapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sonu.drawingapp.R;
import com.sonu.drawingapp.viewholder.AllDrawingsViewHolder;

import java.util.List;

public class AllDrawingsAdapter extends RecyclerView.Adapter<AllDrawingsViewHolder> {
    List<String> list;

    public AllDrawingsAdapter(List<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public AllDrawingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AllDrawingsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.drawing_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AllDrawingsViewHolder holder, int position) {
        Glide.with(holder.imageView.getContext())
                .load(list.get(position))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
