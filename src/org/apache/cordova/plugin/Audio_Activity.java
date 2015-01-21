package org.apache.cordova.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.nuatransmedia.FrontEndBuilder1.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class Audio_Activity extends Activity {

	public static String FileNameArg = "arg_filename";
	private static String filename = null;
	private File opt = null;
	private AnimationDrawable frameAnimation;
	private Button btn_play, btn_pause;
	private MediaPlayer mpa;
	private ImageView view;
	private Boolean mp_pause = false, boo_audioshow = false;
	private Context context;
	private Uri myUri = null; // "http://s3.amazonaws.com/iPhoneBooks/uploaded_videos/14222/NeroSoundTrax_test1_PCM_Stereo_CBR_16SS_6000Hz.wav?1418906984";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		Bundle bun = getIntent().getExtras();
		if (bun != null) {
			String u = bun.getString("url");
			myUri = Uri.parse(u);
			
			if (myUri != null) {
				context = this;
				btn_play = (Button) findViewById(R.id.mplay_playbtn);
				btn_pause = (Button) findViewById(R.id.mplay_pausebtn);
				view = (ImageView) findViewById(R.id.mplay_img);
				view.setBackgroundResource(R.drawable.animation_list);
				frameAnimation = (AnimationDrawable) view.getBackground();
				view.setVisibility(View.GONE);

				try {
					if (mpa != null) {
						// Log.d("recorder", "secondfalse");
						btn_play.setVisibility(View.GONE);
						btn_pause.setVisibility(View.VISIBLE);
						view.setVisibility(View.VISIBLE);
						mpa.seekTo(mpa.getCurrentPosition());
						mpa.start();
						mp_pause = true;
						frameAnimation.start();
						boo_audioshow = true;
						mpa.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {

								btn_play.setVisibility(View.VISIBLE);
								btn_pause.setVisibility(View.GONE);
								frameAnimation.stop();
								view.setVisibility(View.GONE);
								boo_audioshow = false;
								mpp();
								mp_pause = false;
							}

							private void mpp() {
								mpa = null;

							}
						});
					} else {
						btn_play.setVisibility(View.GONE);
						btn_pause.setVisibility(View.VISIBLE);
						view.setVisibility(View.VISIBLE);
						frameAnimation.start();
						mpa = new MediaPlayer();
						mpa.setDataSource(context, myUri);
						mpa.prepare();
						mpa.start();
						mp_pause = true;
						frameAnimation.start();
						boo_audioshow = true;

						mpa.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {

								btn_play.setVisibility(View.VISIBLE);
								btn_pause.setVisibility(View.GONE);
								frameAnimation.stop();
								view.setVisibility(View.GONE);
								boo_audioshow = false;
								mpp();
								mp_pause = false;
							}

							private void mpp() {
								mpa = null;

							}
						});
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				findViewById(R.id.mplay_btncls).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Audio_Activity.this.finish();
						if (boo_audioshow) {
							if (mpa != null) {
								if (mpa.isPlaying()) {
									btn_play.setVisibility(View.VISIBLE);
									btn_pause.setVisibility(View.GONE);
									view.setVisibility(View.GONE);
									frameAnimation.stop();
									mpa.pause();
									if (mp_pause) {
										Log.d("true", "mp_pause = true");
									} else {
										Log.d("true", "mp_pause = false");
										mpa.release();
										mpa = null;
									}
								}
							}
						}
					}
				});

				

				btn_play.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						try {
							if (mpa != null) {
								// Log.d("recorder", "secondfalse");
								btn_play.setVisibility(View.GONE);
								btn_pause.setVisibility(View.VISIBLE);
								view.setVisibility(View.VISIBLE);
								mpa.seekTo(mpa.getCurrentPosition());
								mpa.start();
								mp_pause = true;
								frameAnimation.start();
								boo_audioshow = true;
								mpa.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {

										btn_play.setVisibility(View.VISIBLE);
										btn_pause.setVisibility(View.GONE);
										frameAnimation.stop();
										view.setVisibility(View.GONE);
										boo_audioshow = false;
										mpp();
										mp_pause = false;
									}

									private void mpp() {
										mpa = null;

									}
								});
							} else {
								btn_play.setVisibility(View.GONE);
								btn_pause.setVisibility(View.VISIBLE);
								view.setVisibility(View.VISIBLE);
								frameAnimation.start();
								mpa = new MediaPlayer();
								mpa.setDataSource(context, myUri);
								mpa.prepare();
								mpa.start();
								mp_pause = true;
								frameAnimation.start();
								boo_audioshow = true;

								mpa.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {

										btn_play.setVisibility(View.VISIBLE);
										btn_pause.setVisibility(View.GONE);
										frameAnimation.stop();
										view.setVisibility(View.GONE);
										boo_audioshow = false;
										mpp();
										mp_pause = false;
									}

									private void mpp() {
										mpa = null;

									}
								});
							}
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				btn_pause.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						btn_play.setVisibility(View.VISIBLE);
						btn_pause.setVisibility(View.GONE);
						view.setVisibility(View.GONE);
						frameAnimation.stop();
						boo_audioshow = false;
						mpa.pause();
						mp_pause = false;
					}
				});

			}
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

		if (boo_audioshow) {
			if (mpa != null) {
				if (mpa.isPlaying()) {
					btn_play.setVisibility(View.VISIBLE);
					btn_pause.setVisibility(View.GONE);
					view.setVisibility(View.GONE);
					frameAnimation.stop();
					mpa.pause();
					if (mp_pause) {
						Log.d("true", "mp_pause = true");
					} else {
						Log.d("true", "mp_pause = false");
						mpa.release();
						mpa = null;
					}
				}
			}
		}
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (boo_audioshow) {
			if (mpa != null) {
				if (mpa.isPlaying()) {
					btn_play.setVisibility(View.VISIBLE);
					btn_pause.setVisibility(View.GONE);
					view.setVisibility(View.GONE);
					frameAnimation.stop();
					mpa.pause();
					if (mp_pause) {
						Log.d("true", "mp_pause = true");
					} else {
						Log.d("true", "mp_pause = false");
						mpa.release();
						mpa = null;
					}
				}
			}
		}
	}

}
