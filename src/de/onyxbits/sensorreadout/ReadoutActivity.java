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

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.hardware.*;
import java.util.*;
import android.graphics.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;

import org.achartengine.*;
import org.achartengine.chart.*;
import org.achartengine.model.*;
import org.achartengine.renderer.*;
import org.achartengine.tools.*;

/**
 * <code>Activity</code> that displays the readout of one <code>Sensor</code>.
 * This <code>Activity</code> must be started with an <code>Intent</code> that passes 
 * in the number of the <code>Sensor</code> to display. If none is passed, the
 * first available <code>Sensor</code> is used.
 */
public class ReadoutActivity extends Activity implements View.OnTouchListener {

  /**
   * For passing the index number of the <code>Sensor</code> in its <code>SensorManager</code>
   */
  public static final String SENSORINDEX = "de.onyxbits.sensorreadout.SensorIndex";
  
  /**
   * How often to sample per second.
   */
  public static final int SAMPLERATE = 10;
  
  /**
   * The <code>Sensor</code> we are dealing with
   */
  private Sensor sensor;
  
  /**
   * The displaying component
   */
  protected GraphicalView chartView;
  
  /**
   * Dataset of the graphing component
   */
  private XYMultipleSeriesDataset sensorData;
  
  /**
   * Renderer for actually drawing the graph
   */
  protected XYMultipleSeriesRenderer renderer;
  
  /**
   * Our <code>SensorManager</code>
   */
  private SensorManager sensorManager;
  
  /**
   * Data channels. Corresponds to <code>SensorEvent.values</code>. Individual channels may
   * be set to null to indicate that they must not be painted.
   */
  protected XYSeries channel[];
  
  /**
   * The ticker thread takes care of updating the UI
   */
  private Thread ticker;
  
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    int idx = getIntent().getIntExtra(SENSORINDEX,0);
    sensor = sensorManager.getSensorList(Sensor.TYPE_ALL).get(idx);
    setTitle(sensor.getName());
    
