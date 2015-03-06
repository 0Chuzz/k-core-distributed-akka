package Pi;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

//#frontend
public class PiFrontend extends UntypedActor {
  final int upToN;
  final boolean repeat;
  private double totpi = 0;
  private int counter = 0;

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  ActorRef backend = getContext().actorOf(FromConfig.getInstance().props(),
      "piBackendRouter");

  public PiFrontend() {
    this.upToN = 20;
    this.repeat = true;
  }

  @Override
  public void preStart() {
    log.info("Initializing...");
    sendJobs();
    getContext().setReceiveTimeout(Duration.create(10, TimeUnit.SECONDS));
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof PiResult) {
      PiResult result = (PiResult) message;
      log.debug("Result received: {}! = {}", result.n, result.pi);
      totpi += result.pi;
      counter++;
      if (counter == upToN){
        log.info("PI = {}", totpi);
        counter = 0;
        totpi = 0;
        sendJobs();
      }
      /*
      if (result.n == upToN) {
        if (repeat) {
          log.info("Top result received, restarting jobs...");
          sendJobs();
        }else
          getContext().stop(getSelf());
      }*/


    } else if (message instanceof ReceiveTimeout) {
      log.info("Timeout");
      counter = 0;
      totpi = 0;
      sendJobs();

    } else {
      unhandled(message);
    }
  }

  void sendJobs() {
    log.info("Starting batch of workers up to [{}]", upToN);
    for (int n = 0; n <= upToN; n ++) {
      backend.tell(new PiWork(n, upToN), getSelf());
    }
  }

}

//#frontend

