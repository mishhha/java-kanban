package tasks;

public class SubTask extends Task {

    private Integer epicId;

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Integer epicId) {
        super(name, description, TaskStatus.NEW);
        this.epicId = epicId;
    }
}
