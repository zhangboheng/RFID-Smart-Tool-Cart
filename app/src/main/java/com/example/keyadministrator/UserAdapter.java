package com.example.keyadministrator;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        int getUserId = user.getId();
        holder.nameId.setText(String.valueOf(user.getId()));
        holder.nameTextView.setText(user.getUsername()); // 假设 User 类有一个 getName() 方法
        holder.departmentTextView.setText(user.getUserPart()); // 假设 User 类有一个 getDepartment() 方法
        holder.phoneTextView.setText(user.getUserPhone()); // 假设 User 类有一个 getPhone() 方法
        String imagePath = user.getImagePath();
        if (imagePath != null &&!imagePath.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagePath)
                    .into(holder.userImage);
        }
        TextView detailTextView = holder.itemView.findViewById(R.id.detailTextView);
        detailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserDetailEditActivity.class);
                intent.putExtra("user_id", getUserId);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> newList) {
        userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameId;
        TextView nameTextView;
        TextView departmentTextView;
        TextView phoneTextView;
        ImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameId = itemView.findViewById(R.id.nameId);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            departmentTextView = itemView.findViewById(R.id.departmentTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            userImage = itemView.findViewById(R.id.user_image);
        }
    }
}
