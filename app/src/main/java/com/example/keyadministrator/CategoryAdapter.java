package com.example.keyadministrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnItemClickListener listener; // 添加点击监听器接口


    public CategoryAdapter(List<Category> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    // 点击监听器接口
    public interface OnItemClickListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView indexTextView;
        public TextView typeNameTextView;
        public TextView typeUnitTextView;
        public TextView editButton;
        public TextView deleteButton;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            indexTextView = itemView.findViewById(R.id.textViewIndex); // 替换 R.id.textViewIndex 为你的列表项布局中索引序号 TextView 的 ID
            typeNameTextView = itemView.findViewById(R.id.textViewTypeName); // 替换 R.id.textViewTypeName 为你的列表项布局中类型名称 TextView 的 ID
            typeUnitTextView = itemView.findViewById(R.id.textViewTypeUnit); // 替换 R.id.textViewTypeUnit 为你的列表项布局中类型单位 TextView 的 ID
            editButton = itemView.findViewById(R.id.textViewEdit);
            deleteButton = itemView.findViewById(R.id.textViewDelete);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false); // 替换 R.layout.category_item 为你的列表项布局文件
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.indexTextView.setText(String.valueOf(category.getId()));
        holder.typeNameTextView.setText(category.getTypeName());
        holder.typeUnitTextView.setText(category.getTypeUnit());
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if(clickedPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(categories.get(clickedPosition));
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(categories.get(clickedPosition));
                    categories.remove(clickedPosition);
                    notifyItemRemoved(clickedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}