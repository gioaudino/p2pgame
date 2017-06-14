package it.gioaudino.game.Simulator;

import it.unimi.Simulator.Buffer;
import it.unimi.Simulator.Measurement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class MeasurementAnalyser implements Runnable {

    private static final int SLEEPING_TIME = 1000;
    private Buffer<Measurement> buffer;
    private boolean isKilled = false;
    private int s = 0;
    private Map<Integer, Double> mean = new HashMap<>();
    private Map<Integer, Double> ema = new HashMap<>();

    public MeasurementAnalyser(Buffer<Measurement> buffer) {
        this.buffer = buffer;
    }

    public void setKilled() {
        isKilled = true;
    }

    @Override
    public void run() {
        while (!isKilled) {
            try {
                Thread.sleep(SLEEPING_TIME);
            } catch (Exception ignored) {
            }
            List<Measurement> measurements = buffer.readAllAndClean();
//            for (Measurement measure : measurements) {
//                System.out.println(measure.getValue());
//            }

        }
    }
}
