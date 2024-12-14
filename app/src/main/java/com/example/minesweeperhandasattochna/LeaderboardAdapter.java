package com.example.minesweeperhandasattochna;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LeaderboardAdapter extends BaseAdapter {

    private final Context context;
    private final List<LeaderboardActivity.LeaderboardEntry> leaderboardEntries;

    public LeaderboardAdapter(Context context, List<LeaderboardActivity.LeaderboardEntry> leaderboardEntries) {
        this.context = context;
        this.leaderboardEntries = leaderboardEntries;
    }

    @Override
    public int getCount() {
        return leaderboardEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return leaderboardEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        }

        TextView rankText = convertView.findViewById(R.id.rankText);
        TextView displayNameText = convertView.findViewById(R.id.displayNameText);
        TextView timeText = convertView.findViewById(R.id.timeText);

        LeaderboardActivity.LeaderboardEntry entry = leaderboardEntries.get(position);

        // Set rank, display name, and time
        rankText.setText(String.valueOf(position + 1));
        displayNameText.setText(entry.getDisplayName());
        timeText.setText(entry.getTime() + "s");

        // Apply colors for the top 3 ranks
        switch (position) {
            case 0:
                rankText.setTextColor(Color.parseColor("#FFD700")); // Gold
                break;
            case 1:
                rankText.setTextColor(Color.parseColor("#C0C0C0")); // Silver
                break;
            case 2:
                rankText.setTextColor(Color.parseColor("#CD7F32")); // Bronze
                break;
            default:
                rankText.setTextColor(Color.WHITE); // Regular
                break;
        }

        displayNameText.setTextColor(Color.LTGRAY);
        timeText.setTextColor(Color.WHITE);

        return convertView;
    }
}
