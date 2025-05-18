package edu.ec.projecttracker.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tasks",
        foreignKeys = @ForeignKey(
                entity = Project.class,
                parentColumns = "id",
                childColumns = "project_id",
                onDelete = CASCADE
        ),
        indices = {@Index("project_id")}
)
public class Task {

    @PrimaryKey(autoGenerate = true)
    public  int id;
    @ColumnInfo(name = "project_id")
    public int projectId;
    public  String name;
    public  String description;
    @ColumnInfo(name = "start_date")
    public  String startDate;
    @ColumnInfo(name = "end_date")
    public  String endDate;

    @ColumnInfo(name = "status")
    public String status; // "Planificado", "En ejecución", "Realizado"

    public Task() {
        // Default status for new tasks
        this.status = "Planificado";
    }

    @Override
    public String toString() {
        return name;
    }
}
