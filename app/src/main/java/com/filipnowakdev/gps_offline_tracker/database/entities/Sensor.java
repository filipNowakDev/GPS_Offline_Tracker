package com.filipnowakdev.gps_offline_tracker.database.entities;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Sensor
{
    @PrimaryKey(autoGenerate = true)

    public long id;
    public String address;
    public String name;
    public boolean isDefault;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return address.equals(sensor.address) &&
                name.equals(sensor.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, name);
    }
}
