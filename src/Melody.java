import java.util.List;

/**
 * Represents a melody, ie. a sequence of Notes
 */
public class Melody {

    /**
     * The notes in this melody
     */
    private final Note[] notes;

    public Melody(List<Note> notes) {
        this.notes = (Note[]) notes.toArray();
    }

    public Note[] getNotes() {
        return notes;
    }

    public double getNumBeats() {
        double count = 0;
        for (Note note : notes)
            count += note.getDur();
        return count;
    }

    @Override
    public String toString() {
        if (notes.length == 0) {
            return "Empty Melody";
        }
        StringBuilder builder = new StringBuilder();
        for (Note note : notes) {
            builder.append(note.toString());
            builder.append('\n');
        }
        return builder.toString().substring(0, builder.length()-1);
    }
}
