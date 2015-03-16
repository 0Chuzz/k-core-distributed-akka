package Pi;

import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


//#backend
public class PiBackend extends UntypedActor {
    final int N = 20;
    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() {
        log.info("Initializing backend");
    }

    private double calculatePiFor(int start, int nrOfElements) {
        double acc = 0.0;
        for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
            acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
        }
        return acc;
    }

    @Override
    public void onReceive(Object message) {
        //log.debug("message received");
        if (message instanceof PiWork) {
            final int n = ((PiWork) message).n;
            final int nrE = ((PiWork) message).nrofEls;

            Future<PiResult> result = Futures.future(new Callable<PiResult>() {
                public PiResult call() {
                    PiResult ret = new PiResult(n, calculatePiFor(n, nrE));
                    log.debug(ret.toString());
                    return ret;
                }
            }, getContext().dispatcher());


            Patterns.pipe(result, getContext().dispatcher()).to(getSender());
            //getSender().tell(new FactorialResult(n, calculatePiFor(n, nrE)), getSelf());

        } else {
            log.debug("unhandled message {}", message);
            unhandled(message);
        }
    }
}
//#backend

