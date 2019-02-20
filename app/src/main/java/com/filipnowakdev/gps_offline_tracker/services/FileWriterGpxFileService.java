package com.filipnowakdev.gps_offline_tracker.services;

import android.content.Context;
import android.location.Location;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileWriterGpxFileService implements IGpxFileService
{

    private final static String TEMP_FILE_NAME = "com-filipnowakdev-gps_offline_tracker-tempFile.gpx";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private static final String GPX_HEADER =
            "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                    "     creator=\"anonymous\" version=\"1.1\"\n" +
                    "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                    "    <trk>\n" +
                    "        <trkseg>\n";

    private static final String GPX_FOOTER =
            "        </trkseg>\n" +
                    "    </trk>\n" +
                    "</gpx>";
    private static final String TRACKS_TEMP_DIR = "tracks/temp/";
    private static final String TRACKS_RECORDINGS_DIR = "tracks/recordings/";


    private Context context;
    private File tempFile;
    private BufferedWriter tempWriter;

    public FileWriterGpxFileService(Context context)
    {
        this.context = context;
        initTracksDirectory();

    }

    private void initTracksDirectory()
    {
        File tempDir = new File(context.getExternalFilesDir(null), TRACKS_TEMP_DIR);
        File tracksDir = new File(context.getExternalFilesDir(null), TRACKS_RECORDINGS_DIR);

        try
        {
            if (!tempDir.exists())
                if (!tempDir.mkdirs())
                    throw new IOException();

            if (!tracksDir.exists())
                if (!tracksDir.mkdirs())
                    throw new IOException();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void createNewTrack()
    {
        initTempFile();
    }

    private void initTempFile()
    {
        try
        {
            tempFile = new File(context.getExternalFilesDir(null), TRACKS_TEMP_DIR + TEMP_FILE_NAME);
            tempWriter = new BufferedWriter(new FileWriter(tempFile));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        writeTempHeaders();
    }

    private void reinitTempFile()
    {
        try
        {
            tempFile = new File(context.getExternalFilesDir(null), TRACKS_TEMP_DIR + TEMP_FILE_NAME);
            tempWriter = new BufferedWriter(new FileWriter(tempFile, true));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeTempHeaders()
    {
        try
        {
            tempWriter.write(XML_HEADER);
            tempWriter.write(GPX_HEADER);
            tempWriter.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void addNewTrackpoint(Location location)
    {
        try
        {
            reinitTempFile();
            tempWriter.write(
                    "            <trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">\n" +
                            "                <ele>" + location.getAltitude() + "</ele>\n" +
                            "                <time>" + convertTime(location.getTime()) + "</time>\n" +
                            "            </trkpt>\n"
            );
            tempWriter.flush();
            tempWriter.close();
            System.out.println("NEW TRKPT");

        } catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("WRONG TRKPT NOT ADDED");

        }
    }

    @Override
    public void saveTrackAsFile(String filename)
    {
        try
        {
            reinitTempFile();
            tempWriter.write(GPX_FOOTER);
            tempWriter.flush();
            tempWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (tempFile.renameTo(new File(context.getExternalFilesDir(null), TRACKS_RECORDINGS_DIR + filename + ".gpx")))
            System.out.println("RENAMED");
        else
            System.out.println("NOT RENAMED");
        tempFile = null;
    }

    @Override
    public List<File> getListOfFiles()
    {
        File folder = new File(context.getExternalFilesDir(null), TRACKS_RECORDINGS_DIR);
        if (folder != null)
            return Arrays.asList(folder.listFiles());
        else
            return new ArrayList<>();
    }

    private String convertTime(long time)
    {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return format.format(date);
    }
}
