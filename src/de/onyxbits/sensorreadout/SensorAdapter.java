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
    ImageView icon = (ImageView)ret.findViewById(R.id.sensor_icon);
    switch (sensor.getType()) {
      case Sensor.TYPE_ACCELEROMETER: {
        icon.setImageResource(R.drawable.ic_sensor_accelerometer);
        break;
      }
      case Sensor.TYPE_TEMPERATURE: {
        // Deprecated -> Fall through
      }
      case Sensor.TYPE_AMBIENT_TEMPERATURE: {
        icon.setImageResource(R.drawable.ic_sensor_ambient_temperature);
        break;
      }
      case Sensor.TYPE_GRAVITY: {
        icon.setImageResource(R.drawable.ic_sensor_gravity);
        break;
      }
      case Sensor.TYPE_GYROSCOPE: {
        icon.setImageResource(R.drawable.ic_sensor_gyroscope);
        break;
      }
      case Sensor.TYPE_LIGHT: {
        icon.setImageResource(R.drawable.ic_sensor_light);
        break;
      }
      case Sensor.TYPE_LINEAR_ACCELERATION: {
        icon.setImageResource(R.drawable.ic_sensor_linear_acceleration);
        break;
      }
      case Sensor.TYPE_MAGNETIC_FIELD: {
        icon.setImageResource(R.drawable.ic_sensor_magnetic_field);
        break;
      }
      case Sensor.TYPE_PRESSURE: {
        icon.setImageResource(R.drawable.ic_sensor_pressure);
        break;
      }
      case Sensor.TYPE_PROXIMITY: {
        icon.setImageResource(R.drawable.ic_sensor_proximity);
        break;
      }
      case Sensor.TYPE_RELATIVE_HUMIDITY: {
        icon.setImageResource(R.drawable.ic_sensor_relatice_humidity);
        break;
      }
      case Sensor.TYPE_ROTATION_VECTOR: {
        icon.setImageResource(R.drawable.ic_sensor_rotation_vector);
        break;
      }
    }
            
    return ret;
  }
}