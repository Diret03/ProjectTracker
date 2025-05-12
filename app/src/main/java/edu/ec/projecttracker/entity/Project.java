package edu.ec.projecttracker.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String description;
    @ColumnInfo(name = "start_date")
    public  String startDate;
    @ColumnInfo(name = "end_date")
    public  String endDate;

    @ColumnInfo(name = "user_id")
    public int userId;
}
