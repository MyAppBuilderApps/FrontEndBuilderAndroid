package org.apache.cordova.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.plugin.ScalingUtilities.ScalingLogic;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
//import android.widget.Toast;
import com.squareup.okhttp.internal.Base64;

public class Echo_Threes extends CordovaPlugin {

	public static boolean boo_result_three = false;
	public static CallbackContext callbackContext3;
	public Context context;
	private String file_getter3 = null, file_setter3 = null,file_ = null,
			file_filename3 = null;
	private File file3 = null;
	private Bitmap scaledBitmap_three = null;
	private String strMyImagePath3 = null;
	public static int SCALE = 25;
	private int rswidth = 0, rsheight = 0;
	private String stng_img = null, stng_method = null, stng_api = null;
	private String[] stng_ary = null;

	private JSONObject obj;
	private String TAG = "TAG";

	// ["320","460","splash_image","C:\\fakepath\\20140905_154637.jpg","http:\/\/build.myappbuilder.com\/api\/apps\/settings\/general.json?","put",
	// {"bar_button_color":"light","title":"apptest","description":"<p
	// style=\"text-align:
	// center;\">Dgui<\/p>","api_key":"f538901f8804a3ad9d322d1cade66495","domain":"buildap.ps","subdomain":"fgg","button_color":"energized","bar_color":"balanced"}]
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity().getApplicationContext();

