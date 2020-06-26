/**
 * Represents a single note or rest. This Object is immutable; all fields are final
 */
public class Note {

    /**
     * The pitch of this note in Hz
     */
    private final double freq;

    /**
     * The duration of this note as a number of quarter notes at 60BPM (ie. seconds)
     */
    private final double dur;

    public Note(double freq, double dur) {
        this.freq = freq;
        this.dur = dur;
    }

    public double getFreq() {
        return freq;
    }

    public double getDur() {
        return dur;
    }

    public boolean isRest() {
        return freq == 0;
    }

    /**
     * Return a new Note with the same pitch but the duration adjusted by the given proportion
     *
     * @param p The proportion by which to change the duration of the pitch
     * @return A new Note with the different duration
     */
    public Note adjustDuration(int p) {
        return new Note(freq, dur * p);
    }

    @Override
    public String toString() {
        return "Duration: " + dur + " Frequency: " + freq;
    }
}
