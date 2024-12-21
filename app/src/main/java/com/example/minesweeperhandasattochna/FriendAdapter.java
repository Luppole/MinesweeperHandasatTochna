package com.example.minesweeperhandasattochna;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private ArrayList<Friend> friendsList;
    private Context context;

    public FriendAdapter(ArrayList<Friend> friendsList, Context context) {
        this.friendsList = friendsList;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.nameTextView.setText(friend.getName());
        holder.pointsTextView.setText("Points: " + friend.getPoints());

        // Decode Base64 and set profile picture
        String profilePicture = friend.getProfilePicture();
        if (profilePicture != null && !profilePicture.isEmpty()) {
            byte[] decodedString = Base64.decode(profilePicture, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.profileImageView.setImageBitmap(decodedBitmap);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profile); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, pointsTextView;
        ImageView profileImageView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friendNameTextView);
            pointsTextView = itemView.findViewById(R.id.friendPointsTextView);
            profileImageView = itemView.findViewById(R.id.friendProfileImageView);
        }
    }
}
