package com.kot.core;

import com.kot.model.Chef;
import com.kot.model.Waiter;
import com.kot.ui.gui.ChefWindow;
import com.kot.ui.gui.WaiterWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton registry linking active windows to background threads so Owner can manage them.
 */
public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    
    private final List<WaiterWindow> activeWaiters;
    private final List<ChefWindow> activeChefs;
    private final List<Integer> loggedInUserIds;

    private SessionManager() {
        this.activeWaiters = new ArrayList<>();
        this.activeChefs = new ArrayList<>();
        this.loggedInUserIds = new ArrayList<>();
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public synchronized void registerWaiter(WaiterWindow window) {
        activeWaiters.add(window);
    }

    public synchronized void unregisterWaiter(WaiterWindow window) {
        activeWaiters.remove(window);
        window.disposeWindow();
    }

    public synchronized void registerChef(ChefWindow window) {
        activeChefs.add(window);
    }

    public synchronized void unregisterChef(ChefWindow window) {
        activeChefs.remove(window);
        window.disposeWindow();
    }

    public synchronized List<Waiter> getActiveWaitersList() {
        List<Waiter> waiters = new ArrayList<>();
        for (WaiterWindow w : activeWaiters) {
            waiters.add(w.getWaiter());
        }
        return waiters;
    }
    
    public synchronized List<Chef> getActiveChefsList() {
        List<Chef> chefs = new ArrayList<>();
        for (ChefWindow c : activeChefs) {
            chefs.add(c.getChef());
        }
        return chefs;
    }
    
    public synchronized WaiterWindow getWaiterWindow(int id) {
        for (WaiterWindow w : activeWaiters) {
            if (w.getWaiter().getEmployeeId() == id) return w;
        }
        return null;
    }

    public synchronized ChefWindow getChefWindow(int id) {
        for (ChefWindow c : activeChefs) {
            if (c.getChef().getEmployeeId() == id) return c;
        }
        return null;
    }
    
    public synchronized boolean isUserLoggedIn(int userId) {
        return loggedInUserIds.contains(userId);
    }
    
    public synchronized void registerSession(int userId) {
        loggedInUserIds.add(userId);
    }
    
    public synchronized void unregisterSession(int userId) {
        loggedInUserIds.remove(Integer.valueOf(userId));
    }
}
