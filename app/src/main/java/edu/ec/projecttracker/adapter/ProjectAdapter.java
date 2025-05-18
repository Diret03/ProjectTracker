package edu.ec.projecttracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.utils.ProjectProgress;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private Context context;
    private List<Project> projects;
    private OnProjectClickListener listener;
    private AppDatabase db;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(Context context, List<Project> projects, OnProjectClickListener listener) {
        this.context = context;
        this.projects = projects;
        this.listener = listener;
        this.db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.tvProjectName.setText(project.name);
        holder.tvProjectDescription.setText(project.description);
        holder.tvProjectDates.setText("Desde: " + project.startDate + " - Hasta: " + project.endDate);

        // Use the stored progress value
        holder.progressBar.setProgress(project.progress);
        holder.tvProgressPercent.setText(project.progress + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName;
        TextView tvProjectDescription;
        TextView tvProjectDates;
        ProgressBar progressBar;
        TextView tvProgressPercent;

        ProjectViewHolder(View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvProjectDates = itemView.findViewById(R.id.tvProjectDates);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
        }
    }
}