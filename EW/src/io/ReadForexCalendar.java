package io;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * TODO Skriv ett script som omvandlar html-filen till en bra xml-fil
 * 
 * @author tobbew92
 * 
 */
public class ReadForexCalendar {

	private final String fileName;
	private final File file;

	public ReadForexCalendar(String fileName) {

		file = new File(fileName);
		this.fileName = fileName;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// Parse the downloaded file with xpath
	public ArrayList<StringBuilder> parseWithXPath(String xPath) {

		//Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Document doc = null;
		try {
			doc = builder.parse(fileName);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = null;
		try {
			expr = xpath.compile(xPath);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NodeList nl = null;
		try {
			// evaluation = (String) expr.evaluate(doc, XPathConstants.STRING);
			nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Iterate through the XML tree
		// TODO Kom ih?g att tiden den visar ?r 2h bak.
		ArrayList<StringBuilder> evaluation = new ArrayList<StringBuilder>();

		for (int i = 0; i < nl.getLength(); i++) {
			Node currentNode = nl.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which is Element
				// System.out.println(currentNode.getFirstChild().getNodeValue());
				NodeList nl2 = currentNode.getChildNodes();
				StringBuilder sb = new StringBuilder();
				evaluation.add(sb);

				for (int j = 0; j < nl2.getLength(); j++) {
					Node currentChildNode = nl2.item(j);
					if (currentChildNode.getFirstChild() == null)
						continue;
					if (currentChildNode.getNodeType() == Node.ELEMENT_NODE) {
						sb.append(currentChildNode.getFirstChild()
								.getNodeValue() + " | ");
					}
				}
				// TODO ska bort s?klart
				// System.out.println("--------------NEW EVENT--------------");
				// System.out.println(evaluation.get(i));
			}
		}
		return evaluation;

	}

	public void downloadFile(String url) {

		// Get information from website url
		URL website;
		try {
			website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			fos.close();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return file;
	}

}