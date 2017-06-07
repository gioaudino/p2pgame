package it.gioaudino.game.Simulator;

import it.unimi.Simulator.Measurement;

import java.util.List;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class MeasurementAnalyser implements Runnable {

    private static final int SLEEPING_TIME = 1000;
    private BufferedMeasurements buffer;
    private boolean isKilled = false;

    public MeasurementAnalyser(BufferedMeasurements buffer) {
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
            for(Measurement measure: measurements){

            }

        }
    }
}
