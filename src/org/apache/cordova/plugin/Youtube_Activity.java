package org.apache.cordova.plugin;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.nuatransmedia.FrontEndBuilder.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Youtube_Activity extends YouTubeBaseActivity implements
		YouTubePlayer.OnInitializedListener {
	public static final String API_KEY = "AIzaSyB2nbB2TqqO8YqcWrzB_eYPRc-kuEleoWQ";
	public static String VIDEO_ID = "null";
	public Context context;
	public static Boolean isInternetPresent = false;
	public static ConnectionDetector cd;
	
	@Override
	public void onBackPressed() {
		final Button cancel_btn, logout_btn;
		AlertDialog.Builder build_dialog;
		final AlertDialog alert_dialog;

		build_dialog = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.altdialog_videoexit,
				(ViewGroup) findViewById(R.id.videoexit_sample));
		cancel_btn = (Button) layout.findViewById(R.id.vdoext_btn_cancel);
		logout_btn = (Button) layout.findViewById(R.id.vdoext_btn_ok);
		build_dialog.setView(layout);
		alert_dialog = build_dialog.create();
		alert_dialog.setView(layout, 0, 0, 0, 0);

		cancel_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert_dialog.dismiss();

			}
		});

		logout_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert_dialog.dismiss();
				setResult(RESULT_OK);
				finish();
			}
		});

		alert_dialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_activity);
		context = this;
		YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubeplayerview);
		cd = new ConnectionDetector(context);
		isInternetPresent = cd.isConnectingToInternet();
		if (isInternetPresent) {
			Bundle bun = getIntent().getExtras();
			if (bun != null) {
				VIDEO_ID = bun.getString("youtube");
				youTubePlayerView.initialize(API_KEY, this);
			} else {
				toastsettext("This format could not support, Please try another");
			}
		} else {
			toastsettext("No Internet Connection");
		}
	}

	public void toastsettext(String string1) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_activity,
				(ViewGroup) findViewById(R.id.toast_rl));
		TextView txt = (TextView) layout.findViewById(R.id.toast_txt);
		txt.setText(string1);
		Toast tst = new Toast(getApplicationContext());
		tst.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		tst.setDuration(Toast.LENGTH_SHORT);
		tst.setView(layout);
		tst.show();
	}

	@Override
	public void onInitializationFailure(Provider provider,
			YouTubeInitializationResult result) {
		Toast.makeText(getApplicationContext(), "onInitializationFailure()",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onInitializationSuccess(Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			player.cueVideo(VIDEO_ID);
		}
	}

}
