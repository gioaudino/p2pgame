package it.gioaudino.game.Simulator;

import it.gioaudino.game.Client.ClientObject;
import it.gioaudino.game.Entity.PositionZone;
import it.unimi.Simulator.Buffer;
import it.unimi.Simulator.Measurement;

import java.util.List;

/**
 * Created by gioaudino on 07/06/17.
 * Package it.gioaudino.game.Simulator in game
 */
public class MeasurementAnalyser implements Runnable {

    private static final int SLEEPING_TIME = 1000;
    private static final double ALPHA = 0.5;
    private static final double THRESHOLD = 10;


    private ClientObject client;
    private Buffer<Measurement> buffer;
    private boolean isKilled = false;
    private Double last = null;

    public MeasurementAnalyser(ClientObject client, Buffer<Measurement> buffer) {
        this.client = client;
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
            new Thread(new QuickAnalyzer(buffer.readAllAndClean())).start();
        }
    }

    private class QuickAnalyzer implements Runnable {

        private List<Measurement> measurements;

        private QuickAnalyzer(List<Measurement> measurements) {
            this.measurements = measurements;
        }

        @Override
        public void run() {
            double mean = 0;
            for (Measurement m : measurements) mean += m.getValue();
            mean /= measurements.size();
            if (last == null) {
                last = ALPHA * mean;
            } else {
                Double ema = last + ALPHA * (mean - last);
                if (ema - last > THRESHOLD) {
                    client.addBomb(ema);
                }
                last = ema;
            }

        }
    }
}
