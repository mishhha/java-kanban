package tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<SubTask> subTasks;

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subTasks = new ArrayList<>();
    }

    public Epic(Integer id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
        this.subTasks = new ArrayList<>();
    }

}