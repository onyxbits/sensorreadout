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
