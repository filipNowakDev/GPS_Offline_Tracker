package com.filipnowakdev.gps_offline_tracker.services;

import android.content.Context;
import android.location.Location;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class GpxFileService implements IGpxFileService
{
	private Context context;
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder documentBuilder;
	private Document currentDocument;
	private Element gpx;
	private Element trk;
	private Element trkseg;

	public GpxFileService(Context context)
	{
		this.context = context;
		//init();
	}

	private void init()
	{
		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void createNewTrack()
	{
		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//DEBUG
		System.out.println("CREATE DOC");

		currentDocument = documentBuilder.newDocument();
		gpx = currentDocument.createElement("gpx");
		gpx.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		gpx.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		gpx.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
		gpx.setAttribute("version", "1.1");
		gpx.setAttribute("creator", "anonymous");

		currentDocument.appendChild(gpx);

		trk = currentDocument.createElement("trk");
		gpx.appendChild(trk);
		trkseg = currentDocument.createElement("trkseg");
		trk.appendChild(trkseg);
	}

	@Override
	public void addNewTrackpoint(Location location)
	{
		if (currentDocument != null)
		{
			//DEBUG
			System.out.println("ADD TRACKPOINT");

			Element trkpt = currentDocument.createElement("trkpt");
			trkpt.setAttribute("lat", String.valueOf(location.getLatitude()));
			trkpt.setAttribute("lon", String.valueOf(location.getLongitude()));

			Element ele = currentDocument.createElement("ele");
			ele.appendChild(currentDocument.createTextNode(String.valueOf(location.getAltitude())));

			Element time = currentDocument.createElement("time");
			time.appendChild(currentDocument.createTextNode(convertTime(location.getTime())));


			trkpt.appendChild(ele);
			trkpt.appendChild(time);

			trkseg.appendChild(trkpt);
		}
	}


	@Override
	public void saveTrackAsFile(String filename)
	{
		try
		{
			//DEBUG
			System.out.println("SAVE FILE");

			File file = new File(context.getExternalFilesDir(null), filename + ".gpx");

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(currentDocument);


			// Output to console for testing
			StreamResult testResult = new StreamResult(System.out);
			transformer.transform(source, testResult);


			//Final Output
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

			currentDocument = null;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public List<File> getListOfFiles()
	{
		File folder = context.getExternalFilesDir(null);
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
