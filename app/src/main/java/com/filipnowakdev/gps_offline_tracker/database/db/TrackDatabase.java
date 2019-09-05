package com.filipnowakdev.gps_offline_tracker.database.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.filipnowakdev.gps_offline_tracker.database.daos.TrackDao;
import com.filipnowakdev.gps_offline_tracker.database.daos.TrackpointDao;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

@Database(entities = {Track.class, Trackpoint.class}, version = 5)
public abstract class TrackDatabase extends RoomDatabase
{
    private static final String DB_NAME = "track-database.db";

    public abstract TrackDao trackDao();

    public abstract TrackpointDao trackpointDao();

    private static TrackDatabase instance;
    private static TrackDatabase locationServiceInstance;

    public static synchronized TrackDatabase getInstance(Context context)
    {
        if (instance == null)
        {
            instance = create(context);
        }
        return instance;
    }

    public static synchronized TrackDatabase getLocationServiceInstance(Context context)
    {
        if (locationServiceInstance == null)
        {
            locationServiceInstance = create(context);
        }
        return locationServiceInstance;
    }


    private static TrackDatabase create(final Context context)
    {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                TrackDatabase.class,
                DB_NAME)
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()
                .build();
    }
}
