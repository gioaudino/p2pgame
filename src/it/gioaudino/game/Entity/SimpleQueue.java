package it.gioaudino.game.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gioaudino on 12/06/17.
 * Package it.gioaudino.game.Service in game
 */
public class SimpleQueue<T> {
    private List<T> queue;

    public SimpleQueue() {
        queue = new ArrayList<>();
    }

    public void push(T el) {
        queue.add(el);
    }

    public T pop() {
        return queue.remove(0);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void clearQueue() {
        queue = new ArrayList<>();
    }
}
