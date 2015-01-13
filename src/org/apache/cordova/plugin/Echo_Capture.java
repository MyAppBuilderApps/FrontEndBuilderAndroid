package org.apache.cordova.plugin;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Echo_Capture extends CordovaPlugin {

	public static CallbackContext callbackContext1;
	public Context context;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		callbackContext1 = callbackContext;
		Constant.videoCapture = null;
		Start("");
		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		result.setKeepCallback(true);
		callbackContext1.sendPluginResult(result);
		return true;
	}

	private void Start(String string) {
		
		Intent intent = new Intent(context, Gallery_Activity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
	}

	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d("result", "result");
		if (requestCode == 0) {
			try {
				PluginResult result = null;
				Log.d("result", "result");
				if (resultCode == android.app.Activity.RESULT_OK) {
					Toast.makeText(context, "calledded", Toast.LENGTH_LONG).show();
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("frame", Constant.febCapture);
					jsonObj.put("video", Constant.videoCapture);
					callbackContext1.success(jsonObj);
					result = new PluginResult(PluginResult.Status.OK, jsonObj);
				} else {
					if (intent != null) {
						if (intent.hasExtra("result")) {
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("frame", "null");
							jsonObj.put("video", Constant.videoCapture);
							Toast.makeText(context, "failure",
									Toast.LENGTH_LONG).show();
							callbackContext1.error(jsonObj);
							result = new PluginResult(
									PluginResult.Status.ERROR, jsonObj);
						}
					}

				}
				if (result != null) {
					result.setKeepCallback(false);
					if (callbackContext1 != null) {
						callbackContext1.sendPluginResult(result);
						callbackContext1 = null;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}*/
}
