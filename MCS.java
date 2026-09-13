import java.util.concurrent.atomic.AtomicReference;


public class MCS implements Lock {

    //Node a thread queues itself with; it spins on its own `locked` flag.
    private static class QNode {

        volatile boolean locked = false;

        final AtomicReference<QNode> next = new AtomicReference<>(null);
    }

    private final AtomicReference<QNode> tail = new AtomicReference<>(null);
 
    // Each thread keeps its own node handle across lock()/unlock().
    private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

    @Override
    public void lock(){

        QNode qnode = myNode.get();

        // reset from any previous use
        qnode.next.set(null); 
        
        // atomically enqueue myself
        QNode pred = tail.getAndSet(qnode);

        if (pred != null) {

            // There was already someone in the queue: wait to be signaled.
            qnode.locked = true;

            pred.next.set(qnode);// link predecessor -> me

            while (qnode.locked) { 
                // spin on OWN node
                Thread.yield();
            }
        }
        // pred == null means the queue was empty: lock acquired immediately.
    }
 
    @Override
    public void unlock(){

        QNode qnode = myNode.get();

        if (qnode.next.get() == null){

            // No visible successor yet. Try to atomically clear the tail,
            // meaning "the queue is now empty".

            if (tail.compareAndSet(qnode, null)) {

                return;
            }

            // CAS failed: a successor is in the middle of enqueuing itself
            // (it already did getAndSet on tail but hasn't set pred.next yet).
            // Wait for that link to appear.


            while (qnode.next.get() == null) {

                Thread.yield(); // scheduling hint: be polite when oversubscribed / single-core
            }
        }


        // Signal the successor directly.
        qnode.next.get().locked = false;
        
        qnode.next.set(null);
    }
    
}
