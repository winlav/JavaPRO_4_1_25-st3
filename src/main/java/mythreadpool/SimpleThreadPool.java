package mythreadpool;

import java.util.LinkedList;
import java.util.List;

/*
  Простейший пул потоков с очередью задач на основе LinkedList.
*/
public class SimpleThreadPool {

    // Список всех рабочих потоков (воркеров)
    private final List<Worker> workers = new LinkedList<>();
    // Очередь задач для выполнения
    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    // Флаг, что пул закрыт (новые задачи не принимаются)
    private volatile boolean isShutdown = false;

/*
  Конструктор: создаёт пул с заданным количеством потоков.
  Сразу запускает все рабочие потоки.
*/
    public SimpleThreadPool(int numThreads) {
        for (int i = 1; i <= numThreads; i++) {
            Worker worker = new Worker("Воркер-" + i);
            worker.start(); // Запускаем поток
            workers.add(worker);
        }
    }

/*
  Добавляет новую задачу в очередь на выполнение.
*/
    public void execute(Runnable task) {
        synchronized (taskQueue) {
            if (isShutdown) {
                // Если пул закрыт — выбрасываем исключение
                throw new IllegalStateException("Пул потоков закрыт. Новые задачи не принимаются.");
            }
            taskQueue.addLast(task); // Добавляем задачу в конец очереди
            System.out.println("Задача добавлена в очередь.");
            taskQueue.notifyAll(); // Будим воркеры: задача появилась!
        }
    }

/*
 Закрывает пул:
 Новые задачи больше не принимаются,
 все воркеры завершат работу после выполнения оставшихся задач.
*/
    public void shutdown() {
        synchronized (taskQueue) {
            isShutdown = true; // Ставим флаг закрытия
            System.out.println("Пул потоков закрыт. Новые задачи больше не принимаются.");
            taskQueue.notifyAll(); // Будим всех воркеров: пора завершаться
        }
    }

/*
 Ожидает завершения всех рабочих потоков.
 Аналогично awaitTermination() у стандартного ThreadPoolExecutor.
*/
    public void awaitTermination() {
        for (Worker worker : workers) {
            try {
                worker.join(); // Ждём завершения каждого воркера
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Ожидание завершения воркеров прервано.");
            }
        }
        System.out.println("Все рабочие потоки завершили работу.");
    }

/*
 Класс рабочего потока.
 Каждый Worker берёт задачи из очереди и выполняет их.
*/
    private class Worker extends Thread {

        public Worker(String name) {
            super(name); // Задаём имя потока
        }

        @Override
        public void run() {
            Runnable task;
            while (true) {
                synchronized (taskQueue) {
                    // Пока очередь пуста и пул не закрыт — ждём
                    while (taskQueue.isEmpty() && !isShutdown) {
                        try {
                            System.out.println(getName() + " ждёт задачу...");
                            taskQueue.wait(); // Засыпаем, пока не появится задача или не будет shutdown
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println(getName() + " прерван во время ожидания задачи.");
                            return; // Прерываем работу
                        }
                    }

                    // Если задач больше нет и пул закрыт — выходим из цикла
                    if (taskQueue.isEmpty() && isShutdown) {
                        System.out.println(getName() + " завершает работу (нет задач и пул закрыт).");
                        break;
                    }

                    // Берём задачу из очереди
                    task = taskQueue.poll();
                }

                if (task != null) {
                    System.out.println(getName() + " выполняет задачу...");
                    try {
                        task.run(); // Выполняем задачу
                        System.out.println(getName() + " завершил задачу.");
                    } catch (Exception e) {
                        System.out.println(getName() + " поймал исключение при выполнении задачи: " + e.getMessage());
                    }
                }
            }
        }
    }
}
