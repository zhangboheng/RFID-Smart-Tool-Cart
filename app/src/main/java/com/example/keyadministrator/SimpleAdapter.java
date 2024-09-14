package com.example.keyadministrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {

    private final List<Notify> mData;

    public SimpleAdapter(List<Notify> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notify notification = mData.get(position);
        String formattedId = String.format(Locale.getDefault(),"%03d", notification.getId());
        holder.itemNumber.setText(formattedId);
        holder.itemStatue.setText(mData.get(position).getStatus());
        holder.itemName.setText(mData.get(position).getItem());
        holder.itemTime.setText(mData.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNumber;
        TextView itemStatue;
        TextView itemName;
        TextView itemTime;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNumber = itemView.findViewById(R.id.itemNumber);
            itemStatue = itemView.findViewById(R.id.itemStatue);
            itemName = itemView.findViewById(R.id.itemName);
            itemTime = itemView.findViewById(R.id.itemTime);
        }
    }
}
