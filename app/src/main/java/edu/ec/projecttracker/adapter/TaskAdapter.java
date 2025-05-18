package edu.ec.projecttracker.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.entity.Task;
import edu.ec.projecttracker.utils.TaskStatus;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> tasks;
    private OnTaskClickListener listener;
    private ExecutorService executor;
    private Map<Integer, Project> projectsMap = new HashMap<>();

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> tasks, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
        // Only preload if there are tasks
        if (!tasks.isEmpty()) {
            preloadProjects();
        }
    }

    private void preloadProjects() {
        Set<Integer> projectIds = new HashSet<>();
        for (Task task : tasks) {
            projectIds.add(task.projectId);
        }

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                for (Integer projectId : projectIds) {
                    Project project = db.projectDao().findById(projectId);
                    if (project != null) {
                        projectsMap.put(projectId, project);
                    }
                }
                // Notify adapter that data is ready
                new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTaskName.setText(task.name);
        holder.tvTaskDescription.setText(task.description);
        holder.tvTaskDates.setText("Desde: " + task.startDate + " - Hasta: " + task.endDate);

        // Set task status with appropriate color
        holder.tvStatus.setText(task.status);
        setStatusColor(holder.tvStatus, task.status);

        // Get project name from preloaded map
        Project project = projectsMap.get(task.projectId);
        if (project != null) {
            holder.tvProjectName.setText("Proyecto: " + project.name);
        } else {
            holder.tvProjectName.setText("Proyecto: Cargando...");
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    private void setStatusColor(TextView tvStatus, String status) {
        int color;
        switch (status) {
            case TaskStatus.PLANNED:
                color = R.color.status_planned;
                break;
            case TaskStatus.IN_PROGRESS:
                color = R.color.status_in_progress;
                break;
            case TaskStatus.COMPLETED:
                color = R.color.status_completed;
                break;
            default:
                color = android.R.color.darker_gray;
                break;
        }
        tvStatus.setTextColor(ContextCompat.getColor(context, color));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvProjectName;
        TextView tvTaskDescription;
        TextView tvTaskDates;
        TextView tvStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTaskDates = itemView.findViewById(R.id.tvTaskDates);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (executor != null) {
            executor.shutdown();
        }
    }
}