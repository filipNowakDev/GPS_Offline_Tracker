package com.filipnowakdev.gps_offline_tracker.database.entities;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Track
{
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public String creator;

    public long creationDate;

    public double avgSpeed;

    public double maxSpeed;

    public double distance;

    public long duration;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return creationDate == track.creationDate &&
                Objects.equals(name, track.name) &&
                Objects.equals(creator, track.creator);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, creator, creationDate);
    }
}
