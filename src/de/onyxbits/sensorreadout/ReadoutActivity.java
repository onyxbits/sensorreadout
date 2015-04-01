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
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.hardware.*;
import android.graphics.*;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.*;
import android.content.res.*;

import org.achartengine.*;
import org.achartengine.model.*;
import org.achartengine.renderer.*;

/**
 * <code>Activity</code> that displays the readout of one <code>Sensor</code>.
 * This <code>Activity</code> must be started with an <code>Intent</code> that
 * passes in the number of the <code>Sensor</code> to display. If none is
 * passed, the first available <code>Sensor</code> is used.
 */
public class ReadoutActivity extends Activity implements View.OnTouchListener {

	/**
	 * For passing the index number of the <code>Sensor</code> in its
	 * <code>SensorManager</code>
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
	private GraphicalView chartView;

	/**
	 * Dataset of the graphing component
	 */
	private XYMultipleSeriesDataset sensorData;

	/**
	 * Renderer for actually drawing the graph
	 */
	private XYMultipleSeriesRenderer renderer;

	/**
	 * Our <code>SensorManager</code>
	 */
	private SensorManager sensorManager;

	/**
	 * Data channels. Corresponds to <code>SensorEvent.values</code>. Individual
	 * channels may be set to null to indicate that they must not be painted.
	 */
	private XYSeries channel[];

	/**
	 * The ticker thread takes care of updating the UI
	 */
	private Thread ticker;

	/**
	 * For moving the viewport of the graph
	 */
	private int xTick = 0;

	/**
	 * For moving the viewport of the grpah
	 */
	private int lastMinX = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.readout, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		int idx = getIntent().getIntExtra(SENSORINDEX, 0);
		sensor = sensorManager.getSensorList(Sensor.TYPE_ALL).get(idx);
		setTitle(sensor.getName());

