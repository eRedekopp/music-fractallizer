import javax.sound.sampled.*;

public class AudioController {

    /**
     * Is the audio currently supposed to be playing?
     */
    private volatile boolean audioPlaying;

    /**
     * The format to be used for audio output
     */
    private AudioFormat audioFormat;

    /**
     * The data line to which metronome data is written
     */
    private SourceDataLine dataLine;

    /**
     * Audio sample rate in Hz
     */
    private static final int SAMPLE_RATE_HZ = 44100;

    /**
     * The size of the audio buffer used
     */
    private static final int BUFFER_SIZE_BYTES = 4096;

    public AudioController() {
        this.audioPlaying = false;
        try {
            this.audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE_HZ,
                    16,
                    1,
                    2,
                    SAMPLE_RATE_HZ,
                    true
            );
            dataLine = AudioSystem.getSourceDataLine(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void startAudioLoop(Melody melody) {
        if (audioPlaying)
            return;

        audioPlaying = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (! dataLine.isOpen()) {
                    try {
                        dataLine.open(audioFormat, BUFFER_SIZE_BYTES);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                dataLine.start();
                while (audioPlaying) {
                    writeMelody(melody);
                }
                // loop complete; finish up
                dataLine.stop();
                dataLine.flush();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopAudioLoop() {
        audioPlaying = false;
    }

    private void writeMelody(Melody melody) {
        byte[] toWrite = new byte[BUFFER_SIZE_BYTES];
        int bufIdx = 0;
        for (Note note : melody.getNotes()) {
            double period = SAMPLE_RATE_HZ / note.getFreq();
            for (int i = 0; i < SAMPLE_RATE_HZ * note.getDur(); i++, bufIdx += 2) {
                if (! audioPlaying)
                    break;
                if (bufIdx >= BUFFER_SIZE_BYTES - 1) {
                    dataLine.write(toWrite, 0, bufIdx);
                    bufIdx = 0;
                }
                double angle = 2 * i * Math.PI / (period);
                short a = (short) (Math.sin(angle) / (2 * Math.PI) * Short.MAX_VALUE);
                // write to buffer as bytes
                toWrite[bufIdx] = (byte) (a >> 8);
                toWrite[bufIdx + 1] = (byte) (a & 0xFF);
            }
        }
    }

}
