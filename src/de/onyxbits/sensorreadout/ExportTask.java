package de.onyxbits.sensorreadout;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import android.content.Intent;
import android.os.AsyncTask;

/**
 * Builds the CSV dump for sharing.
 * 
 * @author patrick
 * 
 */
class ExportTask extends AsyncTask<XYMultipleSeriesDataset, Integer, String> {

	private ReadoutActivity activity;

	public ExportTask(ReadoutActivity activity) {
		this.activity = activity;
	}

	@Override
	protected String doInBackground(XYMultipleSeriesDataset... params) {
		StringBuilder sb = new StringBuilder();
		XYSeries series[] = params[0].getSeries();
		int samples = series[0].getItemCount();
		for (int i = 0; i < samples; i++) {
			sb.append(i);
			sb.append(", ");
			sb.append(series[0].getY(i));
			if (series.length > 1) {
				sb.append(", ");
				sb.append(series[1].getY(i));
				if (series.length > 2) {
					sb.append(", ");
					sb.append(series[2].getY(i));
				}
			}
			sb.append("\n");
			publishProgress(10000 * i / samples);
		}
		return sb.toString();
	}

	@Override
	public void onPreExecute() {
		activity.setProgressBarVisibility(true);
	}

	@Override
	public void onProgressUpdate(Integer... values) {
		activity.setProgress(values[0]);
	}

	@Override
	public void onPostExecute(String result) {
		activity.setProgressBarVisibility(false);
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, result);
		sendIntent.setType("text/plain");
		activity.startActivity(Intent.createChooser(sendIntent, activity
				.getResources().getText(R.string.send_to)));
	}
}
