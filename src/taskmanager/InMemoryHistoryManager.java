package taskmanager;

import tasks.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    private static class Node {
        Task task;
        Node prev;
        Node next;

    public Node(Task task) {
        this.task = task;
    }
}

    private void removeNode(Node node) { // Метод удаления узла из истории. Сложность амортиз. О(1)
        if (node == null) return; // Если узел не пустой

        Node prev = node.prev; // Сохраняем ссылку на пред. узел
        Node next = node.next; // Сохраняем ссылку на след. узел

        if (prev != null) { // Если ссылка на пред. узел не пустая
            prev.next = next; // Ссылаем предыдущий узел на следующий за удаляемым.
        } else { // иначе если prev пустая
            head = next; // значит головой становится след узел
        }

        if (next != null) { // Если next узел не пустой
            next.prev = prev; // То next узел должен ссылаться на prev
        } else { // А если пустой
            tail = prev; // то prev становится хвостом
        }

        history.remove(node.task.getId()); // Получаем id узла и удаляем запись из истории
    }

    @Override
    public void add(Task task) { // Добавляем новый узел в начало истории. Сложность амортиз. O(1)
        if (task == null) { // Если задачи нет, прерываем.
            return;
        }
        int id = task.getId(); // Получаем id

        if (history.containsKey(id)) { // Если история содержит id задачи (Не повтор)
            remove(id); // Удаляем по id
        }

        Task taskCopy = new Task(// Создаю копию задачи, что бы тест на проверку старой версии задачи работал.
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getTaskStatus()
        );

        Node node = new Node(taskCopy); // Создаем новый узел с задачей, если такой нет.

        if (tail == null) { // Если хвост пустой
            head = node; // Присваиваем ссылки на голову
            tail = node; // и на хвост
        } else { // Иначе если не пустой
            tail.next = node; // Ссылаем хвост на новый узел
            node.prev = tail; // А новый узел ссылаем на старый хвост
            tail = node; // Новым хвостом, стал новый узел.
        }

        history.put(id, node); // Добавили в историю новый узел

    }

    @Override
    public List<Task> getHistory() { // Получение истории. Сложность операции O(n)
        List<Task> result = new ArrayList<>(); // Создали пустой список
        Node current = head; // Сохранили голову
        while (current != null) { // Повторяем, пока голова содержит узел
            result.add(current.task); // Добавляем задачу узла.
            current = current.next; // Переходим на след. узел.
        }
        return result; // Вернули список
    }

    @Override
    public void remove(int id) { // Удаляем узел по Id, который вызывает метод перелинковки узлов
        Node node = history.get(id); // Получаем узел из истории по ключу
        if (node != null) { // Если узел не пустой
            removeNode(node); // используем новый метод, передаем туда узел.
        }
    }

}