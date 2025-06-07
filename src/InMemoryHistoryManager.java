import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        history.removeIf(t -> t.getId() == task.getId());
        history.add(0, task);
        if (history.size() > 10) {
            history.remove(history.size() - 1);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию
    }

}