		if (action.equals("echo_three")) {
			Log.e("jsonarray", args.toString());
			callbackContext3 = callbackContext;
			rswidth = Integer.parseInt(args.getString(0));
			rsheight = Integer.parseInt(args.getString(1));
			stng_img = args.getString(2);
			file_filename3 = args.getString(3);
			stng_api = args.getString(4);
			stng_method = args.getString(5);
			obj = args.getJSONObject(6);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Log.d("new", file_filename3);
				file_getter3 = getKitKat(Uri.parse(file_filename3));
				Log.d("new", file_getter3);
				if (file_getter3 != null) {
					ExifInterface exif = null;
					try {
						exif = new ExifInterface(file_getter3);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					int orientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION,
							ExifInterface.ORIENTATION_NORMAL);
					Matrix matrix = new Matrix();
					if (orientation == 6) {
						matrix.postRotate(90);
					} else if (orientation == 3) {
						matrix.postRotate(180);
					} else if (orientation == 8) {
						matrix.postRotate(270);
					}

					Bitmap rotatedBitmap = null;
					int DESIREDWIDTH = rswidth, DESIREDHEIGHT = rsheight;

					Bitmap scaledBitmap_three = BitmapFactory
							.decodeFile(file_getter3);
					if (scaledBitmap_three != null) {
						rotatedBitmap = Bitmap.createBitmap(scaledBitmap_three,
								0, 0, scaledBitmap_three.getWidth(),
								scaledBitmap_three.getHeight(), matrix, true);

						Bitmap unscaledBitmap_three = ScalingUtilities
								.decodeFile(rotatedBitmap, DESIREDWIDTH,
										DESIREDHEIGHT, ScalingLogic.FIT);
						Bitmap scaled = ScalingUtilities.createScaledBitmap(
								unscaledBitmap_three, DESIREDWIDTH,
								DESIREDHEIGHT, ScalingLogic.FIT);
						Bitmap newscaledBitmap_three = getResizedBitmap(scaled,
								DESIREDHEIGHT, DESIREDWIDTH);
						createBitmap(newscaledBitmap_three);
					} else {
						PluginResult progressResult = new PluginResult(
								PluginResult.Status.ERROR, "Try Again Later");
						progressResult.setKeepCallback(true);
						callbackContext3.sendPluginResult(progressResult);
					}

				} else {
					PluginResult progressResult = new PluginResult(
							PluginResult.Status.ERROR, "Try Again Later");
					progressResult.setKeepCallback(true);
					callbackContext3.sendPluginResult(progressResult);
				}

			} else {
				Log.d("new", file_filename3);
				if (file_filename3.contains("content")
						|| file_filename3.contains("file")) {
					getFileNameByUri(context, Uri.parse(file_filename3));
				} else {
					try {
						String file_getter = saveImage(file_filename3);
						if (file_getter != null) {
							decodeFile(file_getter, rswidth, rsheight);
						} else {
							PluginResult progressResult = new PluginResult(
									PluginResult.Status.ERROR, "Try Again Later");
							progressResult.setKeepCallback(true);
							callbackContext.sendPluginResult(progressResult);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			return true;
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
		}
		return false;
	}

	private String saveImage(String b64String) throws InterruptedException,
			JSONException {
		File file3 = null;
		try {

			FileOutputStream fos = null;
			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/MYAPPBUILDER");
			if (!mFolder.exists()) {
				mFolder.mkdir();
			}

			String s = "myappbuilders.png";

			file3 = new File(mFolder.getAbsolutePath(), s);

			// Decode Base64 back to Binary format
			byte[] decodedBytes = Base64.decode(b64String.getBytes());

			// Save Binary file to phone
			file3.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file3);
			fOut.write(decodedBytes);
			fOut.close();


		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return file3.getPath();
	}

	private void createBitmap(Bitmap newscaledBitmap_three) {
		FileOutputStream fos = null;
		String extr = Environment.getExternalStorageDirectory().toString();
		File mFolder = new File(extr + "/MYAPPBUILDER");
		if (!mFolder.exists()) {
			mFolder.mkdir();
		}

		String s = "myappbuilder.png";

		file3 = new File(mFolder.getAbsolutePath(), s);

		strMyImagePath3 = file3.getAbsolutePath();
		Log.d("f", strMyImagePath3);
		Log.d("main", file_filename3);
		try {
			fos = new FileOutputStream(file3);
			newscaledBitmap_three.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (strMyImagePath3 == null) {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.OK, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
		} else {
			Bitmap btmp = BitmapFactory.decodeFile(strMyImagePath3);
			Log.d("final width",
					String.valueOf(btmp.getHeight() + " , final Height ="
							+ btmp.getWidth()));
			File files = new File(strMyImagePath3);
			try {
				imagePost(file3);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@SuppressLint("NewApi")
	public String getKitKat(Uri uri) {
		String fileName = "unknown";// default fileName
		Uri filePathUri = uri;
		if (uri.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor = context.getContentResolver().query(uri, null, null,
					null, null);
			cursor.moveToFirst();
			String document_id = cursor.getString(0);
			document_id = document_id
					.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();

			cursor = context
					.getContentResolver()
					.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							null, MediaStore.Images.Media._ID + " = ? ",
							new String[] { document_id }, null);
			cursor.moveToFirst();
			fileName = cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			cursor.close();
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


	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

	private String decodeFile(String path, int DESIREDWIDTH, int DESIREDHEIGHT) {

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

		//Bitmap rotatedBitmap = null;

		Bitmap scaledBitmap_three = BitmapFactory.decodeFile(path);
		if (scaledBitmap_three != null) {
			/*rotatedBitmap = Bitmap.createBitmap(scaledBitmap_three, 0, 0,
					scaledBitmap_three.getWidth(),
					scaledBitmap_three.getHeight(), matrix, true);*/

			Bitmap unscaledBitmap_three = ScalingUtilities.decodeFile(
					scaledBitmap_three, DESIREDWIDTH, DESIREDHEIGHT,
					ScalingLogic.FIT);
			Bitmap scaled = ScalingUtilities.createScaledBitmap(
					unscaledBitmap_three, DESIREDWIDTH, DESIREDHEIGHT,
					ScalingLogic.FIT);
			Bitmap newscaledBitmap_three = getResizedBitmap(scaled,
					DESIREDHEIGHT, DESIREDWIDTH);

			String extr = Environment.getExternalStorageDirectory().toString();
			File mFolder = new File(extr + "/MYAPPBUILDER");
			if (!mFolder.exists()) {
				mFolder.mkdir();
			}

			String s = "myappbuilder.png";

			file3 = new File(mFolder.getAbsolutePath(), s);

			strMyImagePath3 = file3.getAbsolutePath();
			try {
				fos = new FileOutputStream(file3);
				newscaledBitmap_three.compress(Bitmap.CompressFormat.PNG, 100,
						fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}

		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.OK, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
			return path;
		}

		if (strMyImagePath3 == null) {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.OK, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
			return path;
		} else {
			Bitmap btmp = BitmapFactory.decodeFile(strMyImagePath3);
			Log.d("final width",
					String.valueOf(btmp.getHeight() + " , final Height ="
							+ btmp.getWidth()));
			///exit();
			/*File files = new File(strMyImagePath3);
			if (!files.exists()) {
				Toast.makeText(context, "no", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "yes", Toast.LENGTH_LONG).show();
			}*/
			try {
				imagePost(file3);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return strMyImagePath3;

	}

	private void imagePost(File f) throws JSONException, FileNotFoundException {

		RequestParams params = new RequestParams();
		if (obj != null) {
			if (obj.has("bar_button_color")) {
				params.put("bar_button_color",
						obj.getString("bar_button_color"));
			}
			if (obj.has("button_id")) {
				params.put("button_id", obj.getString("button_id"));
			}
			if (obj.has("id")) {
				params.put("id", obj.getString("id"));
			}
			if (obj.has("title")) {
				params.put("title", obj.getString("title"));
			}

			if (obj.has("text")) {
				params.put("text", obj.getString("text").toString());
			}
			if (obj.has("description")) {
				params.put("description",
						(obj.getString("description")).toString());
			}
			// Html.fromHtml(obj.getString("description")).toString());
			if (obj.has("api_key")) {
				params.put("api_key", obj.getString("api_key"));
			}
			if (obj.has("subdomain")) {
				params.put("subdomain", obj.getString("subdomain"));
			}
			if (obj.has("domain")) {
				params.put("domain", obj.getString("domain"));
			}
			if (obj.has("bar_color")) {
				params.put("bar_color", obj.getString("bar_color"));
			}
			if (obj.has("button_color")) {
				params.put("button_color", obj.getString("button_color"));
			}
			String Path1 = f.getAbsolutePath();

			params.put(stng_img, f);

		}

		AsyncHttpClient client = new AsyncHttpClient();
	//	client.addHeader("Content-type", "multipart/form-data");
	//	client.addHeader("Content-type",
			//	"application/x-www-form-urlencoded; charset=utf-8");
		client.addHeader("Accept", "appliction/json");
	//	client.addHeader("Content-Transfer-Encoding", "binary");

		Log.d("f", strMyImagePath3);
		Log.d("main", file_filename3);

		if (stng_method.contentEquals("post")) {
			client.post(stng_api, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String arg0) {
					super.onSuccess(arg0);
					Log.d("create success folder", arg0);
					Log.d("from success", "get json");
					/*PluginResult progressResult = new PluginResult(
							PluginResult.Status.OK, arg0);
					progressResult.setKeepCallback(true);
					callbackContext3.sendPluginResult(progressResult);
					Log.d("from success", "from success");*/
					

				}

				@Override
				public void onFinish() {
					super.onFinish();
					Log.d("from finish", "from finish");
				}

				@Override
				public void onFailure(Throwable arg0, String arg1) {

					if (arg0.getCause() instanceof ConnectTimeoutException) {
						// Toast.makeText(context, "Connection timeout !",
						// Toast.LENGTH_LONG).show();
					}
					if(arg1 != null){
						Log.d("Current failure post method", arg1);
						PluginResult progressResult = new PluginResult(
								PluginResult.Status.ERROR, arg1);
						progressResult.setKeepCallback(true);
						callbackContext3.sendPluginResult(progressResult);
					}
					Log.d("onFailure", "onFailure");
					super.onFailure(arg0, arg1);
				}

			});
		} else if (stng_method.contentEquals("put")) {
			client.put(stng_api, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String arg0) {
					super.onSuccess(arg0);
					Log.d("create success folder", arg0);
					Log.d("from success", "get json");
					//exit();
					PluginResult progressResult = new PluginResult(
							PluginResult.Status.OK, arg0);
					progressResult.setKeepCallback(true);
					callbackContext3.sendPluginResult(progressResult);
					// Toast.makeText(context, "post response",
					// Toast.LENGTH_LONG)
					// .show();

					Log.d("from success", "from success");
				}

				@Override
				public void onFinish() {
					super.onFinish();
					Log.d("from finish", "from finish");
					// Toast.makeText(context, "post finish", Toast.LENGTH_LONG)
					// .show();

				}

				@Override
				public void onFailure(Throwable arg0, String arg1) {

					if (arg0.getCause() instanceof ConnectTimeoutException) {
						// Toast.makeText(context, "Connection timeout !",
						// Toast.LENGTH_LONG).show();
					}
					// Toast.makeText(context, "post failure",
					// Toast.LENGTH_LONG)
					// .show();

					if(arg1 != null){
						Log.d("Current failure put method", arg1);
						PluginResult progressResult = new PluginResult(
								PluginResult.Status.ERROR, arg1);
						progressResult.setKeepCallback(true);
						callbackContext3.sendPluginResult(progressResult);
					}
					Log.d("onFailure", "onFailure");
					super.onFailure(arg0, arg1);
				}

			});
		}

	}
	
	public void getFileNameByUri(Context context, Uri uri) {
		String fileName = "unknown";// default fileName
		Uri filePathUri = uri;
		String names = null;
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
		File files = new File(fileName);
		if (fileName != null) {
			file_ = fileName;
			decodeFile(fileName, rswidth, rsheight);
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
		}
		
		
		if (fileName != null) {
			decodeFile(fileName, rswidth, rsheight);
		} else {
			PluginResult progressResult = new PluginResult(
					PluginResult.Status.ERROR, "Try Again Later");
			progressResult.setKeepCallback(true);
			callbackContext3.sendPluginResult(progressResult);
		}
	}

}

// function(e){alert(e);},"Echo", "echo", ["640", "480",
// document.getElementById("videoUpload").value])

