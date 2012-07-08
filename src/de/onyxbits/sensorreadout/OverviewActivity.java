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

import android.app.*;
import android.os.Bundle;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.hardware.*;
import java.util.*;
import android.graphics.*;
import android.content.*;
import android.net.*;

/**
 * Main <code>Activity</code>. Shows a list of all available <code>Sensor</code>S and
 * starts the <code>ReadoutActivity</code> of the selected <code>Sensor</code>
 */
public class OverviewActivity extends ListActivity implements AdapterView.OnItemLongClickListener,
DialogInterface.OnClickListener  {

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    setListAdapter(new SensorAdapter(this,0,sensorManager.getSensorList(Sensor.TYPE_ALL)));
    getListView().setOnItemLongClickListener(this);
    AppRater.appLaunched(this);
  }
  
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    // Short click on an item starts the readout activity
    Intent intent = new Intent(this, ReadoutActivity.class);
    intent.putExtra(ReadoutActivity.SENSORINDEX,position);
    startActivity(intent);
  }
  
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    // Long click on an item brings up verbose info about the sensor
    Sensor sensor = (Sensor)parent.getItemAtPosition(position);
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.verbose,null);
    ((TextView)layout.findViewById(R.id.vendor_value)).setText(sensor.getVendor());
    ((TextView)layout.findViewById(R.id.power_value)).setText(sensor.getPower()+" mA");
    ((TextView)layout.findViewById(R.id.resolution_value)).setText(sensor.getResolution()+"");
    ((TextView)layout.findViewById(R.id.version_value)).setText(sensor.getVersion()+"");
    ((TextView)layout.findViewById(R.id.delay_value)).setText(sensor.getMinDelay()+" µS");
    ((TextView)layout.findViewById(R.id.range_value)).setText(sensor.getMaximumRange()+"");
    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(layout);
    
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(sensor.getName());
    int imageResource = R.drawable.ic_sensor_unknown;
    try {
      // Don't use a giant switch statement here to map types to image files. Besides saving
      // a lot of code, this solution also has the advantage that we can declare the
      // app to require a low API level while still being able to show icons for sensors that
      // came in later.
      imageResource = getResources().getIdentifier("drawable/ic_sensor_"+sensor.getType(), null, "de.onyxbits.sensorreadout");
      if (imageResource==0) {
        imageResource=R.drawable.ic_sensor_unknown;
      }
    }
    catch (Exception e) {}
    builder.setIcon(imageResource);
    builder.setView(scrollView);
    builder.create().show();
    
    return true;
  }
  
  public void onClick(DialogInterface di, int id) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.onyxbits.de"));
    startActivity(browserIntent);
  }
}
