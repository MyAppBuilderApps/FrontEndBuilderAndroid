package org.apache.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Echo_Video extends CordovaPlugin {

	public CallbackContext callbackContext1;
	public Context context;
	private String stng_img = null;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();
		callbackContext1 = callbackContext;
		if (action.equals("echo_video")) {
			stng_img = args.getString(0);
			if (stng_img != null) {
				Intent intent = new Intent(context, Video_Activity.class);
				Bundle bun = new Bundle();
				//bun.putString("img", "url");
				//bun.putString("vid", "/sdcard/videokit/final.mp4");
				bun.putString("img", "uri");
				bun.putString("vid", stng_img);
				intent.putExtras(bun);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.cordova.getActivity().startActivityForResult(intent, 111);
			}
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext1.sendPluginResult(progressResult);
		}
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 111) {
			if (resultCode == android.app.Activity.RESULT_OK) {
				String result = "";
				this.callbackContext1.success(result);
			} else {
				String message = "";
				this.callbackContext1.error(message);
			}
		}
	}

}
