package com.netcompss.ffmpeg4android_client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.plugin.Audio_Activity;
import org.apache.cordova.plugin.ScalingUtilities;
import org.apache.cordova.plugin.Video_Activity;
import org.apache.cordova.plugin.Video_Compress;
import org.apache.cordova.plugin.ScalingUtilities.ScalingLogic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netcompss.ffmpeg4android.IFfmpgefRemoteServiceBridge;
import com.netcompss.ffmpeg4android.LicenseCheckJNI;
import com.nuatransmedia.FrontEndBuilder1.R;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.Html;
import android.util.Log;
//import android.widget.Toast;

//import android.widget.Toast;

/**
 * This class use the Template Method design pattern, the invokeService()
 * virtual method is implemented at children level and called in this class
 * template method onServiceConnected()
 * 
 * @author ehasson
 * 
 */



public class BaseWizard extends CordovaPlugin {

	protected IFfmpgefRemoteServiceBridge remoteService;
	protected boolean started = false;
	protected RemoteServiceConnection conn = null;
	protected boolean invokeFlag = false;
	protected ProgressDialog progressDialog;

	protected int PICK_REQUEST_CODE = 0;

	protected static String workingFolder;
	protected String outputFile;
	protected String outputFilePath;
	protected String inputFilePath;
	protected Prefs _prefs = null;

	protected String commandStr, returnPath = null;

	private int srcBgId = R.drawable.bg;
	static int w = 80;
	static int h = 80;

	private String progressDialogMessage;
	private String progressDialogTitle;

	private int notificationIcon;
	private String notificationTitle = null;
	private String notificationMessage = null;
	private String notificationfinishedMessageTitle = null;
	private String notificationStoppedMessage = null;
	private String notificationfinishedMessageDesc = null;

	public void setNotificationfinishedMessageDesc(
			String notificationfinishedMessageDesc) {
		this.notificationfinishedMessageDesc = notificationfinishedMessageDesc;
	}

	protected String[] commandComplex;

	public String[] getCommandComplex() {
		return commandComplex;
	}

	public void setCommandComplex(String[] commandComplex) {
		this.commandComplex = commandComplex;
	}

	public String getNotificationfinishedMessageTitle() {
		return notificationfinishedMessageTitle;
	}

	public void setNotificationfinishedMessageTitle(
			String notificationfinishedMessage) {
		this.notificationfinishedMessageTitle = notificationfinishedMessage;
	}

	public String getNotificationStoppedMessage() {
		return notificationStoppedMessage;
	}

