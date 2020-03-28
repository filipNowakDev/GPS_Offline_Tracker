package com.filipnowakdev.gps_offline_tracker.track_utils;

import android.location.Location;

import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrackRecordingManager
{
    private TrackDatabase db;
    private Track recordedTrack;
    private long currentSequenceNumber;
    private boolean hasSpeedFromGps;
    public static final String TEMP_TRACK_NAME = "com.filipnowakdev.__temp";


    public TrackRecordingManager(TrackDatabase db)
    {
        this.db = db;
        hasSpeedFromGps = false;
    }

    public void createNewTrack()
    {
        recordedTrack = new Track();
        recordedTrack.creationDate = Calendar.getInstance().getTimeInMillis();
        recordedTrack.creator = "anonymous";
        recordedTrack.name = TEMP_TRACK_NAME;
        currentSequenceNumber = 0;
        Callable<Long> getCallable = () -> db.trackDao().insert(recordedTrack);
        Long id = null;
        Future<Long> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            id = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        if (id != null)
            recordedTrack.id = id;
        System.out.println("[DEBUG] NEW TRACK: " + recordedTrack.id);
    }

    public void stopRecording(String name)
    {
        if (recordedTrack != null)
        {
            List<Trackpoint> trackpoints = getTrackpoints();

            recordedTrack.name = name;

            if (!hasSpeedFromGps)
                setTrackpointsSpeed(trackpoints);

            setTrackpointsData(trackpoints);
            saveUpdatedTrack(trackpoints);
        }
    }

    private void saveUpdatedTrack(List<Trackpoint> trackpoints)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            db.trackDao().update(recordedTrack);
            db.trackpointDao().updateAll(trackpoints);
        });
    }

    @Nullable
    private List<Trackpoint> getTrackpoints()
    {
        List<Trackpoint> trackpoints = null;

        Callable<List<Trackpoint>> getCallable = () -> db.trackpointDao().getByTrackIdPOJO(recordedTrack.id);
        Future<List<Trackpoint>> future = Executors.newSingleThreadExecutor().submit(getCallable);
        try
        {
            trackpoints = future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        return trackpoints;
    }

    private void setTrackpointsSpeed(List<Trackpoint> trackpoints)
    {
        if (trackpoints == null)
            return;
        if (trackpoints.size() >= 2)
            for (int i = 0; i < trackpoints.size() - 1; i++)
            {
                double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
                double timeDiff = (trackpoints.get(i + 1).time - trackpoints.get(i).time) / 1000.0;
                trackpoints.get(i).speed = distanceDiff / timeDiff;
            }
        else
            for (Trackpoint trackpoint : trackpoints)
                trackpoint.speed = 0.0;
    }

    private void setTrackpointsData(List<Trackpoint> trackpoints)
    {

        if (trackpoints == null)
            return;

        double distance = 0;
        double maxSpeed = 0;
        long time = 0;
        if (trackpoints.size() >= 2)
        {
            for (int i = 0; i < trackpoints.size() - 1; i++)
            {
                trackpoints.get(i).distanceFromStart = distance;
                trackpoints.get(i).timeFromStart = time;

                double distanceDiff = trackpoints.get(i).distanceTo(trackpoints.get(i + 1));
                distance += distanceDiff;

                long timeDiff = trackpoints.get(i + 1).time - trackpoints.get(i).time;
                time += timeDiff;

                if (trackpoints.get(i).speed > maxSpeed && trackpoints.get(i).speed != Double.POSITIVE_INFINITY)
                    maxSpeed = trackpoints.get(i).speed;
            }
            recordedTrack.duration = trackpoints.get(trackpoints.size() - 1).time - trackpoints.get(0).time;
            double allSecondsDuration = recordedTrack.duration / 1000.0;

            recordedTrack.avgSpeed = distance / allSecondsDuration;
            recordedTrack.maxSpeed = maxSpeed;
            recordedTrack.distance = distance;
        } else
        {
            recordedTrack.maxSpeed = 0;
            recordedTrack.distance = 0;
            recordedTrack.avgSpeed = 0;
        }
    }

    public void addTrackpoint(Location location, int bpm)
    {
        if (recordedTrack != null && recordedTrack.id != 0)
        {
            Trackpoint trackpoint = new Trackpoint(recordedTrack, location, bpm, currentSequenceNumber++);
            if (!hasSpeedFromGps && location.hasSpeed())
                hasSpeedFromGps = true;
            Executors.newSingleThreadExecutor().execute(() -> db.trackpointDao().insert(trackpoint));
        } else
            System.out.println("[ERROR] We lost the track reference somehow");
    }
}
