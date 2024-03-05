import java.util.Comparator;
import java.util.PriorityQueue;

public class MyHost extends Host {
    public PriorityQueue<Task> taskQueue;
    public Task currentTask;
    public volatile boolean ok = true;

    public MyHost() {
        taskQueue = new PriorityQueue<>( new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                int prio = o2.getPriority() - o1.getPriority();
                if (prio == 0)
                    return o1.getStart() - o2.getStart();
                else
                    return prio;
            }
        });
    }
    public boolean isTaskRunning() {
        return currentTask != null;
    }

    @Override
    public void run() {
        while(ok) {
            synchronized (this) {
                if (currentTask != null) {
                    if (!taskQueue.isEmpty()) {
                        Task nextTask = taskQueue.peek();
                        if (currentTask.isPreemptible() && nextTask.getPriority() > currentTask.getPriority()) { // fac switch
                            taskQueue.add(currentTask);
                            currentTask = taskQueue.poll();
                        }
                    }
                } else {
                    if (!taskQueue.isEmpty()) {
                        currentTask = taskQueue.poll();
                    } else {
                        continue;
                    }
                }
            }

            try {
                Thread.sleep(200);
                currentTask.setLeft(currentTask.getLeft() - 200);
                if (currentTask.getLeft() <= 0) {
                    currentTask.finish();
                    currentTask = null;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public synchronized void addTask(Task task) {
        taskQueue.add(task);
    }

    @Override
    public synchronized int getQueueSize() {
        return taskQueue.size();
    }
    @Override
    public synchronized long getWorkLeft() {
        long totalWorkLeft = 0;

        if (currentTask != null) {
            totalWorkLeft += currentTask.getLeft();
        }

        for (Task task : taskQueue) {
            totalWorkLeft += task.getLeft();
        }

        return totalWorkLeft;
    }

    @Override
    public void shutdown() {
        ok = false;
    }
}