		sensorData = new XYMultipleSeriesDataset();
		renderer = new XYMultipleSeriesRenderer();
		renderer.setGridColor(Color.DKGRAY);
		renderer.setShowGrid(true);
		renderer.setXAxisMin(0.0);
		renderer.setXTitle(getString(R.string.samplerate, 1000 / SAMPLERATE));
		renderer.setXAxisMax(10000 / (1000 / SAMPLERATE)); // 10 seconds wide
		renderer.setXLabels(10); // 1 second per DIV
		renderer.setChartTitle(" ");
		renderer.setYLabelsAlign(Paint.Align.RIGHT);
		chartView = ChartFactory.getLineChartView(this, sensorData, renderer);
		chartView.setOnTouchListener(this);
		float textSize = new TextView(this).getTextSize();
		float upscale = textSize / renderer.getLegendTextSize();
		renderer.setLabelsTextSize(textSize);
		renderer.setLegendTextSize(textSize);
		renderer.setChartTitleTextSize(textSize);
		renderer.setAxisTitleTextSize(textSize);
		renderer.setFitLegend(true);
		int[] margins = renderer.getMargins();
		margins[0] *= upscale;
		margins[1] *= upscale;
		margins[2] = (int) (2 * renderer.getLegendTextSize());
		renderer.setMargins(margins);
		setContentView(R.layout.readout_pending);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Lock the screen to its current rotation. Some sensors become impossible
		// to read otherwise.
		switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_PORTRAIT: {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			}
			case Configuration.ORIENTATION_LANDSCAPE: {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			}
		}

		if (xTick == 0) {
			ticker = new Ticker(this);
			ticker.start();
			sensorManager.registerListener((SensorEventListener) ticker, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopSampling();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.share: {
				stopSampling();
				new ExportTask(this).execute(sensorData);
				break;
			}
			case R.id.restart: {
				stopSampling();
				startActivity(getIntent());
				finish();
				break;
			}
			case R.id.info: {
				stopSampling();
				try {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(getString(R.string.homepage_sensor,sensor.getType())));
					startActivity(browserIntent);
				}
				catch (ActivityNotFoundException e) {
					Toast.makeText(this,R.string.no_webbrowser_installed,Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// channel!=null -> need to ensure that configure() has been called before
		// the user is allowed to abort (can happen by an accidental doubletap on
		// app start). Otherwise the screen will just stay black, making the app
		// appear to hang.
		if (v == chartView && ticker != null && channel != null) {
			// Design decision: When the user pans the view, s/he will (likely) no
			// longer see the point of data entry. We might as well stop sampling
			// then, since the user will (likely) not want to bother finding that
			// point again if it is still moving. Follow up design decision: Stopping
			// is final. We don't provide a resume option. Doing so would only add
			// complexity to the code/app for the sole purpose of producing a faulty
			// graph. If the user wants to continue, s/he has to return to the
			// OverviewActivity and restart from there.
			stopSampling();
		}
		return v.onTouchEvent(event);
	}

	/**
	 * Periodically called by the ticker
	 * 
	 * @param currentEvent
	 *          current sensor data.
	 */
	protected void onTick(SensorEvent currentEvent) {

		if (xTick == 0) {
			// Dirty, but we only learn a few things after getting the first event.
			configure(currentEvent);
			setContentView(chartView);
		}

		if (xTick > renderer.getXAxisMax()) {
			renderer.setXAxisMax(xTick);
			renderer.setXAxisMin(++lastMinX);
		}

		fitYAxis(currentEvent);

		for (int i = 0; i < channel.length; i++) {
			if (channel[i] != null) {
				channel[i].add(xTick, currentEvent.values[i]);
			}
		}

		xTick++;

		switch (currentEvent.accuracy) {
			case SensorManager.SENSOR_STATUS_ACCURACY_HIGH: {
				renderer.setChartTitle(getString(R.string.sensor_accuracy_high));
				break;
			}
			case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: {
				renderer.setChartTitle(getString(R.string.sensor_accuracy_medium));
				break;
			}
			case SensorManager.SENSOR_STATUS_ACCURACY_LOW: {
				renderer.setChartTitle(getString(R.string.sensor_accuracy_low));
				break;
			}
			default: {
				renderer.setChartTitle(getString(R.string.sensor_accuracy_unreliable));
				break;
			}
		}
		chartView.repaint();
	}

	/**
	 * Stop sampling
	 */
	private void stopSampling() {
		try {
			sensorManager.unregisterListener((SensorEventListener) ticker);
			ticker.interrupt();
			ticker.join();
			ticker = null;
			Toast.makeText(this, R.string.msg_stopped, Toast.LENGTH_SHORT).show();
		}
		catch (Exception e) {
		}
	}

	/**
	 * Make sure the Y axis is large enough to display the graph
	 * 
	 * @param event
	 *          current event
	 */
	private void fitYAxis(SensorEvent event) {
		double min = renderer.getYAxisMin(), max = renderer.getYAxisMax();
		for (int i = 0; i < channel.length; i++) {
			if (event.values[i] < min) {
				min = event.values[i];
			}
			if (event.values[i] > max) {
				max = event.values[i];
			}
		}
		float sum = 0;
		for (int i = 0; i < event.values.length; i++) {
			sum += event.values[i];
		}
		double half = 0;
		if (xTick == 0 && sum == event.values[0] * event.values.length) {
			// If the plot flatlines on the first event, we can't grade the Y axis.
			// This is especially bad if the sensor does not change without a
			// stimulus. the graph will then flatline on the x-axis where it is
			// impossible to be seen.
			half = event.values[0] * 0.5 + 1;
		}
		renderer.setYAxisMax(max + half);
		renderer.setYAxisMin(min - half);
	}

	/**
	 * Final configuration step. Must be called between receiving the first
	 * <code>SensorEvent</code> and updating the graph for the first time.
	 * 
	 * @param event
	 *          the event
	 */
	private void configure(SensorEvent event) {
		String[] channelNames = new String[event.values.length];
		channel = new XYSeries[event.values.length];
		for (int i = 0; i < channelNames.length; i++) {
			channelNames[i] = getString(R.string.channel_default) + i;
		}

		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				renderer.setYTitle(getString(R.string.unit_acceleration));
				break;
			}
			case Sensor.TYPE_GRAVITY: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				renderer.setYTitle(getString(R.string.unit_acceleration));
				break;
			}
			case Sensor.TYPE_GYROSCOPE: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				renderer.setYTitle(getString(R.string.unit_gyro));
				break;
			}
			case Sensor.TYPE_LIGHT: {
				channel = new XYSeries[1];
				channelNames[0] = getString(R.string.channel_light);
				renderer.setYTitle(getString(R.string.unit_light));
				break;
			}
			case Sensor.TYPE_LINEAR_ACCELERATION: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				renderer.setYTitle(getString(R.string.unit_acceleration));
				break;
			}
			case Sensor.TYPE_MAGNETIC_FIELD: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				renderer.setYTitle(getString(R.string.unit_magnetic));
				break;
			}
			case Sensor.TYPE_PRESSURE: {
				channel = new XYSeries[1];
				channelNames[0] = getString(R.string.channel_pressure);
				renderer.setYTitle(getString(R.string.unit_pressure));
				break;
			}
			case Sensor.TYPE_PROXIMITY: {
				channel = new XYSeries[1];
				channelNames[0] = getString(R.string.channel_distance);
				renderer.setYTitle(getString(R.string.unit_distance));
				break;
			}
			case Sensor.TYPE_ROTATION_VECTOR: {
				channelNames[0] = getString(R.string.channel_x_axis);
				channelNames[1] = getString(R.string.channel_y_axis);
				channelNames[2] = getString(R.string.channel_z_axis);
				break;
			}
			case Sensor.TYPE_ORIENTATION: {
				channelNames[0] = getString(R.string.channel_azimuth);
				channelNames[1] = getString(R.string.channel_pitch);
				channelNames[2] = getString(R.string.channel_roll);
				break;
			}
			case 7:
			case 13: {
				// Dirty hack: TYPE_TEMPERATURE became deprecated in favour of
				// TYPE_AMBIENT_TEMPERATURE. By
				// using the numeric instead of the symbolic constants, we can easily
				// compile for pre- and
				// post API level 14.
				renderer.setYTitle(getString(R.string.unit_temperature));
				break;
			}
		}

		int[] colors = {
				Color.RED,
				Color.YELLOW,
				Color.BLUE,
				Color.GREEN,
				Color.MAGENTA,
				Color.CYAN };
		for (int i = 0; i < channel.length; i++) {
			channel[i] = new XYSeries(channelNames[i]);
			sensorData.addSeries(channel[i]);
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i % colors.length]);
			renderer.addSeriesRenderer(r);
		}
	}

}