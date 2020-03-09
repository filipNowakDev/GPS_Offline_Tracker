package com.filipnowakdev.gps_offline_tracker.database.entities;


import android.location.Location;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Track.class,
        parentColumns = "id",
        childColumns = "trackId",
        onDelete = CASCADE))
public class Trackpoint
{
    @PrimaryKey(autoGenerate = true)
    public long id;

    public double latitude;
    public double longitude;
    public double elevation;
    public long time;
    public int bpm;
    public long trackId;
    public long sequenceNumber;

    public double speed;
    public double distanceFromStart;
    public long timeFromStart;

    public Trackpoint(Track recordedTrack, Location location, int bpm, long sequenceNumber)
    {
        trackId = recordedTrack.id;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        elevation = location.getAltitude();
        speed = location.getSpeed();
        this.bpm = bpm;
        this.sequenceNumber = sequenceNumber;
        time = location.getTime();
    }

    public Trackpoint()
    {
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public double distanceTo(Trackpoint trackpoint)
    {
        Location location1 = new Location("db");
        location1.setLatitude(this.latitude);
        location1.setLongitude(this.longitude);

        Location location2 = new Location("db");
        location2.setLatitude(trackpoint.latitude);
        location2.setLongitude(trackpoint.longitude);

        return location1.distanceTo(location2);
    }

}
