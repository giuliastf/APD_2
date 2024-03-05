import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MyDispatcher extends Dispatcher {
    private AtomicInteger lastHostId = new AtomicInteger(-1);

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }

    @Override
    public void addTask(Task task) {
        switch (algorithm) {
            case ROUND_ROBIN:
                int nextHostId = lastHostId.incrementAndGet() % hosts.size();
                MyHost host = (MyHost) hosts.get(nextHostId);
                host.addTask(task);
                break;

            case SHORTEST_QUEUE:
                MyHost shortestQueueHost = (MyHost) hosts.get(0);
                int minQueueSize = shortestQueueHost.getQueueSize() + (shortestQueueHost.isTaskRunning() ? 1 : 0);

                for (int i = 0; i < hosts.size(); i++) {
                    MyHost currentHost = (MyHost) hosts.get(i);
                    int currentQueueSize = currentHost.getQueueSize() + (currentHost.isTaskRunning() ? 1 : 0);
                    if (currentQueueSize < minQueueSize || (currentQueueSize == minQueueSize)) {
                        shortestQueueHost = currentHost;
                        minQueueSize = currentQueueSize;
                    }
                }
                shortestQueueHost.addTask(task);
                break;

            case SIZE_INTERVAL_TASK_ASSIGNMENT:
                TaskType taskType = task.getType();
                int nodeIndex;
                switch (taskType) {
                    case SHORT:
                        nodeIndex = 0;
                        break;
                    case MEDIUM:
                        nodeIndex = 1;
                        break;
                    case LONG:
                        nodeIndex = 2;
                        break;
                    default:
                        throw new IllegalArgumentException("Tip de task necunoscut: " + taskType);
                }

                MyHost assignedHost = (MyHost) hosts.get(nodeIndex);
                assignedHost.addTask(task);
                break;

            case LEAST_WORK_LEFT:
                MyHost leastWorkHost = (MyHost) hosts.get(0);
                long minWorkLeft = leastWorkHost.getWorkLeft();

                for (int i = 1; i < hosts.size(); i++) {
                    MyHost currentHost = (MyHost) hosts.get(i);
                    long currentWorkLeft = currentHost.getWorkLeft();

                    if (currentWorkLeft < minWorkLeft || currentWorkLeft == minWorkLeft) {
                        leastWorkHost = currentHost;
                        minWorkLeft = currentWorkLeft;
                    }
                }

                leastWorkHost.addTask(task);
                break;

            default:
                System.out.println("Warning: Unknown scheduling algorithm. Defaulting to ROUND_ROBIN.");
        }
    }
}
