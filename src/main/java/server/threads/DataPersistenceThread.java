package server.threads;

import java.util.logging.Logger;

import server.dataStructures.SharedObject;

/**
 * This class represents a thread responsible for persisting data at regular intervals.
 */
public class DataPersistenceThread implements Runnable {

  private static final Logger logger = Logger.getLogger(DataPersistenceThread.class.getName());

  /**
   * The run method of the DataPersistenceThread.
   * This method is responsible for saving data at regular intervals.
   */
  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(10000);
        SharedObject.saveData();
        logger.info("Data Saved");
      } catch (Exception e) {
        logger.severe("Error Saving Data! " + e.getMessage());
      }
    }
  }
}
