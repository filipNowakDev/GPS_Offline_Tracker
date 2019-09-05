package com.filipnowakdev.gps_offline_tracker.track_utils;

import android.location.Location;

import com.filipnowakdev.gps_offline_tracker.database.db.TrackDatabase;
import com.filipnowakdev.gps_offline_tracker.database.entities.Track;
import com.filipnowakdev.gps_offline_tracker.database.entities.Trackpoint;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrackRecordingService
{
    private TrackDatabase db;
    private Track recordedTrack;
    private long currentSequenceNumber;


    public TrackRecordingService(TrackDatabase db)
    {
        this.db = db;
    }

    public void createNewTrack()
    {
        recordedTrack = new Track();
        recordedTrack.creationDate = Calendar.getInstance().getTimeInMillis();
        recordedTrack.creator = "anonymous";
        recordedTrack.name = "temp";
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
        System.out.println("NEW TRACK: " + recordedTrack.id);
    }

    public void stopRecording(String name)
    {
        if (recordedTrack != null)
        {
            recordedTrack.name = name;

            Executors.newSingleThreadExecutor().execute(() -> db.trackDao().update(recordedTrack));
            //recordedTrack = null;
        }
    }

    public void addTrackpoint(Location location, int bpm)
    {
        System.out.println("TERAZ BEDZIE KURUA TRACK ID: " + recordedTrack.id);
        if (recordedTrack != null && recordedTrack.id != 0)
        {
            System.out.println("TERAZ BEDZIE WSTAWIONE W TRACK ID: " + recordedTrack.id);
            Trackpoint trackpoint = new Trackpoint();
            trackpoint.trackId = recordedTrack.id;
            trackpoint.latitude = location.getLatitude();
            trackpoint.longitude = location.getLongitude();
            trackpoint.elevation = location.getAltitude();
            trackpoint.trackId = recordedTrack.id;
            trackpoint.bpm = bpm;
            trackpoint.sequenceNumber = currentSequenceNumber++;
            trackpoint.time = location.getTime();
            Executors.newSingleThreadExecutor().execute(() -> db.trackpointDao().insert(trackpoint));
        }
    }
}
