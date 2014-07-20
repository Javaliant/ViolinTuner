import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ViolinTuner {
    private static enum Note {
        G(55), D(62), A(69), E(76);

        public final int midiNoteNumber;

        Note(int midiNoteNumber) {
            this.midiNoteNumber = midiNoteNumber;
        }
    }

    private static enum Instrument {
        // Orchestras tune to oboes.
        OBOE("Oboe", 68), PIANO("Piano", 0), CELESTA("Celesta", 8), GUITAR("Guitar", 24), VIOLIN(" Violin", 40);

        public final int instrumentChannel;
        public final String instrumentName;

        Instrument(String instrumentName, int instrumentChannel) {
            this.instrumentName = instrumentName;
            this.instrumentChannel = instrumentChannel;
        }
    }

    int c = 1;

    private MidiChannel channel;
    private final AbstractButton[] buttons = new AbstractButton[Note.values().length];

    public ViolinTuner() throws MidiUnavailableException {
        this.channel = initMidiChannel(Instrument.values()[0].instrumentChannel);
        

        JFrame frame = new JFrame("Violin Tuner");
        frame.setIconImage(new ImageIcon("Images/Icon.png").getImage());

        JButton instrumentChanger = new JButton(Instrument.values()[0].instrumentName);
        instrumentChanger.addActionListener(new ActionListener() {
            int curr = 1;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (curr == 5) curr = 0;
                try {
                    ViolinTuner.this.changeChannel();
                } catch(MidiUnavailableException noMidi) {
                    noMidi.printStackTrace();
                }
                instrumentChanger.setText(Instrument.values()[curr].instrumentName);
                curr++;
            }
        });
        JPanel instrumentPanel = new JPanel();
        instrumentPanel.add(instrumentChanger);

        // buttons to panel
        JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        int i = 0;
        for (Note note : Note.values()) {
            notePanel.add(buttons[i++] = makeButton(note));
        }

        // Minimum width to ensure window title is not truncated
        JPanel widener = new JPanel();
        widener.setPreferredSize(new Dimension(250, 0));

        // Panels to frame
        frame.add(notePanel, BorderLayout.NORTH);
        frame.add(instrumentPanel, BorderLayout.CENTER);
        frame.add(widener, BorderLayout.SOUTH);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static MidiChannel initMidiChannel(int instrument) throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[0];
        channel.programChange(instrument);
        channel.setChannelPressure(5);  // optional vibrato
        return channel;
    }

    private AbstractButton makeButton(final Note note) {
        AbstractButton button = new JToggleButton(note.name());
        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (((AbstractButton)e.getSource()).isSelected()) {
                    for (AbstractButton b : ViolinTuner.this.buttons) {
                        // Turn off any note that might be being played
                        if (b != e.getSource()) {
                            b.setSelected(false);
                        }
                    }
                    ViolinTuner.this.play(note);
                } else {
                    ViolinTuner.this.silence();
                }
            }
        });
        return button;
    }

    public void play(Note note) {
        this.channel.noteOn(note.midiNoteNumber, 127); // 127 is maximum volume
    }

    public void silence() {
        this.channel.allNotesOff();
    }

    public void changeChannel() throws MidiUnavailableException {
        ViolinTuner.this.silence();
        if (c == 5) c = 0;
        this.channel = initMidiChannel(Instrument.values()[c].instrumentChannel);
        c++;
    }   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new ViolinTuner();
                } catch (MidiUnavailableException noMidi) {
                    noMidi.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}
