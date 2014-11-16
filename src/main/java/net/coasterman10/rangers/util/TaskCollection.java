package net.coasterman10.rangers.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TaskCollection {
    private static final int CLEANUP_THRESHOLD = 10;
    
    private Collection<BukkitTask> tasks = new HashSet<>();

    public void addTask(BukkitTask task) {
        tasks.add(task);

        // Clear any old tasks to prevent excessive memory leak
        if (tasks.size() >= CLEANUP_THRESHOLD) {
            for (Iterator<BukkitTask> it = tasks.iterator(); it.hasNext();) {
                int id = it.next().getTaskId();
                if (!Bukkit.getScheduler().isCurrentlyRunning(id) && !Bukkit.getScheduler().isQueued(id))
                    it.remove();
            }
        }
    }

    public void cancelAll() {
        for (Iterator<BukkitTask> it = tasks.iterator(); it.hasNext();) {
            Bukkit.getScheduler().cancelTask(it.next().getTaskId());
            it.remove();
        }
    }
}
