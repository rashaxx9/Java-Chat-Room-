package server;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.Queue;

public class SharedBuffer {
    private Queue<SimpleEntry<Request, ClientHandler>> buffer;
    private int capacity;
    
    public SharedBuffer(int capacity) {
        this.buffer = new LinkedList<>();
        this.capacity = capacity;
    }
    
    public synchronized void put(SimpleEntry<Request, ClientHandler> item) throws InterruptedException {
        while (buffer.size() == capacity) {
            System.out.println("Buffer FULL, waiting...");
            wait();
        }
        buffer.add(item);
        System.out.println("Buffer size: " + buffer.size());
        notifyAll();
    }
    
    public synchronized SimpleEntry<Request, ClientHandler> take() throws InterruptedException {
        while (buffer.isEmpty()) {
            System.out.println("Buffer EMPTY, waiting...");
            wait();
        }
        SimpleEntry<Request, ClientHandler> item = buffer.poll();
        System.out.println("Buffer size: " + buffer.size());
        notifyAll();
        return item;
    }
}