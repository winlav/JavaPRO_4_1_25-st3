import mythreadpool.SimpleThreadPool;

public class Main {
    public static void main(String[] args) {
        // Создаём пул из 3 рабочих потоков
        SimpleThreadPool pool = new SimpleThreadPool(4);

        // Добавляем 10 задач
        for (int i = 1; i <= 10; i++) {
            int taskNumber = i;
            pool.execute(() -> {
                System.out.println("    Задача №" + taskNumber + " выполняется потоком " +
                        Thread.currentThread().getName());
                try {
                    Thread.sleep(1000); // Имитация длительной работы
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("    Задача №" + taskNumber + " завершена потоком " +
                        Thread.currentThread().getName());
            });
        }

        // Останавливаем пул: новые задачи больше не принимаются
        pool.shutdown();

        // Ожидаем завершение всех рабочих потоков
        pool.awaitTermination();

        System.out.println("Все задачи выполнены, пул потоков завершил работу");
    }
}
