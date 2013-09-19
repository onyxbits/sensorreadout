/*
   Copyright 2012 Patrick Ahlbrecht

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
   * For moving the viewport of the graph
   */
  private int xTick = 0;
  
  /**
   * For moving the viewport of the grpah
   */
  private int lastMinX = 0; 
  
  /**
   * How long to sleep between taking a sample
   */
  private static final int SLEEPTIME = (int) 1000/ReadoutActivity.SAMPLERATE;
  
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
          Thread.sleep(SLEEPTIME);
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
      // Dirty, but we only learn a few things after getting the first event.
      activity.configure(currentEvent);
    }
    
    if (xTick > activity.renderer.getXAxisMax()) {
      activity.renderer.setXAxisMax(xTick);
      activity.renderer.setXAxisMin(++lastMinX);
    }
    
    activity.fitYAxis(currentEvent);
    
    for (int i=0;i<activity.channel.length;i++) {
      if (activity.channel[i]!=null) {
        activity.channel[i].add(xTick,currentEvent.values[i]);
      }
    }
    
    xTick++;

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