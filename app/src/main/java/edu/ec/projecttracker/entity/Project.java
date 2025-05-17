package edu.ec.projecttracker.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "projects",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = CASCADE
        ),
        indices = {@Index("user_id")}
)
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
