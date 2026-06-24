package server;

import java.util.AbstractMap.SimpleEntry;

public class Dispatcher extends Thread {
    private SharedBuffer sharedBuffer;
    private ChatServer server;
    private boolean running;
    
    public Dispatcher(SharedBuffer sharedBuffer, ChatServer server) {
        this.sharedBuffer = sharedBuffer;
        this.server = server;
        this.running = true;
    }
    
    @SuppressWarnings("unchecked")
    public void run() {
        while (running) {
            try {
                // Take the entry (Request + ClientHandler) from buffer
                SimpleEntry<Request, ClientHandler> entry = (SimpleEntry<Request, ClientHandler>) sharedBuffer.take();
                Request request = entry.getKey();
                ClientHandler handler = entry.getValue();
                
                server.log("Dispatcher took request: " + request.getType());
                
                // Create worker with both request AND handler
                Worker worker = new Worker(request, server, handler);
                worker.start();
                
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
    
    public void stopDispatcher() {
        running = false;
        this.interrupt();
    }
}