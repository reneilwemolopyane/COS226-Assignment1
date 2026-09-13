import java.util.concurrent.atomic.AtomicBoolean;

public class TTAS implements Lock {

// false = unlocked, true = locked

private final AtomicBoolean state = new AtomicBoolean(false);

@Override 
public void lock(){

    while(true){
        //Spin phase: just read, don't write, while someone
        //else holds it

        while(state.get()){

            Thread.yield();
        }

        //Test phase: try to actually grab it with an atomic RMW

        if(!state.getAndSet(true)){

            //We flipped it from false -> true: we own the lock now
            return;


        }

        //Someone beat us to it, go back to spinning

    }

}

@Override 
public void unlock(){

state.set(false);
}
    
}
