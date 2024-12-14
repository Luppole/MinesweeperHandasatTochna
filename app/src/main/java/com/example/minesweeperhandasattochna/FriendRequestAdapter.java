package com.example.minesweeperhandasattochna;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    public interface FriendRequestActionsListener {
        void onAccept(String requesterEmail);
        void onReject(String requesterEmail);
    }

    private final Context context;
    private final ArrayList<String> friendRequests;
    private final FriendRequestActionsListener actionsListener;

    public FriendRequestAdapter(Context context, ArrayList<String> friendRequests, FriendRequestActionsListener actionsListener) {
        this.context = context;
        this.friendRequests = friendRequests;
        this.actionsListener = actionsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String requesterEmail = friendRequests.get(position);
        holder.requesterEmailTextView.setText(requesterEmail);

        holder.acceptButton.setOnClickListener(v -> actionsListener.onAccept(requesterEmail));
        holder.rejectButton.setOnClickListener(v -> actionsListener.onReject(requesterEmail));
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requesterEmailTextView;
        Button acceptButton, rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterEmailTextView = itemView.findViewById(R.id.requesterEmail);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}
