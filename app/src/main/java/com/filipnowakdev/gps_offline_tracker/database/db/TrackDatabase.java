package com.filipnowakdev.gps_offline_tracker.database.db;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.filipnowakdev.gps_offline_tracker.database.daos.SensorDao;
import com.filipnowakdev.gps_offline_tracker.database.daos.TrackDao;
import com.filipnowakdev.gps_offline_tracker.database.daos.TrackpointDao;
import com.filipnowakdev.gps_offline_tracker.database.entities.Sensor;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

@Database(entities = {Track.class, Trackpoint.class, Sensor.class}, version = 9)
public abstract class TrackDatabase extends RoomDatabase
{
    private static final String DB_NAME = "track-database.db";


    private static final Migration MIGRATION_5_6 = new Migration(5, 6)
    {
        @Override
        public void migrate(SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE trackpoint "
                    + " ADD COLUMN speed REAL DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE trackpoint "
                    + " ADD COLUMN distanceFromStart REAL DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE trackpoint "
                    + " ADD COLUMN timeFromStart INTEGER DEFAULT 0 NOT NULL");

            database.execSQL("ALTER TABLE track "
                    + " ADD COLUMN distance REAL DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE track "
                    + " ADD COLUMN avgSpeed REAL DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE track "
                    + " ADD COLUMN maxSpeed REAL DEFAULT 0 NOT NULL");

        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7)
    {
        @Override
        public void migrate(SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE track "
                    + " ADD COLUMN duration INTEGER DEFAULT 0 NOT NULL");

        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("CREATE TABLE `sensor` " +
                    "(`id` INTEGER, `address` TEXT, PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE sensor " +
                    "ADD COLUMN name TEXT DEFAULT \"\" NOT NULL");
            database.execSQL("ALTER TABLE sensor " +
                    "ADD COLUMN isDefault INTEGER DEFAULT 0 NOT NULL");
        }
    };

    public abstract TrackDao trackDao();
    public abstract TrackpointDao trackpointDao();
    public abstract SensorDao sensorDao();

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
                .enableMultiInstanceInvalidation()
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build();
    }
}
