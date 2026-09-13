import java.util.concurrent.atomic.AtomicReference;

public class CHL implements Lock{

//This is the node whose locked flag
//a successor thread spins on

private static class QNode {


    volatile boolean locked = false;

}

// (its locked flag is false, so the first thread proceeds immediately)
private final AtomicReference<QNode> tail = new AtomicReference<>(new QNode());

private final ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

private final ThreadLocal<QNode> myPred = ThreadLocal.withInitial(() -> null);


@Override 
public void lock() {

    QNode qnode = myNode.get();

    qnode.locked = true;// announce: "I want the lock"

    QNode pred = tail.getAndSet(qnode); //atomically equeue myself

    myPred.set(pred);

    while(pred.locked){

        Thread.yield();

    }

    //pre.locked == false means it's now our turn

}

@Override 

public void unlock(){

    QNode qnode = myNode.get();

    //release: let successor proceed
    qnode.locked = false;
    
    // Recycle predecessor's node as our own node for the next lock() call,
    // since the predecessor node is now free to be reused (garbage-free-ish).

    myNode.set(myPred.get());

    myPred.set(null);

}

  
}
