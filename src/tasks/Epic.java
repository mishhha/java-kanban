package tasks;

import taskmanager.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subTasksId;

    public ArrayList<Integer> getSubTasks() {
        return subTasksId;
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subTasksId = new ArrayList<>();
    }

}
