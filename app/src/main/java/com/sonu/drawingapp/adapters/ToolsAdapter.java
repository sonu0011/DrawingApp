package com.sonu.drawingapp.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonu.drawingapp.R;
import com.sonu.drawingapp.interfaces.ToolsListener;
import com.sonu.drawingapp.interfaces.ViewOnClick;
import com.sonu.drawingapp.model.ToolsItem;
import com.sonu.drawingapp.viewholder.ToolsViewHolder;

import java.util.List;

public class ToolsAdapter extends RecyclerView.Adapter<ToolsViewHolder> {
    private List<ToolsItem> list;
    private int selected = -1;
    private ToolsListener listener;

    public ToolsAdapter(List<ToolsItem> list, ToolsListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToolsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tools_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToolsViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        holder.icon.setImageResource(list.get(position).getIcon());
        holder.setViewOnClick(new ViewOnClick() {
            @Override
            public void onclick(int pos) {
                selected = pos;
                listener.onSelected(list.get(pos).getName());
                notifyDataSetChanged();
            }
        });
        if (selected == position) {
            holder.name.setTextColor(holder.name.getContext().getResources().getColor(R.color.colorAccent));
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD);
        } else {
            holder.name.setTextColor(holder.name.getContext().getResources().getColor(R.color.colorPrimaryDark));
            holder.name.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
