package it.gioaudino.game.Simulator;

import it.unimi.Simulator.Buffer;
import it.unimi.Simulator.Measurement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class BufferedMeasurements<T extends Measurement> implements Buffer<T> {

    private List<T> buffer = new ArrayList<>();


    @Override
    public void addNewMeasurement(T value) {
        this.buffer.add(value);
    }

    @Override
    public List<T> readAllAndClean() {
        List<T> tbr = buffer;
        buffer = new ArrayList<>();
        return tbr;
    }
}