    sensorData = new XYMultipleSeriesDataset();
    renderer = new XYMultipleSeriesRenderer();
    renderer.setGridColor(Color.DKGRAY);
    renderer.setShowGrid(true);
    renderer.setXAxisMin(0.0);
    double[] range= {1,1,1,1};
    renderer.setXTitle("Samplerate: 1/"+ (1000 / SAMPLERATE) +" ms");
    renderer.setXAxisMax(10000 / (1000 / SAMPLERATE)); // 10 seconds wide
    renderer.setXLabels(10); // 1 second per DIV
    renderer.setChartTitle(" ");
    renderer.setYLabelsAlign(Paint.Align.RIGHT);
    chartView = ChartFactory.getLineChartView(this,sensorData,renderer);
    chartView.setOnTouchListener(this);
    float textSize = new TextView(this).getTextSize();
    renderer.setLabelsTextSize(textSize);
    renderer.setLegendTextSize(textSize);
    renderer.setChartTitleTextSize(textSize);
    // Note: The chart is not ready to use yet! It still lacks some information, we can only
    // obtain from a SensorEvent, so its either sticking to only known sensors or defereing
    // the final setup till we get our hands on such an event. Design choice: Let's try to even
    // handle unknown sensors as good as we can.
    setContentView(chartView);
  }
  
  /**
   * Final configuration step. Must be called between receiving the first <code>SensorEvent</code>
   * and updating the graph for the first time. This is done from the ticker thread.
   * @param event the event
   */
  protected void configure(SensorEvent event) {
    String channelNames[] = {"X-Axis", "Y-Axis", "Z-Axis"}; // Defaults...
    channel = new XYSeries[event.values.length]; // ..work for most sensors
    
    switch (event.sensor.getType()) {
      case Sensor.TYPE_ACCELEROMETER: {
        renderer.setYTitle("m/s²");
        break;
      }
      case Sensor.TYPE_GRAVITY: {
        channel = new XYSeries[event.values.length];
        renderer.setYTitle("m/s²");
        break;
      }
      case Sensor.TYPE_GYROSCOPE: {
        channel = new XYSeries[event.values.length];
        renderer.setYTitle("rad/s");
        break;
      }
      case Sensor.TYPE_LIGHT: {
        channel = new XYSeries[1];
        channelNames = new String[1];
        channelNames[0] = "Light";
        renderer.setYTitle("lux");
        break;
      }
      case Sensor.TYPE_LINEAR_ACCELERATION: {
        renderer.setYTitle("m/s²");
        break;
      }
      case Sensor.TYPE_MAGNETIC_FIELD: {
        renderer.setYTitle("µT");
        break;
      }
      case Sensor.TYPE_PRESSURE: {
        renderer.setYTitle("hPa");
        break;
      }
      case Sensor.TYPE_PROXIMITY: {
        channel = new XYSeries[1];
        channelNames = new String[1];
        channelNames[0] = "Distance";
        renderer.setYTitle("cm");
        break;
      }
      case Sensor.TYPE_ROTATION_VECTOR: {
        break;
      }
      case Sensor.TYPE_ORIENTATION: {
        break;
      }
      case 7:
      case 13: {
        // Dirty hack: TYPE_TEMPERATURE became deprecated in favour of TYPE_AMBIENT_TEMPERATURE. By
        // using the numeric instead of the symbolic constants, we can easily compile for pre- and
        // post API level 14.
        renderer.setYTitle("°C");
        channel = new XYSeries[1];
        channelNames = new String[1];
        channelNames[0] = "Ambient room temperature";
        break;
      }
      
      default: {
        // Unknown sensor -> Just show all the channels.
        channel = new XYSeries[event.values.length];
        for (int i=0;i<channelNames.length;i++) channelNames[i]="Channel"+i;
      }
    }
    
    int[] colors = { Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN };
    for (int i=0;i<channel.length;i++) {
      channel[i] = new XYSeries(channelNames[i]);
      sensorData.addSeries(channel[i]);
      XYSeriesRenderer r = new XYSeriesRenderer();
      r.setColor(colors[i % colors.length] );
      renderer.addSeriesRenderer(r);
    }
    
    if (channel.length==1) {
      // I'm pretty sure there's a more elegant solution to this, but for now: dirty hack!
      // Sensors that only have one channel, which also only delivers an unchanging value without
      // external stimulus (e.g. cheap light or proximity sensors) don't provide the chartengine
      // with enough data to chop the Y Axis into units. The result is that the readout flatlines
      // and get's painted over by the X Axis. So to prevent this, we add two invisible channels
      // that each contain one sample.
      XYSeries[] fake = {new XYSeries("mute A"), new XYSeries("mute B")};
      double half = event.values[0]*0.5+1;
      XYSeriesRenderer r = new XYSeriesRenderer();
      r.setColor(Color.BLACK);
      fake[0].add(0,event.values[0]+half);
      sensorData.addSeries(fake[0]);
      fake[1].add(0,event.values[0]-half);
      sensorData.addSeries(fake[1]);
      renderer.addSeriesRenderer(r);
      renderer.addSeriesRenderer(r);
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    
    // Lock the screen to its current rotation. Some sensors become impossible to read otherwise.
    switch (getResources().getConfiguration().orientation) {
      case Configuration.ORIENTATION_PORTRAIT: {
        setRequestedOrientation(
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      }
      case Configuration.ORIENTATION_LANDSCAPE: {
        setRequestedOrientation(
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        break;
      }
    }
    
    if (channel!=null) {
      for (XYSeries chan: channel) {
        if (chan!=null) {
          chan.clear();
        }
      }
    }
    
    ticker = new Ticker(this);
    ticker.start();
    sensorManager.registerListener((SensorEventListener)ticker, sensor, SensorManager.SENSOR_DELAY_UI);
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener((SensorEventListener)ticker);
    try {
      ticker.interrupt();
      ticker.join();
      ticker=null;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  // Interface: View.OnTouchListener
  public boolean onTouch(View v, MotionEvent event) {
    // channel!=null -> need to ensure that configure() has been called before the user is
    // allowed to abort (can happen by an accidental doubletap on app start). Otherwise the
    // screen will just stay black, making the app appear to hang.
    if (v==chartView && ticker!=null && channel!=null) {
      try {
        // Design decission: When the user pans the view, s/he will (likely) no longer see the point of 
        // data entry. We might as well stop sampling then, since the user will (likely) not want to
        // bother finding that point again if it is still moving. 
        // Follow up design decission: Stopping is final. We don't provide a resume option. Doing so would
        // only add complexity to the code/app for the sole purpose of producing a faulty graph. If the user
        // wants to continue, s/he has to return to the OverviewActivity and restart from there.
        sensorManager.unregisterListener((SensorEventListener)ticker);
        ticker.interrupt();
        ticker.join();
        ticker=null;
        Toast.makeText(this,R.string.msg_stopped,Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) {}
    }
    return v.onTouchEvent(event);
  }
}