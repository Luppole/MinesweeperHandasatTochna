package com.example.minesweeperhandasattochna;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final ArrayList<Friend> friendsList;

    public FriendAdapter(ArrayList<Friend> friendsList) {
        this.friendsList = friendsList;
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
        holder.friendNameTextView.setText(friend.getName());
        holder.friendPointsTextView.setText("Points: " + friend.getPoints());
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendNameTextView, friendPointsTextView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendNameTextView);
            friendPointsTextView = itemView.findViewById(R.id.friendPointsTextView);
        }
    }
}
