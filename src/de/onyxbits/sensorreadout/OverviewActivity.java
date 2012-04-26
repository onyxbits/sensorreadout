package de.onyxbits.sensorreadout;

import android.app.*;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.hardware.*;
import java.util.*;
import android.graphics.*;
import android.content.*;

/**
 * Main <code>Activity</code>. Shows a list of all available <code>Sensor</code>S and
 * starts the <code>ReadoutActivity</code> of the selected <code>Sensor</code>
 */
public class OverviewActivity extends ListActivity {

  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    setListAdapter(new SensorAdapter(this,0,sensorManager.getSensorList(Sensor.TYPE_ALL)));
  }
  
  public void onListItemClick(ListView l, View v, int position, long id) {
    Intent intent = new Intent(this, ReadoutActivity.class);
    intent.putExtra(ReadoutActivity.SENSORINDEX,position);
    startActivity(intent);
  }
}
