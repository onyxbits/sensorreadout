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
    ((TextView)ret.findViewById(R.id.sensor_vendor)).setText(sensor.getVendor());
    ((TextView)ret.findViewById(R.id.sensor_power)).setText(sensor.getPower()+" mA");
            
    return ret;
  }
}