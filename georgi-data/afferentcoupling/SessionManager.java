package com.example.session;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private Map<String, String> sessions = new HashMap<>();

    public void createSession(String sessionId, String userId) {
        sessions.put(sessionId, userId);
    }

    public String getUserBySession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
