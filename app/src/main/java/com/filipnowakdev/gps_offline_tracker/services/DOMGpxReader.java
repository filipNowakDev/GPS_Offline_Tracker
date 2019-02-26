package com.filipnowakdev.gps_offline_tracker.services;

import android.content.Context;

import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
    public List<GeoPoint> getListOfTrackpoints(String filename)
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
                double elevation = 0;//Double.parseDouble(trkpt.getElementsByTagName("ele").item(0).getNodeValue());
                GeoPoint location = new GeoPoint(latitude, longitude, elevation);
                trackpointList.addLast(location);
            }
            return trackpointList;
        }
        return null;
    }
}
