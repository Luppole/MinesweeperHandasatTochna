package com.example.minesweeperhandasattochna;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {

    private final HashMap<String, Achievement> achievements;
    private final HashMap<String, Integer> userProgress;

    public AchievementsAdapter(HashMap<String, Achievement> achievements, HashMap<String, Integer> userProgress) {
        this.achievements = achievements;
        this.userProgress = userProgress;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArrayList<String> keys = new ArrayList<>(achievements.keySet());
        String key = keys.get(position);

        Achievement achievement = achievements.get(key);
        int progress = userProgress.getOrDefault(key, 0);

        holder.titleText.setText(achievement.getTitle());
        holder.descriptionText.setText(achievement.getDescription());
        holder.progressBar.setMax(achievement.getGoal());
        holder.progressBar.setProgress(progress);
        holder.progressText.setText(progress + "/" + achievement.getGoal());
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, progressText;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.achievementTitle);
            descriptionText = itemView.findViewById(R.id.achievementDescription);
            progressText = itemView.findViewById(R.id.achievementProgressText);
            progressBar = itemView.findViewById(R.id.achievementProgressBar);
        }
    }
}
