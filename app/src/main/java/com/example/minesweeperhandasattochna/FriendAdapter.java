package com.example.minesweeperhandasattochna;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final ArrayList<Friend> friendList;
    private final Context context;

    public FriendAdapter(ArrayList<Friend> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);

        holder.friendNameTextView.setText(friend.getName());
        holder.friendPointsTextView.setText("Points: " + friend.getPoints());

        if (friend.getProfilePicture() != null) {
            Glide.with(context).load(friend.getProfilePicture()).into(holder.friendProfilePicture);
        } else {
            holder.friendProfilePicture.setImageResource(R.drawable.default_profile);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FriendProfileActivity.class);
            intent.putExtra("friendEmail", friend.getEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView friendNameTextView, friendPointsTextView;
        ImageView friendProfilePicture;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendNameTextView);
            friendPointsTextView = itemView.findViewById(R.id.friendPointsTextView);
            friendProfilePicture = itemView.findViewById(R.id.friendProfilePicture);
        }
    }
}
