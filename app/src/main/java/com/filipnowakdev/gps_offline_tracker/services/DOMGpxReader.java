package com.filipnowakdev.gps_offline_tracker.services;

import android.content.Context;
import android.location.Location;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMGpxReader implements IGpxFileReader
{

    private Context context;

    public DOMGpxReader(Context context)
    {
        this.context = context;
    }

    @Override
    public List<GeoPoint> getGeoPointsList(String filename)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        LinkedList<GeoPoint> trackpointList = new LinkedList<>();
        Document document = null;
        try
        {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(context.getExternalFilesDir(null),
                    FileWriterGpxFileService.TRACKS_RECORDINGS_DIR + filename));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (document != null)
        {
            NodeList trkptList = document.getElementsByTagName("trkpt");
            for (int i = 0; i < trkptList.getLength(); i++)
            {
                Element trkpt = (Element) trkptList.item(i);
                double latitude = Double.parseDouble(trkpt.getAttribute("lat"));
                double longitude = Double.parseDouble(trkpt.getAttribute("lon"));
                double elevation = Double.parseDouble(trkpt.getElementsByTagName("ele").item(0).getFirstChild().getNodeValue());
                GeoPoint location = new GeoPoint(latitude, longitude, elevation);
                trackpointList.addLast(location);
            }
            return trackpointList;
        }
        return null;
    }



    public List<Location> getLocationList(String filename)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        LinkedList<Location> trackpointList = new LinkedList<>();
        Document document = null;
        try
        {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(context.getExternalFilesDir(null),
                    FileWriterGpxFileService.TRACKS_RECORDINGS_DIR + filename));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (document != null)
        {
            NodeList trkptList = document.getElementsByTagName("trkpt");
            for (int i = 0; i < trkptList.getLength(); i++)
            {
                Element trkpt = (Element) trkptList.item(i);
                double latitude = Double.parseDouble(trkpt.getAttribute("lat"));
                double longitude = Double.parseDouble(trkpt.getAttribute("lon"));
                double elevation = Double.parseDouble(trkpt.getElementsByTagName("ele").item(0).getFirstChild().getNodeValue());
                Date date = parseTime(trkpt.getElementsByTagName("time").item(0).getFirstChild().getNodeValue());
                Location location = new Location("gpx");
                location.setAltitude(elevation);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setTime(date.getTime());
                trackpointList.addLast(location);
            }
            return trackpointList;
        }
        return null;

    }

    private Date parseTime(String timeString)
    {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Date date = null;
        try
        {
            date = format.parse(timeString);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
}
