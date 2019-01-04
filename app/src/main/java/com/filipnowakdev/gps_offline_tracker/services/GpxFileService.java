package com.filipnowakdev.gps_offline_tracker.services;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.widget.Toast;
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
import java.util.Date;
import java.util.Locale;

public class GpxFileService
{
	private  Context context;
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder documentBuilder;
	private Document currentDocument;
	private Element gpxRootElement;
	private Element trkElement;
	private Element trksegElement;

	public GpxFileService(Context context)
	{
		this.context = context;
		init();
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

	public void createNewDocument()
	{
		currentDocument = documentBuilder.newDocument();
		gpxRootElement = currentDocument.createElement("gpx");
		gpxRootElement.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		gpxRootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		gpxRootElement.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
		gpxRootElement.setAttribute("version", "1.1");
		gpxRootElement.setAttribute("creator", "anonymous");

		currentDocument.appendChild(gpxRootElement);

		trkElement = currentDocument.createElement("trk");
		gpxRootElement.appendChild(trkElement);
		trksegElement = currentDocument.createElement("trkseg");
		trkElement.appendChild(trksegElement);
	}

	public void addNewTrackpoint(Location location)
	{
		if (currentDocument != null)
		{
			Element trkptElement = currentDocument.createElement("trkpt");
			trkptElement.setAttribute("lat", String.valueOf(location.getLatitude()));
			trkptElement.setAttribute("lon", String.valueOf(location.getLongitude()));

			Element eleElement = currentDocument.createElement("ele");
			eleElement.appendChild(currentDocument.createTextNode(String.valueOf(location.getAltitude())));

			Element timeElement = currentDocument.createElement("time");
			timeElement.appendChild(currentDocument.createTextNode(convertTime(location.getTime())));


			trkptElement.appendChild(eleElement);
			trkptElement.appendChild(timeElement);

			trksegElement.appendChild(trkptElement);
		}
	}


	public void saveDocumentAsFile(String filename)
	{
		try
		{
			File file = new File(context.getExternalFilesDir(null), filename + ".gpx");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(currentDocument);
			StreamResult result = new StreamResult(file);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
			currentDocument = null;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public File[] getListOfFiles()
	{
		File folder = context.getExternalFilesDir(null);
		if(folder != null)
			return folder.listFiles();
		else
			return null;
	}

	private String convertTime(long time)
	{
		Date date = new Date(time);
		Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		return format.format(date);
	}



}
