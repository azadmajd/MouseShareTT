package de.ugdata.mousesharett.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Collection;

@Service
public class WorkerRegistry {
    
    public static class WorkerInfo {
        private String id;
        private String hostname;
        private String ip;
        private long lastHeartbeat;

        public WorkerInfo(String id, String hostname, String ip) {
            this.id = id;
            this.hostname = hostname;
            this.ip = ip;
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getHostname() { return hostname; }
        public String getIp() { return ip; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    }

    private final Map<String, WorkerInfo> workers = new ConcurrentHashMap<>();

    public void register(String id, String hostname, String ip) {
        workers.put(id, new WorkerInfo(id, hostname, ip));
    }

    public void updateHeartbeat(String id) {
        WorkerInfo info = workers.get(id);
        if (info != null) {
            info.setLastHeartbeat(System.currentTimeMillis());
        }
    }

    public Collection<WorkerInfo> getActiveWorkers() {
        // Simple cleanup: remove workers that haven't sent a heartbeat in 30 seconds
        long timeout = System.currentTimeMillis() - 30000;
        workers.entrySet().removeIf(entry -> entry.getValue().getLastHeartbeat() < timeout);
        return workers.values();
    }
}
