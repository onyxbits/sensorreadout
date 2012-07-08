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
 * Bug the user about rating the app after a reasonable amount of uses.
 */
public class AppRater implements DialogInterface.OnClickListener {

  private final static String APP_PNAME = "de.onyxbits.sensorreadout";
  
  private static final int DAYS_UNTIL_PROMPT = 2;
  private static final int LAUNCHES_UNTIL_PROMPT = 7;
  private static final String PREFSFILE = "apprater";
  
  private Context context;
  
  private AppRater(Context context) {
    this.context=context;
  }
  
  /**
   * Should be called on every <code>Activity.onCreate()</code>. Checks if the trial
   * period is over and if so brings up the dialog to rate the app
   * @param ctx The application context.
   */
  public static void appLaunched(Context ctx) {
    SharedPreferences prefs = ctx.getSharedPreferences(PREFSFILE, 0);
    if (prefs.getBoolean("dontshowagain", false)) { 
      return;
    }
    
    SharedPreferences.Editor editor = prefs.edit();
    
    // Increment launch counter
    long launch_count = prefs.getLong("launch_count", 0) + 1;
    editor.putLong("launch_count", launch_count);

    // Get date of first launch
    Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
    if (date_firstLaunch == 0) {
      date_firstLaunch = System.currentTimeMillis();
      editor.putLong("date_firstlaunch", date_firstLaunch);
    }
    editor.commit();
    
    // Wait at least n days before opening
    if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
      if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
        showRateDialog(ctx);
      }
    }
  }   
  
  /**
   * Just bring up the rating dialog without checking whether or not the trial perriod has ended
   * @param ctx application context
   */
  public static void showRateDialog(Context ctx) {
    AppRater rater = new AppRater(ctx);
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle("Rate this app?");
    builder.setIcon(R.drawable.ic_rate);
    builder.setMessage("Hi,\nseems like you find this app useful. Would you consider rating it? This will only take a minute of your time, but encourages further development.");
    builder.setPositiveButton("Sure!",rater);
    builder.setNegativeButton("No thanks",rater);
    builder.setNeutralButton("Later",rater);
    builder.create().show();
  }
  
  @Override
  public void onClick(DialogInterface di, int id) {
    SharedPreferences.Editor editor = context.getSharedPreferences(PREFSFILE, 0).edit();
    switch(id) {
      case DialogInterface.BUTTON_POSITIVE: {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        // Fall through
      }
      case DialogInterface.BUTTON_NEGATIVE: {
        editor.putBoolean("dontshowagain", true);
        break;
      }
      case DialogInterface.BUTTON_NEUTRAL:
      default: {
        // Did the user select "Later" because s/he does not have network access right
        // now or because s/he wants to extend the trial period? Let's assume the later
        // and reset the launch counter, as it is the less pesky option.
        editor.putLong("launch_count",0);
      }
    }
    editor.commit();
    di.dismiss();
  }
}
