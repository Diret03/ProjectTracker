package edu.ec.projecttracker.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public  int id;
    public int projectId;
    public  String name;
    public  String description;
    @ColumnInfo(name = "start_date")
    public  String startDate;
    @ColumnInfo(name = "end_date")
    public  String endDate;
}
