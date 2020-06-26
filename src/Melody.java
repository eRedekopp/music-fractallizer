import java.util.List;

/**
 * Represents a melody, ie. a sequence of Notes
 */
public class Melody {

    /**
     * The notes in this melody
     */
    private final List<Note> notes;

    public Melody(List<Note> notes) {
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        if (notes.size() == 0) {
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
