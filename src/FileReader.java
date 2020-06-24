import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Read a MusicXML file and get the notes from it
 */
public class FileReader {

    /**
     * Indexes into the note:frequency table
     */
    private static final int C  = 0;
    private static final int CS = 1;
    private static final int DB = CS;
    private static final int D  = 2;
    private static final int DS = 3;
    private static final int EB = DS;
    private static final int E  = 4;
    private static final int FB = E;
    private static final int F  = 5;
    private static final int ES = F;
    private static final int FS = 6;
    private static final int GB = FS;
    private static final int G  = 7;
    private static final int GS = 8;
    private static final int AB = GS;
    private static final int A  = 9;
    private static final int AS = 10;
    private static final int BB = AS;
    private static final int B = 11;
    private static final int BS = C;
    private static final int CB = B;

    /**
     * Frequencies in Hz for 12-TET for the first octave starting from C chromatically upward to B.
     */
    private static final double[] freqs = {
            32.70320,
            34.64783,
            36.70810,
            38.89017,
            41.20344,
            43.65353,
            46.24930,
            48.99943,
            51.91309,
            55.00000,
            58.27047,
            61.73541
    };


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
        NodeList parts = root.getElementsByTagName("part");
        if (parts.getLength() == 0)
            throw new ScoreFormatException("No parts found (partslist empty)");
        for (int i = 0; i < parts.getLength(); i++) {
            if (parts.item(i).getNodeType() == Node.ELEMENT_NODE) {
                part = (Element) parts.item(i);
                break;
            }
        }
        if (part == null)
            throw new ScoreFormatException("No parts found (no element nodes in partslist)");

        // Find all the measures in the melody
        NodeList measures = part.getElementsByTagName("measure");
        ArrayList<Note> outputNotes = new ArrayList<>();
        for (int i = 0; i < measures.getLength(); i++) {
            Node node = measures.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Find all the notes in the measure
                Element measure = (Element) node;
                NodeList notes = measure.getElementsByTagName("note");
                for (int j = 0; j < notes.getLength(); j++) {
                    Node noteNode = notes.item(j);
                    if (noteNode.getNodeType() == Node.ELEMENT_NODE) {
                        // Add the note or rest
                        Element note = (Element) noteNode;
                        NodeList rest = note.getElementsByTagName("rest");
                        double freq;
                        if (rest == null || rest.getLength() == 0) {  // note
                            String step = note.getElementsByTagName("step").item(0).getTextContent();
                            int octave = Integer.parseInt(note.getElementsByTagName("octave").item(0).getTextContent());
                            freq = musicNoteToFreq(step, octave);
                        } else {  // rest
                            freq = 0;
                        }
                        int beats = Integer.parseInt(note.getElementsByTagName("duration").item(0).getTextContent());
                        String noteType = note.getElementsByTagName("type").item(0).getTextContent();
                        double duration = getDurationFromTypeAndBeats(beats, noteType);
                        outputNotes.add(new Note(freq, duration));
                    }
                }
            }
        }
        return new Melody(outputNotes);
    }

    private static double getDurationFromTypeAndBeats(int beats, String type) {
        // TODO figure out the conventions for naming note types and parse the string to figure out if/how much we
        //  need to change the note's duration from the 1/4 note value
        return -1;
    }

    private static double musicNoteToFreq(String note, int octave) {
        return freqs[noteNameToArrayIndex(note)] * octave;
    }

    private static int noteNameToArrayIndex(String note) {
        // TODO figure out the conventions for naming notes and parse the string to one of the constants above
        return -1;
    }

    public static class ScoreFormatException extends RuntimeException {
        public ScoreFormatException(String reason) {
            super(reason);
        }
    }
}
