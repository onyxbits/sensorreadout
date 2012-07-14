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

import android.widget.*;
import android.view.*;
import android.content.*;
import android.hardware.*;
import java.util.*;

/**
 * <code>Adapter</code> for plugging a <code>Sensor</code> into a <code>ListView</code>
 */
class SensorAdapter extends ArrayAdapter<Sensor> {

  public SensorAdapter(Context context, int textViewResourceId, List<Sensor> sensors) {
    super(context, textViewResourceId, sensors);
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View ret;
    
    Sensor sensor = getItem(position);
    LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ret = inflater.inflate(R.layout.identifierplate,null);
    ((TextView)ret.findViewById(R.id.sensor_name)).setText(sensor.getName());
    
    TextView description = (TextView)ret.findViewById(R.id.sensor_description);
    ImageView icon = (ImageView)ret.findViewById(R.id.sensor_icon);
    
    switch(sensor.getType()) {
      // Bit of a dirty hack: Use numeric instead of symbolic constants since some have been
      // deprecated while others have been added in higher API levels. Going numeric causes
      // the least headaches for supporting all devices.
      case 1: {
        icon.setImageResource(R.drawable.ic_sensor_1);
        description.setText(R.string.sensor_desc_1);
        break;
      }
      case 2: {
        icon.setImageResource(R.drawable.ic_sensor_2);
        description.setText(R.string.sensor_desc_2);
        break;
      }
      case 3: {
        icon.setImageResource(R.drawable.ic_sensor_3);
        description.setText(R.string.sensor_desc_3);
        break;
      }
      case 4: {
        icon.setImageResource(R.drawable.ic_sensor_4);
        description.setText(R.string.sensor_desc_4);
        break;
      }
      case 5: {
        icon.setImageResource(R.drawable.ic_sensor_5);
        description.setText(R.string.sensor_desc_5);
        break;
      }
      case 6: {
        icon.setImageResource(R.drawable.ic_sensor_6);
        description.setText(R.string.sensor_desc_6);
        break;
      }
      case 7: {
        // Sensor.TYPE_TEMPERATURE has been deprectated -> use 13 instead
        icon.setImageResource(R.drawable.ic_sensor_13);
        description.setText(R.string.sensor_desc_13);
        break;
      }
      case 8: {
        icon.setImageResource(R.drawable.ic_sensor_8);
        description.setText(R.string.sensor_desc_8);
        break;
      }
      case 9: {
        icon.setImageResource(R.drawable.ic_sensor_9);
        description.setText(R.string.sensor_desc_9);
        break;
      }
      case 10: {
        icon.setImageResource(R.drawable.ic_sensor_10);
        description.setText(R.string.sensor_desc_10);
        break;
      }
      case 11: {
        icon.setImageResource(R.drawable.ic_sensor_11);
        description.setText(R.string.sensor_desc_11);
        break;
      }
      case 12: {
        icon.setImageResource(R.drawable.ic_sensor_12);
        description.setText(R.string.sensor_desc_12);
        break;
      }
      case 13: {
        icon.setImageResource(R.drawable.ic_sensor_13);
        description.setText(R.string.sensor_desc_13);
        break;
      }
      default: {
        // Defaults are already set in the XML file
      }
    }
    
    return ret;
  }
}