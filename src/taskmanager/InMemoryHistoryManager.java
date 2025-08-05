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

    public void linkLast(Task task) { // Метод по добавлению узлов в конец истории и удаления головы.
        if (task == null) return; // Если задача пустая, прерываем
        int id = task.getId(); // Получаем id задачи

        if (history.containsKey(id)) { // Если история содержит узел с таким id
            remove(id); // Удалить через метод
        }

        Node node = new Node(task); // Создаем новый узел с задачей, если такой нет.

        if (tail == null) { // Если хвост пустой
            head = node; // Присваиваем ссылки на голову
            tail = node; // и на хвост
        } else { // Иначе если не пустой
            tail.next = node; // Ссылаем хвост на новый узел
            node.prev = tail; // А новый узел ссылаем на старый хвост
            tail = node; // Новым хвостом, стал новый узел.
        }

        history.put(id, node); // Добавили в историю новый узел

        if (history.size() > 10) { // Если размер истории больше 10
            remove(head.task.getId()); // Получаем id задачи, узла головы и удаляем, старую, так как удаляем с начала.
        }
    }

    public List<Task> getTasks() { // Метод получения задач. Сложность операции O(n)
        List<Task> result = new ArrayList<>(); // Создали список с задачами
        Node current = head; // Получили узел головы
        while (current != null) { // Пока узел не нул, продолжаем
            result.add(current.task); // Добавляем в список
            current = current.next; // Перешагиваем на след. узел, проходимся по мапе.
        }
        return result; // Возвращаем список
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

        Task taskCopy = new Task( // Создаю копию задачи, что бы тест на проверку старой версии задачи работал.
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getTaskStatus()
        );

        Node node = new Node(taskCopy); // Создаем новый узел на основе копии задачи.
        node.next = head; // Новый узел ссылается на след узел -> Старая голова (nodeNew -> oldHead)

        if (head != null) { // Если head не пусто
            head.prev = node; // Старая голова ссылается на предыдущий узел -> новый узел, сдвинули.
        }

        head = node; // Ну и Head теперь новый узел -> node

        if (tail == null) { // Если хвост пустой
            tail = node; // Хвост теперь новый узел
        }

        history.put(id, node); // Добавляем в историю

        if (history.size() > 10) { // Если размер истории >10
            remove(tail.task.getId()); // Удаляем методом
        }
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