	public void setNotificationStoppedMessage(String notificationStoppedMessage) {
		this.notificationStoppedMessage = notificationStoppedMessage;
	}

	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
	}

	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public void setNotificationIcon(int notificationIcon) {
		this.notificationIcon = notificationIcon;
	}

	public String getProgressDialogMessage() {
		return progressDialogMessage;
	}

	public void setProgressDialogMessage(String progressDialogMessage) {
		this.progressDialogMessage = progressDialogMessage;
	}

	public String getProgressDialogTitle() {
		return progressDialogTitle;
	}

	public void setProgressDialogTitle(String progressDialogTitle) {
		this.progressDialogTitle = progressDialogTitle;
	}

	public String getCommand() {
		return commandStr;
	}

	public void setCommand(String commandStr) {
		Log.i(Prefs.TAG, "Command is set");
		this.commandStr = commandStr;
	}

	public static String getWorkingFolder() {
		return workingFolder;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	private void setRemoteNotificaitonIcon() {
		if (notificationIcon != -1)
			Prefs.setRemoteNotificationIconId(this.cordova.getActivity()
					.getApplicationContext(), notificationIcon);
	}

	private void setRemoteNotificationInfo() {
		try {
			if (remoteService != null) {
				Log.i(Prefs.TAG, "setting remote notification info");
				if (notificationTitle != null)
					remoteService.setNotificationTitle(notificationTitle);
				if (notificationMessage != null)
					remoteService.setNotificationMessage(notificationMessage);
			} else {
				Log.w(Prefs.TAG,
						"remoteService is null, can't set remote notification info");
			}
		} catch (RemoteException e1) {
			Log.w(Prefs.TAG, e1.getMessage(), e1);
		}
	}

	// called from onServiceConnected
	public void invokeService() {

		Log.i(Prefs.TAG, "invokeService called");

		// needed for demo client for grasfull fail
		int rc = isLicenseValid(this.cordova.getActivity()
				.getApplicationContext());

		if (rc < 0) {
			String message = "";
			if (rc == -1)
				message = "Trial expired, please contact support@netcompss.com";
			else
				message = "License Validation failed, please contact support@netcompss.com";

			/*
			 * new AlertDialog.Builder(this)
			 * .setTitle("License Validation failed") .setMessage(message)
			 * .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			 * public void onClick(DialogInterface dialog, int which) {
			 * 
			 * } }) .show();
			 */

			return;
		}

		setRemoteNotificationInfo();

		// this call with handle license gracefully.
		// If it will be removed, the fail will be in the native code, causing
		// the progress dialog to start.
		// if (! isLicenseValid()) return;

		if (invokeFlag) {
			if (conn == null) {
				// Toast.makeText(this, "Cannot invoke - service not bound",
				// Toast.LENGTH_SHORT).show();
			} else {
				try {
					String command = getCommand();

					if (remoteService != null) {
						if (command != null)
							remoteService.setFfmpegCommand(command);
						else {
							remoteService
									.setComplexFfmpegCommand(commandComplex);
						}
						remoteService.setWorkingFolder(Prefs.getWorkFolder());
						runWithCommand(command);

					} else {
						Log.w(Prefs.TAG,
								"Invoke failed, remoteService is null.");
					}

				} catch (android.os.DeadObjectException e) {
					Log.d(Prefs.TAG,
							"ignoring DeadObjectException (FFmpeg process exit)");
				} catch (RemoteException re) {
					Log.e(Prefs.TAG, re.getMessage(), re);
				}
			}
			invokeFlag = false;
		} else {
			Log.d(Prefs.TAG, "Not invoking");

		}
	}

	protected boolean invokeFileInfoServiceFlag = false;
	private TranscodeBackground _transcodeBackground;

	public void getInputFileAndOutputFileFromCommand(String workingFolder,
			String inputFileName) {

	}

	public IFfmpgefRemoteServiceBridge getRemoteService() {
		return remoteService;
	}

	public void setWorkingFolder(String workingFolder) {
		Prefs.setWorkFolder(workingFolder);
	}

	public int isLicenseValid(Context ctx) {
		LicenseCheckJNI lm = new LicenseCheckJNI();
		int rc = lm.licenseCheck(Prefs.getWorkingFolderForNative(), ctx);
		if (rc < 0) {
			/*
			 * if (rc == -1) // Toast.makeText(this,
			 * "Trail Expired. contact support.", Toast.LENGTH_SHORT).show();
			 * else if (rc == -2) // Toast.makeText(this,
			 * "License invalid contact support", Toast.LENGTH_SHORT).show();
			 * else // Toast.makeText(this,
			 * "License check failed. contact support." + rc,
			 * Toast.LENGTH_SHORT).show();
			 */}

		return rc;

	}

	public void runTranscoing() {
		setRemoteNotificaitonIcon();
		releaseService();
		stopService();
		startService();
		invokeFlag = true;
		bindService();
	}

	public void runWithCommand(String command) {
		Prefs p = new Prefs();
		p.setContext(this.cordova.getActivity().getApplicationContext());

		deleteLogs();
		FileUtils.writeToLocalLog("command: " + command);
		FileUtils.writeToLocalLog("Input file size: " + Prefs.inputFileSize);
		Log.d(Prefs.TAG, "Client invokeService()");
		Random rand = new Random();
		int randInt = rand.nextInt(1000);
		_transcodeBackground = new TranscodeBackground(this, remoteService,
				randInt);
		_transcodeBackground.setProgressDialogTitle(progressDialogTitle);
		_transcodeBackground.setProgressDialogMessage(progressDialogMessage);
		_transcodeBackground.setNotificationIcon(notificationIcon);
		_transcodeBackground
				.setNotificationfinishedMessageTitle(notificationfinishedMessageTitle);
		_transcodeBackground
				.setNotificationfinishedMessageDesc(notificationfinishedMessageDesc);
		_transcodeBackground
				.setNotificationStoppedMessage(notificationStoppedMessage);
		_transcodeBackground.execute();
	}

	public void copyLicenseAndDemoFilesFromAssetsToSDIfNeeded() {
		_prefs = new Prefs();
		_prefs.setContext(this.cordova.getActivity().getApplicationContext());
		File destVid = null;
		File destLic = null;
		// String workingFolderPath = Environment.getExternalStorageDirectory()
		// + Prefs.WORKING_DIRECTORY;
		String workingFolderPath = Prefs.getWorkFolder();
		Log.i(Prefs.TAG, "workingFolderPath: " + workingFolderPath);
		try {
			if (!FileUtils.checkIfFolderExists(workingFolderPath)) {

				boolean isFolderCreated = FileUtils
						.createFolder(workingFolderPath);
				Log.i(Prefs.TAG, workingFolderPath + " created? "
						+ isFolderCreated);
				if (isFolderCreated) {
					destVid = new File(workingFolderPath + "in.mp4");
					Log.i(Prefs.TAG,
							"Adding vid file at " + destVid.getAbsolutePath());
					InputStream is = this.cordova.getActivity()
							.getApplication().getAssets().open("in.mp4");
					BufferedOutputStream o = null;
					try {
						byte[] buff = new byte[10000];
						int read = -1;
						o = new BufferedOutputStream(new FileOutputStream(
								destVid), 10000);
						while ((read = is.read(buff)) > -1) {
							o.write(buff, 0, read);
						}
						Log.i(Prefs.TAG, "Copy " + destVid.getAbsolutePath()
								+ " from assets to SDCARD finished succesfully");
					} catch (Exception e) {
						Log.w(Prefs.TAG,
								"Failed copying: " + destVid.getAbsolutePath());
					} finally {
						is.close();
						if (o != null)
							o.close();

					}

					// Toast.makeText(this, "Demo video created at: " +
					// workingFolderPath , Toast.LENGTH_SHORT).show();
				} else {
					// Toast.makeText(
					// this.cordova.getActivity().getApplicationContext(),
					// "Working folder was not created, You need SDCARD to use this app!",
					// Toast.LENGTH_LONG).show();
				}

			} else {
				Log.d(Prefs.TAG,
						"Working directory exists, not coping assests (license file and demo videos)");
				// Toast.makeText(
				// this.cordova.getActivity().getApplicationContext(),
				// "Sample videos located at: " + workingFolderPath,
				// Toast.LENGTH_SHORT).show();
			}

			// Creation of output directory is removed, since when using custom
			// working folder
			// it created the _prefs.getOutFolder() which is by default
			// /sdcard/videokit
			/*
			 * if (!FileUtils.checkIfFolderExists(_prefs.getOutFolder())) {
			 * boolean isFolderCreated =
			 * FileUtils.createFolder(_prefs.getOutFolder()); Log.i(Prefs.TAG,
			 * _prefs.getOutFolder() + " created? " + isFolderCreated); } else {
			 * Log.d(Prefs.TAG, "output directory exists.");
			 * 
			 * }
			 */
		} catch (FileNotFoundException e) {
			Log.e(Prefs.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(Prefs.TAG, e.getMessage());
		}

		// ========= License Copy
		// ===================================================================
		InputStream is = null;
		BufferedOutputStream o = null;
		boolean copyLic = true;
		try {
			is = this.cordova.getActivity().getApplication().getAssets()
					.open("ffmpeglicense.lic");
		} catch (Exception e) {
			Log.i(Prefs.TAG, "License file does not exist in the assets.");
			copyLic = false;
		}

		if (copyLic) {
			destLic = new File(workingFolderPath + "ffmpeglicense.lic");
			Log.i(Prefs.TAG, "Adding lic file at " + destLic.getAbsolutePath());

			o = null;
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destLic),
						10000);
				while ((read = is.read(buff)) > -1) {
					o.write(buff, 0, read);
				}
				Log.i(Prefs.TAG, "Copy " + destLic.getAbsolutePath()
						+ " from assets to SDCARD finished succesfully");
			} catch (Exception e) {
				Log.e(Prefs.TAG,
						"Error when coping license file from assets to working folder: "
								+ e.getMessage());
			} finally {
				try {
					is.close();
					if (o != null)
						o.close();
				} catch (IOException e) {
					Log.w(Prefs.TAG,
							"Error when closing license file io: "
									+ e.getMessage());
				}

			}

		} else {
			Log.i(Prefs.TAG, "Not coping license");
		}

		// ========License Copy
		// ======================================================================

	}

	protected void startService() {
		if (started) {
			// Toast.makeText(this.cordova.getActivity().getApplicationContext(),
			// "Service already started", Toast.LENGTH_SHORT).show();
		} else {

			Intent i = new Intent(
					"com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
			PackageManager packageManager = this.cordova.getActivity()
					.getPackageManager();
			List<ResolveInfo> services = packageManager.queryIntentServices(i,
					0);
			Log.i(Prefs.TAG,
					"!!!!!!!!!!!!!!!!!!services.size(): " + services.size());

			if (services.size() > 0) {
				ResolveInfo service = services.get(0);
				i.setClassName(service.serviceInfo.packageName,
						service.serviceInfo.name);
				i.setAction("com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");

				if (!invokeFileInfoServiceFlag) {
					i.addCategory("Base");
					Log.i(Prefs.TAG, "putting Base categoty");
				} else {
					i.addCategory("Info");
					Log.i(Prefs.TAG, "putting Info categoty");
				}

				ComponentName cn = this.cordova.getActivity().startService(i);
				Log.d(Prefs.TAG, "started: " + cn.getClassName());
			}

			started = true;
			Log.d(Prefs.TAG, "Client startService()");
		}

	}

	// this is not working, not stopping the remote service.
	protected void stopService() {
		Log.d(Prefs.TAG, "Client stopService()");
		// Intent i = new Intent();
		Intent i = new Intent(
				"com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
		// i.setClassName("com.netcompss.ffmpeg4android",
		// "com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
		this.cordova.getActivity().stopService(i);
		started = false;
	}

	protected void bindService() {
		Log.d(Prefs.TAG, " bindService() called");
		if (conn == null) {
			conn = new RemoteServiceConnection();
			// Intent i = new Intent();
			Intent i = new Intent(
					"com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
			// i.setClassName("com.netcompss.ffmpeg4android",
			// "com.netcompss.ffmpeg4android.FFMpegRemoteServiceBridge");
			this.cordova.getActivity().bindService(i, conn,
					Context.BIND_AUTO_CREATE);
			Log.d(Prefs.TAG, "Client bindService()");
		} else {
			Log.d(Prefs.TAG, " Client Cannot bind - service already bound");
			// Toast.makeText(this,
			// "Client Cannot bind - service already bound",
			// Toast.LENGTH_SHORT).show();
		}
	}

	protected void releaseService() {
		if (conn != null) {
			this.cordova.getActivity().unbindService(conn);
			conn = null;
			Log.d(Prefs.TAG, "releaseService()");
		} else {
			// Toast.makeText(this, "Client Cannot unbind - service not bound",
			// Toast.LENGTH_SHORT).show();
			Log.d(Prefs.TAG, "Client Cannot unbind - service not bound");
		}
	}

	public class RemoteServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className,
				IBinder boundService) {
			Log.d(Prefs.TAG, "Client onServiceConnected()");
			remoteService = IFfmpgefRemoteServiceBridge.Stub
					.asInterface((IBinder) boundService);

			if (invokeFileInfoServiceFlag)
				invokeFileInfoService(inputFilePath);
			else
				invokeService();

		}

		public void onServiceDisconnected(ComponentName className) {
			remoteService = null;
			Log.d(Prefs.TAG, "onServiceDisconnected");
		}
	};

	public void handleServiceFinished() {
		Log.i(Prefs.TAG, "FFMPEG finished.");
		// Toast.makeText(this.cordova.getActivity().getApplicationContext(),
		// "notif_message_ok", Toast.LENGTH_LONG).show();

		// remove the sticky notification
		// fix 4.4.2 bug, should not effect other versions.
		releaseService();
		stopService();

	}

	protected void handleInfoServiceFinished() {
		Log.i(Prefs.TAG, "FFMPEG finished (info).");
		// removeDialog(FILE_INFO_DIALOG);
		// showDialog(FILE_INFO_DIALOG);
		invokeFileInfoServiceFlag = false;

	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = this.cordova.getActivity().managedQuery(contentUri,
				proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void invokeFileInfoService(String inputFilePath) {
		Log.i(Prefs.TAG, "invokeFileInfoService called");

		if (invokeFlag) {

			if (conn == null) {
				// Toast.makeText(
				// this.cordova.getActivity().getApplicationContext(),
				// "Cannot invoke - service not bound", Toast.LENGTH_SHORT)
				// .show();
			} else {
				try {
					// FileUtils.deleteFile(workingFolder + outputFile);
					String command = "ffmpeg -i " + inputFilePath;
					if (remoteService != null) {
						deleteLogs();
						FileUtils.writeToLocalLog("command: " + command);
						Log.i(Prefs.TAG, "command: " + command);
						remoteService.setFfmpegCommand(command);
						Log.d(Prefs.TAG, "Client invokeService()");
						remoteService.runTranscoding();
					} else {
						Log.w(Prefs.TAG,
								"Invoke failed, remoteService is null.");
					}

				} catch (android.os.DeadObjectException e) {

					Log.d(Prefs.TAG,
							"ignoring DeadObjectException (FFmpeg process exit)");

				} catch (RemoteException re) {
					Log.e(Prefs.TAG, re.getMessage(), re);
				}
			}
			handleInfoServiceFinished();
			invokeFlag = false;
		} else {
			Log.d(Prefs.TAG, "Not invoking");

		}
	}

	public void deleteLogs() {
		FileUtils.deleteFile(Prefs.getVkLogFilePath());
		FileUtils.deleteFile(Prefs.getFfmpeg4androidLogFilePath());
		FileUtils.deleteFile(Prefs.getVideoKitLogFilePath());
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
		this.outputFile = FileUtils.getFileNameFromFilePath(outputFilePath);
	}

	public void stopTranscoding() {
		Log.d(Prefs.TAG, "stopTranscoding called");
		if (_transcodeBackground != null) {
			_transcodeBackground.forceCancel();
		}
	}

	public void uploadtoserver() {
		try {
			JSONArray arry = new JSONArray(jsondata);
			RequestParams params = new RequestParams();
			JSONObject obj = arry.getJSONObject(2);

			if (obj.has("button_id")) {
				Log.d("buttonid", String.valueOf(obj.getInt("button_id")));
				params.put("button_id", String.valueOf(obj.getInt("button_id")));
			}

			if (obj.has("id")) {
				Log.d("id", obj.getString("id"));
				params.put("id", obj.getString("id"));
			}

			if (obj.has("title")) {
				Log.d("title", obj.getString("title"));
				params.put("title", obj.getString("title").toString());
			}

			if (obj.has("text")) {
				Log.d("text", obj.getString("text"));
				String txt = obj.getString("text").toString()
						.replace("<p>", "").replace("</p>", "");
				params.put("text", txt);
				// Toast.makeText(context, txt, Toast.LENGTH_LONG).show();
			}
			// Html.fromHtml(obj.getString("description")).toString());
			if (obj.has("description")) {
				Log.d("description", obj.getString("description"));
				params.put("description", obj.getString("description")
						.toString());
			}

			if (obj.has("api_key")) {
				Log.d("api_key", obj.getString("api_key"));
				params.put("api_key", obj.getString("api_key"));
			}

			if (obj.has("video")) {
				Log.d("video", "/sdcard/videokit/final.mp4");
				params.put("video", new File("/sdcard/videokit/final.mp4"));
			}

			// video_thumbnail
			if (obj.has("video_thumbnail")) {
				String image = obj.getString("video_thumbnail");
				if (image != null) {
					Log.d("image", image);
					if (image.contains("content") || image.contains("file")) {
						String getString = getFileNameByUri(context,
								Uri.parse(image));
						String thumb = reporteds(getString);
						params.put("video_thumbnail", new File(thumb));
					} else {
						String thumb = reporteds(image);
						params.put("video_thumbnail", new File(thumb));
					}
				} else {
					String thumb = reporteds(video_thumbpath.getAbsolutePath());
					params.put("video_thumbnail", new File(thumb));
					Log.d("image path32", video_thumbpath.getAbsolutePath());
				}
			} else {
				String thumb = reporteds(video_thumbpath.getAbsolutePath());
				params.put("video_thumbnail", new File(thumb));
				Log.d("image path33", video_thumbpath.getAbsolutePath());
			}

			AsyncHttpClient client = new AsyncHttpClient();
			// client.setTimeout(5000);
			client.addHeader("Accept", "appliction/json");
			Log.d("api", arry.getString(0));
			Log.d("method", arry.getString(1));
			if (arry.getString(1).equalsIgnoreCase("post")) {
				client.post(arry.getString(0).replace("?", ""), params,
						responseHandler);
			} else if (arry.getString(1).equalsIgnoreCase("put")) {
				client.put(arry.getString(0).replace("?", ""), params,
						responseHandler);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	String strMyImagePath = null;
	File f = null;
	@SuppressWarnings("unused")
	private String reporteds(String path) {

		FileOutputStream fos = null;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
		Matrix matrix = new Matrix();
		if (orientation == 6) {
			matrix.postRotate(90);
		} else if (orientation == 3) {
			matrix.postRotate(180);
		} else if (orientation == 8) {
			matrix.postRotate(270);
		}

		if (path != null) {
			Bitmap unbgbtmp = ScalingUtilities.decodeResource(
					context.getResources(), srcBgId, w, h, ScalingLogic.FIT);
			Bitmap bgbtmp = ScalingUtilities.createScaledBitmap(unbgbtmp, w, h,
					ScalingLogic.FIT);
			unbgbtmp.recycle();
			Bitmap unrlbtmp = ScalingUtilities.decodeFile(path, w, h,
					ScalingLogic.FIT);
			Bitmap rlbtmp = ScalingUtilities.createScaledBitmap(unrlbtmp, w, h,
					ScalingLogic.FIT);
			unrlbtmp.recycle();
			Bitmap newscaledBitmap = ProcessingBitmapTwo(bgbtmp, rlbtmp);
			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/MAB");
			if (!mFolder.exists()) {
				mFolder.mkdir();
			}else{
				mFolder.delete();
				mFolder.mkdir();
			}

			String s = "myappbuilderthumb.png";

			f = new File(mFolder.getAbsolutePath(), s);

			strMyImagePath = f.getAbsolutePath();
			Log.d("f", strMyImagePath);
			try {
				fos = new FileOutputStream(f);
				newscaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}
			return strMyImagePath;
		} else {
			return path;
		}

	}
	
	
	public String createVideoThumbnail(String fDescriptor) 
	 {
	    Bitmap thumb = null;
	    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	    try {
	        retriever.setDataSource(fDescriptor);
	        thumb = retriever.getFrameAtTime(-1);
	        int width = thumb.getWidth();
	        int height = thumb.getHeight();
	        int max = Math.max(width, height);
	        if (max > 512) {
	            float scale = 512f / max;
	            int w = Math.round(scale * width);
	            int h = Math.round(scale * height);
	            thumb = Bitmap.createScaledBitmap(thumb, w, h, true);
	        }
	        
			FileOutputStream fos = null;
			String extr = Environment.getExternalStorageDirectory()
					.toString();
			File mFolder = new File(extr + "/MYAPPBUILDER");
			if (!mFolder.exists()) {
				mFolder.mkdir();
			}else{
				mFolder.delete();
				mFolder.mkdir();
			}

			String s = "myappbuilderthumbs.png";

			video_thumbpath = new File(mFolder.getAbsolutePath(), s);
			returnPath = video_thumbpath.getAbsolutePath();

			try {
				fos = new FileOutputStream(video_thumbpath);
				thumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}
		
	    } 
	    catch (IllegalArgumentException ex) {
	        // Assume this is a corrupt video file
	        Log.e("e", "Failed to create video thumbnail for file description: " + fDescriptor.toString());
	    }
	    catch (RuntimeException ex) {
	        // Assume this is a corrupt video file.
	        Log.e("e", "Failed to create video thumbnail for file description: " + fDescriptor.toString());
	    } finally {
	        try {
	            retriever.release();
	        } catch (RuntimeException ex) {
	            // Ignore failures while cleaning up.
	        }
	    }
		return returnPath;
	 }


	

	public AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFinish() {
			super.onFinish();
			Log.d("arg", "finish");
		}

		@Override
		public void onSuccess(String arg0) {
			// TODO Auto-generated method stub
			super.onSuccess(arg0);
			Log.e("mesage", arg0);
			//String path = createVideoThumbnail("/sdcard/videokit/final.mp4");
			//if(path != null){
				//PluginResult progressResult = new PluginResult(
				//PluginResult.Status.OK, arg0 + "thumbpath=" + returnPath);
				//progressResult.setKeepCallback(true);
				//BaseWizard.this.callbackContext.sendPluginResult(progressResult);
			//}else{
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.OK, arg0 );
			progressResult.setKeepCallback(true);
			BaseWizard.this.callbackContext.sendPluginResult(progressResult);
			//}
		}

		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			if (arg1 != null) {
				Log.d("aa", arg1);
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR, arg1);
				progressResult.setKeepCallback(true);
				BaseWizard.this.callbackContext
						.sendPluginResult(progressResult);
			}
		};

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(Prefs.TAG, "BaseWizard onDestroy");
		stopTranscoding();
	}

	/*
	 * ["http:\/\/build.myappbuilder.com\/api\/elements\/create_video.json?",
	 * "post", {"button_id":2882,"title":"ryghh",
	 * "video":{"fileSize":35583,"fileName"
	 * :"final.mp4","lastModifiedDate":"2014-09-24T14:45:33.000Z"
	 * ,"type":"video\/mp4","size":35583,"name":"final.mp4"},
	 * "description":"hvffjgg","api_key":"d1f3d280dbdd89eeacda0077e100ba67"} ]
	 * ddddd 3
	 */
	String Tag = "Video_Compress";
	String jsondata;
	String post_imgpth = null;
	public CallbackContext callbackContext;
	public Context context;
	public int wid = 80, hig = 80;

	public boolean execute(String actiosaveImagen, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		context = this.cordova.getActivity().getApplicationContext();
		jsondata = args.toString();
		Log.e("json", args.toString() + " ddddd " + args.length());
		if (args != null && args.length() > 0) {
			JSONObject obj = args.getJSONObject(2).getJSONObject("video");
			String[] temp = null;// args.getString(0).split("/");
			temp = getVideoPath(obj.getString("fileName"));
			if (temp[0].equalsIgnoreCase("success")) {
				File file = new File(temp[1]);
				String hrSize = "";
				long size = file.length();
				double m = size / 1048576;
				;
				if (m < 32) {
					copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
					commandStr = "ffmpeg -y -i "
							+ temp[1]
							+ " -strict experimental -s 160x120 -r 25 -aspect 4:3 -vcodec mpeg4 -b 97152 -ab 48000 -ac 2 -ar 22050 /sdcard/videokit/final.mp4";
					setWorkingFolder("/sdcard/videokit/");
					setCommand(commandStr);
					setOutputFilePath("/sdcard/videokit/final.mp4");

					Log.i(Prefs.TAG,
							"ffmpeg4android library version: "
									+ Prefs.getLibraryVersionName());
					runTranscoing();
				} else {
					JSONObject object = new JSONObject();
					try {
						object.put("statues", "Error");
						object.put("message",
								"file size should not greater than 30MB....");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					PluginResult progressResult = new PluginResult(
							PluginResult.Status.ERROR, object);
					progressResult.setKeepCallback(true);
					callbackContext.sendPluginResult(progressResult);
				}
			} else {
				PluginResult progressResult = new PluginResult(
						PluginResult.Status.ERROR, temp[0]);
				progressResult.setKeepCallback(true);
				callbackContext.sendPluginResult(progressResult);
			}
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR,
					"parameter required or incorrect");
			progressResult.setKeepCallback(true);
			callbackContext.sendPluginResult(progressResult);
		}

		return true;// super.execute(action, args, callbackContext);
	}

	File video_thumbpath = null;

	private String[] getVideoPath(String name) {
		// TODO Auto-generated method stub
		Log.e(Tag, "file name:" + name);
		Uri mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] cloumns = { MediaStore.Video.VideoColumns.DATA,
				MediaStore.Video.VideoColumns._ID,
				MediaStore.Video.VideoColumns.TITLE,
				MediaStore.Video.Thumbnails.DATA };
		String selection = MediaStore.Video.Media.DATA + " Like '%" + name
				+ "'";
		Cursor videocursor = BaseWizard.this.cordova.getActivity()
				.managedQuery(mUri, cloumns, selection, null, null);
		if (videocursor.getCount() > 0) {
			videocursor.moveToFirst();
			while (!videocursor.isAfterLast()) {
				String path = videocursor.getString(0);
				String thumbpath = videocursor.getString(3);
				Log.e(Tag, "path:" + path + "  thumb:" + thumbpath);
				String[] temp = path.split("/");
				if (temp[temp.length - 1].equalsIgnoreCase(name)) {
					Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
							MediaStore.Images.Thumbnails.MINI_KIND);
					FileOutputStream fos = null;
					String extr = Environment.getExternalStorageDirectory()
							.toString();
					File mFolder = new File(extr + "/MYAPPBUILDER");
					if (!mFolder.exists()) {
						mFolder.mkdir();
					}else{
						mFolder.delete();
						mFolder.mkdir();
					}

					String s = "myappbuilder.png";

					video_thumbpath = new File(mFolder.getAbsolutePath(), s);
					//returnPath = video_thumbpath.getAbsolutePath();

					try {
						fos = new FileOutputStream(video_thumbpath);
						thumb.compress(Bitmap.CompressFormat.PNG, 100, fos);
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {

						e.printStackTrace();
					} catch (Exception e) {

						e.printStackTrace();
					}
					return new String[] { "success", path };
				}
				videocursor.moveToNext();
			}

		} else {
			return new String[] { "error", "Video not Founded..." };
		}
		return new String[] { "error", "Video not Founded..." };
	}

	private Bitmap ProcessingBitmapTwo(Bitmap bm1, Bitmap bm2) {
		Bitmap newBitmap = null;

		int w;
		if (bm1.getWidth() >= bm2.getWidth()) {
			w = bm1.getWidth();
		} else {
			w = bm2.getWidth();
		}

		int h;
		if (bm1.getHeight() >= bm2.getHeight()) {
			h = bm1.getHeight();
		} else {
			h = bm2.getHeight();
		}

		Config config = bm1.getConfig();
		if (config == null) {
			config = Bitmap.Config.ARGB_4444;
		}

		newBitmap = Bitmap.createBitmap(w, h, config);
		Canvas newCanvas = new Canvas(newBitmap);
		newCanvas.drawColor(Color.WHITE);
		if (bm2.getWidth() == bm2.getHeight()) {
			newCanvas.drawBitmap(bm2, (w - bm2.getWidth()) / 2,
					(h - bm2.getHeight()) / 2, null);
		} else {
			newCanvas.drawBitmap(bm2, (w / 2) - (bm2.getWidth() / 2), (h / 2)
					- (bm2.getHeight() / 2), null);
		}

		return newBitmap;
	}

	

	public String getFileNameByUri(Context context, Uri uri) {
		String fileName = "unknown";// default fileName
		Uri filePathUri = uri;
		if (uri.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor = context.getContentResolver().query(uri, null, null,
					null, null);
			if (cursor.moveToFirst()) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				filePathUri = Uri.parse(cursor.getString(column_index));
				fileName = filePathUri.getPath();
			}
			Log.d("content", "content");
		} else if (uri.getScheme().compareTo("file") == 0) {
			fileName = filePathUri.getPath();
			Log.d("file", "file");
		} else {
			fileName = filePathUri.getPath();
			Log.d("else", "else");
		}

		return fileName;

	}

}
