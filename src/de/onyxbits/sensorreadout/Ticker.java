package de.onyxbits.sensorreadout;
import android.hardware.*;
import android.app.*;
import android.os.*;

import org.achartengine.*;
import org.achartengine.chart.*;
import org.achartengine.model.*;
import org.achartengine.renderer.*;


/**
 * Receive events from the <code>Sensor</code> and periodically update the UI
 */
class Ticker extends Thread implements SensorEventListener {

  /**
   * The most recent event, received from the <code>Sensor</code>
   */
  private SensorEvent currentEvent;

  /** 
   * The activity, we are ticking for
   */
  private ReadoutActivity activity;
  
  /**
   * Reference to the worker thread which does the actual UI updating
   */
  private Ticker worker;
  
  /**
   * Tick interval in milliseconds
   */
  public static final int INTERVAL = 100;
  
  /**
   * For moving the viewport of the graph
   */
  private int xTick = 0;
  
  /**
   * For moving the viewport of the grpah
   */
  private int lastMinX = 0; 
  
  /**
   * Create a new <code>Ticker</code> and start ticking the <code>Activity</code>
   * @param activity the <code>Activity</code> to tick.
   */
  public Ticker(ReadoutActivity activity) {
    worker = new Ticker();
    worker.activity = activity;
    this.activity=activity;
  }
  
  /**
   * For creating the worker thread
   */
  private Ticker() {}
  
  // Interface: SensorEventListener
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  // Interface: SensorEventListener
  public void onSensorChanged(SensorEvent event) {
    worker.currentEvent = event;
  }
  
  @Override
  public void run() {
    // This class implements a master and a private worker. We have to figure out on which
    // Thread we are sitting.
    if (worker!=null) {
      // We are the master -> schedule the worker
      try {
        while(true) {
          Thread.sleep(INTERVAL);
          activity.runOnUiThread(worker);
        }
      }
      catch (Exception e) {
        // Goodbye
      }
    }
    else {
      // We are the worker -> update the UI
      if (currentEvent!=null) {
        updateUI();
      }
    }
  }
  
  private void updateUI() {
    
    if(activity.channel==null) {
      activity.configure(currentEvent);
    }
    
    if (xTick > activity.renderer.getXAxisMax()) {
      activity.renderer.setXAxisMax(xTick);
      activity.renderer.setXAxisMin(++lastMinX);
    }

    for (int i=0;i<activity.channel.length;i++) {
      if (activity.channel[i]!=null) {
        activity.channel[i].add(xTick++,currentEvent.values[i]);
      }
    }

    switch (currentEvent.accuracy) {
      case SensorManager.SENSOR_STATUS_ACCURACY_HIGH: {
        activity.renderer.setChartTitle("Sensor accuracy: HIGH");
        break;
      }
      case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: {
        activity.renderer.setChartTitle("Sensor accuracy: MEDIUM");
        break;
      }
      case SensorManager.SENSOR_STATUS_ACCURACY_LOW: {
        activity.renderer.setChartTitle("Sensor accuracy: LOW");
        break;
      }
      default: {
        activity.renderer.setChartTitle("Sensor accuracy: UNRELIABLE");
        break;
      }
    }
    activity.chartView.repaint();
  }
  
}