import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EcommerceFlashSaleInventoryManager {

    // productId -> stock count
    private final ConcurrentHashMap<String, AtomicInteger> inventory;

    // productId -> waiting list (FIFO)
    private final ConcurrentHashMap<String, Queue<Long>> waitingList;

    public EcommerceFlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product with stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
    }

    // Check stock (O(1))
    public String checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        if (stock == null) {
            return "Product not found";
        }
        return stock.get() + " units available";
    }

    // Purchase item (thread-safe)
    public String purchaseItem(String productId, long userId) {
        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();

            // If stock available → try atomic decrement
            if (currentStock > 0) {
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                // Add to waiting list (FIFO)
                Queue<Long> queue = waitingList.get(productId);
                synchronized (queue) {
                    queue.add(userId);
                    return "Added to waiting list, position #" + queue.size();
                }
            }
        }
    }

    // Restock and serve waiting list
    public void restock(String productId, int quantity) {
        AtomicInteger stock = inventory.get(productId);
        Queue<Long> queue = waitingList.get(productId);

        if (stock == null || queue == null) return;

        stock.addAndGet(quantity);

        synchronized (queue) {
            while (stock.get() > 0 && !queue.isEmpty()) {
                queue.poll(); // serve next user
                stock.decrementAndGet();
            }
        }
    }

    // Get waiting list size
    public int getWaitingListSize(String productId) {
        Queue<Long> queue = waitingList.get(productId);
        return (queue == null) ? 0 : queue.size();
    }

    // Demo main method
    public static void main(String[] args) {
        EcommerceFlashSaleInventoryManager manager =
                new EcommerceFlashSaleInventoryManager();

        String product = "IPHONE15_256GB";

        manager.addProduct(product, 3);

        System.out.println(manager.checkStock(product));

        System.out.println(manager.purchaseItem(product, 12345));
        System.out.println(manager.purchaseItem(product, 67890));
        System.out.println(manager.purchaseItem(product, 11111));

        // Stock exhausted
        System.out.println(manager.purchaseItem(product, 99999));

        System.out.println("Waiting list size: " +
                manager.getWaitingListSize(product));
    }
}