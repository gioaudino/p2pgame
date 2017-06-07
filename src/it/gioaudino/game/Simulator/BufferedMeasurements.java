package it.gioaudino.game.Simulator;

import it.unimi.Simulator.Buffer;
import it.unimi.Simulator.Measurement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BufferedMeasurements implements Buffer {

    private List<Measurement> buffer = new ArrayList<>();

    @Override
    public void addNewMeasurement(Measurement measurement) {
        buffer.add(measurement);
    }

    @Override
    public List<Measurement> readAllAndClean() {
        List<Measurement> tbr = buffer;
        buffer = new ArrayList<>();
        return tbr;
    }
}
