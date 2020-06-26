import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.File;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Read a MusicXML file and get the notes from it
 */
public class FileReader {

    public static Melody readXmlFile(File file) throws SAXException, IOException, ScoreFormatException {
        // Get a parsed Document object for this file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException("ParserConfigurationException");
        }
        Document doc = builder.parse(file);
        Element root = doc.getDocumentElement();
        root.normalize();

        // Find the part element with our melody
        Element part = null;
        NodeList parts = root.getElementsByTagName("Staff");
        if (parts.getLength() == 0)
            throw new ScoreFormatException("No parts found (stafflist empty)");
        for (int i = 0; i < parts.getLength(); i++) {
            part = (Element) parts.item(i);
            if (part.getNodeType() == Node.ELEMENT_NODE && hasMeasures(part)) {
                break;
            }
        }
        if (part == null)
            throw new ScoreFormatException("No parts found (no measures found)");

        // Find all the measures in the melody
        try {
            NodeList measures = part.getElementsByTagName("Measure");
            ArrayList<Note> outputNotes = new ArrayList<>();
            for (int i = 0; i < measures.getLength(); i++) {
                Node node = measures.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Find the first voice in the measure
                    Element measure = (Element) node;
                    NodeList voices = measure.getElementsByTagName("voice");
                    Element voice = (Element) voices.item(0);

                    // Find all the chords and rests in the voice
                    NodeList children = voice.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        // Note, rest, or other?
                        Node childNode = children.item(j);
                        if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element childElement = (Element) childNode;
                        String elementTag = childElement.getTagName();
                        double freq;
                        if ("rest".equals(elementTag.toLowerCase())) {
                            freq = 0;
                        } else if ("chord".equals(elementTag.toLowerCase())) {
                            Element noteElement = (Element) childElement.getElementsByTagName("Note").item(0);
                            Element pitchElement = (Element) noteElement.getElementsByTagName("pitch").item(0);
                            int pitch = Integer.parseInt(pitchElement.getTextContent());
                            freq = midiPitchToFreq(pitch);
                        } else {
                            continue;
                        }
                        String durationType = childElement.getElementsByTagName("durationType")
                                                  .item(0).getTextContent();
                        NodeList timeSigList = childElement.getElementsByTagName("duration");
                        String timeSig;
                        if (timeSigList.getLength() > 0) {
                            timeSig = timeSigList.item(0).getTextContent();
                        } else {
                            timeSig = null;
                        }
                        double duration = getDurationFromTypeAndTimeSig(durationType, timeSig);
                        outputNotes.add(new Note(freq, duration));
                    }
                }
            }
            return new Melody(outputNotes);
        } catch (NumberFormatException | DOMException | NullPointerException e) {
            e.printStackTrace();
            throw new ScoreFormatException("Error parsing score");
        }
    }

    private static double getDurationFromTypeAndTimeSig(String type, String timeSig) throws ScoreFormatException {
        switch (type) {
            case "128th":
                return 0.03125;
            case "64th":
                return 0.0625;
            case "32nd":
                return 0.125;
            case "16th":
                return 0.25;
            case "eighth":
                return 0.5;
            case "quarter":
                return 1;
            case "half":
                return 2;
            case "whole":
                // no need to think about notes longer than this because they show up in each measure
                return 4;
            case "measure":
                // measure-long rest
                String[] sigList = timeSig.split("/");
                int bottom = Integer.parseInt(sigList[1]);
                int top = Integer.parseInt(sigList[0]);
                return 4f / bottom * top;
            default:
                throw new ScoreFormatException("Invalid type found: " + type + " | timeSig = " + timeSig);
        }
    }

    /**
     * @param pitch A pitch in MIDI integer notation
     * @return The frequency of that pitch in Hz, as calculated by the MIDI tuning standard
     */
    private static double midiPitchToFreq(int pitch) {
        // https://en.wikipedia.org/wiki/MIDI_tuning_standard
        return Math.pow(2, (pitch - 69)/12f) * 440;
    }

    /**
     * @param staff An Element with tag "staff"
     * @return True if the Element has child Elements with tag "measure", or False if not
     */
    private static boolean hasMeasures(Element staff) {
        NodeList children = staff.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element child = (Element) childNode;
            if (child.getTagName().equals("Measure")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Something's wrong with the format of the score :(
     */
    public static class ScoreFormatException extends RuntimeException {
        public ScoreFormatException(String reason) {
            super(reason);
        }
    }
}
