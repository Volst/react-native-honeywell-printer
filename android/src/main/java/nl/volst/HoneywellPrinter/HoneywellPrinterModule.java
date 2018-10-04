package nl.volst.HoneywellPrinter;

import java.lang.reflect.Method;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.os.AsyncTask;
import android.content.res.AssetManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import static nl.volst.HoneywellPrinter.HoneywellPrinterPackage.TAG;

import com.honeywell.mobility.print.LabelPrinter;
import com.honeywell.mobility.print.LabelPrinterException;
import com.honeywell.mobility.print.PrintProgressEvent;
import com.honeywell.mobility.print.PrintProgressListener;

@SuppressWarnings("unused")
public class HoneywellPrinterModule extends ReactContextBaseJavaModule {

	// Debugging
	private static final boolean D = true;

	private ReactApplicationContext mReactContext;

	public HoneywellPrinterModule(ReactApplicationContext reactContext) {
		super(reactContext);

		mReactContext = reactContext;
	}

	@Override
	public String getName() {
		return "HoneywellPrinter";
	}

	/*******************************/
	/** Methods Available from JS **/
	/*******************************/

	@ReactMethod
	public void print(String printerID, String macAddress, String text, final Promise promise) {
		// Create a PrintTask to do printing on a separate thread.
		PrintTask task = new PrintTask();

		// Executes PrintTask with the specified parameter which is passed
		// to the PrintTask.doInBackground method.
		task.execute(printerID, macAddress, text);
		promise.resolve(true);
	}


	/**
	 * This class demonstrates printing in a background thread and updates
	 * the UI in the UI thread.
	 */
	public class PrintTask extends AsyncTask<String, Integer, String> {

		/**
		 * Runs on the UI thread before doInBackground(Params...).
		 */
		@Override
		protected void onPreExecute()
		{
			//
		}

		/**	
		 * This method runs on a background thread. The specified parameters
		 * are the parameters passed to the execute method by the caller of
		 * this task. This method can call publishProgress to publish updates
		 * on the UI thread.
		 */
		@Override
		protected String doInBackground(String... args)
		{
			AssetManager manager = mReactContext.getAssets();
			byte[] buffer = null;
			try {

				InputStream stream = manager.open("printerprofiles.json");
				int size = stream.available();
				buffer = new byte[size];
				stream.read(buffer);
				stream.close();
			} catch (IOException e) {
				if (D) Log.d(TAG, "Printer profile file error: " + e.getMessage());
			}
			String profiles = new String(buffer).trim();

			LabelPrinter lp = null;
			String sResult = null;
			String sPrinterID = args[0];
			String sPrinterURI = "bt://" + args[1];
			String sText = args[2];
			if (D) Log.d(TAG, "Printing to printer id " + sPrinterID + " with uri " + sPrinterURI + " and text " + sText);

			LabelPrinter.ExtraSettings exSettings = new LabelPrinter.ExtraSettings();
			exSettings.setContext(mReactContext);

			try
			{
				lp = new LabelPrinter(
						profiles,
						sPrinterID,
						sPrinterURI,
						exSettings);

				// Registers to listen for the print progress events.
				lp.addPrintProgressListener(new PrintProgressListener() {
					@Override
					public void receivedStatus(PrintProgressEvent aEvent)
					{
						// Publishes updates on the UI thread.
						publishProgress(aEvent.getMessageType());
					}
				});

				// A retry sequence in case the bluetooth socket is temporarily not ready
				int numtries = 0;
				int maxretry = 10;
				while(numtries < maxretry)
				{
					try
					{
						lp.connect();  // Connects to the printer
						break;
					}
					catch (LabelPrinterException ex)
					{
						numtries++;
						Thread.sleep(1000);
					}
				}
				if (numtries == maxretry) lp.connect();//Final retry

				// Sets up the variable dictionary.
				LabelPrinter.VarDictionary varDataDict = new LabelPrinter.VarDictionary();
				varDataDict.put("TextMsg", sText);

				// Prints the ItemLabel as defined in the printer_profiles.JSON file.
				lp.writeLabel("ItemLabel", varDataDict);

				sResult = "Number of bytes sent to printer: " + lp.getBytesWritten();
			}
			catch (LabelPrinterException ex)
			{
				sResult = "LabelPrinterException: " + ex.getMessage();
			}
			catch (Exception ex)
			{
				if (ex.getMessage() != null)
					sResult = "Unexpected exception: " + ex.getMessage();
				else
					sResult = "Unexpected exception.";
			}
			finally
			{
				if (lp != null)
				{
					try
					{
						// Notes: To ensure the data is transmitted to the printer
						// before the connection is closed, both PB22_Fingerprint and
						// PB32_Fingerprint printer entries specify a PreCloseDelay setting
						// in the printer_profiles.JSON file included with this sample.
						lp.disconnect();  // Disconnects from the printer
						lp.close();  // Releases resources
					}
					catch (Exception ex) {}
				}
			}

			if (D) Log.d(TAG, sResult);
			// The result string will be passed to the onPostExecute method
			// for display in the the Progress and Status text box.
			return sResult;
		}

		/**
		 * Runs on the UI thread after publishProgress is invoked. The
		 * specified values are the values passed to publishProgress.
		 */
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			// TODO
		}

		/**
		 * Runs on the UI thread after doInBackground method. The specified
		 * result parameter is the value returned by doInBackground.
		 */
		@Override
		protected void onPostExecute(String result)
		{
			//
		}
	} //endofclass PrintTask
}
