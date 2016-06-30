package com.zbar.lib;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.scan.R;
import com.zbar.lib.camera.CameraManager;
import com.zbar.lib.decode.CaptureActivityHandler;
import com.zbar.lib.decode.InactivityTimer;

/**
 * 二维码扫描画面
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
public class CaptureActivity extends Activity implements Callback {

	/** 声音大小 */
	private static final float BEEP_VOLUME = 0.50f;

	/** 震动周期 */
	private static final long VIBRATE_DURATION = 200L;

	/** 返回按钮 */
	private Button btnBack;

	/** 扫描画面Handler */
	private CaptureActivityHandler handler;

	/** 是否有SurfaceView */
	private boolean hasSurface;

	/** 计时器 */
	private InactivityTimer inactivityTimer;

	/** 媒体播放器 */
	private MediaPlayer mediaPlayer;

	/** 是否播放音频 */
	private boolean playBeep;

	/** 是否震动音频 */
	private boolean vibrate;

	/** 截取的x坐标 */
	private int x = 0;

	/** 截取的y坐标 */
	private int y = 0;

	/** 截取的区域宽度 */
	private int cropWidth = 0;

	/** 截取的区域高度 */
	private int cropHeight = 0;

	/** 扫描区域 */
	private RelativeLayout captureContainter;

	/** 扫描框布局 */
	private RelativeLayout captureCropLayout;

	/** 是否截取扫描的二维码图片 */
	private boolean isNeedCapture = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 扫描画面
		setContentView(R.layout.layout_qrcode_scan);

		// 初始化 CameraManager
		CameraManager.init(getApplication());

		// 默认没有SurfaceView
		hasSurface = false;

		// 初始化计时器
		inactivityTimer = new InactivityTimer(this);

		// 获取扫描区域
		captureContainter = (RelativeLayout) findViewById(R.id.captureContainter);

		// 获取扫描框布局
		captureCropLayout = (RelativeLayout) findViewById(R.id.captureCropLayout);

		// 扫描线
		ImageView captureScanLine = (ImageView) findViewById(R.id.captureScanLine);
		// 扫描线的动画
		TranslateAnimation mAnimation = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
		// 动画持续时间
		mAnimation.setDuration(1500);
		// 无限循环
		mAnimation.setRepeatCount(-1);
		// 来回扫描
		mAnimation.setRepeatMode(Animation.REVERSE);
		// 动画速率：匀速
		mAnimation.setInterpolator(new LinearInterpolator());
		// 设置动画
		captureScanLine.setAnimation(mAnimation);

		// 获取返回按钮
		btnBack = (Button) findViewById(R.id.btnBack);

		// 返回按钮添加点击监听
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 本画面结束
				finish();
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {

		super.onResume();

		// 相机预览控件（SurfaceView）
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capturePreview);

		// SurfaceHolder
		SurfaceHolder surfaceHolder = surfaceView.getHolder();

		// 已经初始化过相机预览控件
		if (hasSurface) {
			// 初始化相机
			initCamera(surfaceHolder);
		} else {
			// 调用接口初始化相机预览控件
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		// 播放声音
		playBeep = true;

		// 铃声
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		// 响铃类型不是标准
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			// 不播放声音
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Handler退出
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}

		// 相机关闭
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {

		// 计时器停止
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 解码
	 * 
	 * @param qrCodeString
	 *            解码后的文字
	 */
	public void handleDecode(String qrCodeString) {

		inactivityTimer.onActivity();

		// 播放声音及震动手机
		playBeepSoundAndVibrate();

		// 将解码信息传递给前画面
		Intent data = new Intent();
		// bundle
		Bundle bundle = new Bundle();
		// 解码信息
		bundle.putString("qrCodeString", qrCodeString);
		// 设置bundle
		data.putExtras(bundle);
		// 通知前画面
		setResult(RESULT_OK, data);

		// 本画面结束
		finish();

		// 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
		// handler.sendEmptyMessage(R.id.restart_preview);
	}

	/**
	 * 初始化照相机
	 * 
	 * @param surfaceHolder
	 *            SurfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {

		try {

			// 打开相机
			CameraManager.get().openDriver(surfaceHolder);

			// 预览图
			Point point = CameraManager.get().getCameraResolution();

			// 预览图的宽度，也即camera的分辨率宽度
			int width = point.y;
			// 预览图的高度，也即camera的分辨率高度
			int height = point.x;

			/**************************************************************/
			// x： 预览图中二维码图片的左上顶点x坐标，也就是手机中相机预览中看到的待扫描二维码的位置的x坐标
			// y： 预览图中二维码图片的左上顶点y坐标，也就是手机中相机预览中看到的待扫描二维码的位置的y坐标
			// cropHeight： 预览图中二维码图片的高度
			// cropWidth： 预览图中二维码图片的宽度
			// height：预览图的高度，也即camera的分辨率高度
			// width： 预览图的宽度，也即camera的分辨率宽度
			//
			// captureCropLayout.getLeft()： 布局文件中扫描框的左上顶点x坐标
			// captureCropLayout.getTop() 布局文件中扫描框的左上顶点y坐标
			// captureCropLayout.getHeight()： 布局文件中扫描框的高度
			// captureCropLayout.getWidth()： 布局文件中扫描框的宽度
			// captureContainter.getHeight()：布局文件中相机预览控件的高度
			// captureContainter.getWidth()： 布局文件中相机预览控件的宽度
			//
			// 其中存在这样一个等比例公式：
			//
			// x / width = captureCropLayout.getLeft() /
			// captureContainter.getWidth();
			// y / height = captureCropLayout.getTop() /
			// captureContainter.getHeight();
			// cropWidth / width = captureCropLayout.getWidth() /
			// captureContainter.getWidth();
			// cropHeight / height = captureCropLayout.getHeight() /
			// captureContainter.getHeight();
			//
			// 即：
			//
			// x = captureCropLayout.getLeft() * width /
			// captureContainter.getWidth() ;
			// y = captureCropLayout.getTop() * height /
			// captureContainter.getHeight() ;
			// cropWidth = captureCropLayout.getWidth() * width /
			// captureContainter.getWidth() ;
			// cropHeight = captureCropLayout.getHeight() * height /
			// captureContainter.getHeight() ;
			/**************************************************************/

			// 获取预览图中二维码图片的左上顶点x坐标
			int x = captureCropLayout.getLeft() * width
					/ captureContainter.getWidth();
			// 预览图中二维码图片的左上顶点y坐标
			int y = captureCropLayout.getTop() * height
					/ captureContainter.getHeight();

			// 获取预览图中二维码图片的宽度
			int cropWidth = captureCropLayout.getWidth() * width
					/ captureContainter.getWidth();
			// 预览图中二维码图片的高度
			int cropHeight = captureCropLayout.getHeight() * height
					/ captureContainter.getHeight();

			// 设置
			setX(x);
			setY(y);
			setCropWidth(cropWidth);
			setCropHeight(cropHeight);

			// 设置是否需要截图
			setNeedCapture(false);

		} catch (IOException ioe) {
			// 异常处理
			return;
		} catch (RuntimeException e) {
			// 异常处理
			return;
		}

		// 没有Handler新建一个
		if (handler == null) {
			handler = new CaptureActivityHandler(CaptureActivity.this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// SurfaceView已经创建
		if (!hasSurface) {
			// 标记位更改
			hasSurface = true;
			// 初始化相机
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 标记位更改
		hasSurface = false;

	}

	/**
	 * 声音初始化
	 */
	private void initBeepSound() {

		// 允许播放声音及音频播放器未初始化时
		if (playBeep && mediaPlayer == null) {
			// 音频流
			setVolumeControlStream(AudioManager.STREAM_MUSIC);

			// 初始化音频播放器
			mediaPlayer = new MediaPlayer();
			// 音频类型
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 音频播放完成监听
			mediaPlayer.setOnCompletionListener(beepListener);

			// 音频文件
			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				// 设置音频文件
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				// 左右声道声音大小
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				// 准备播放
				mediaPlayer.prepare();
			} catch (IOException e) {
				// 异常处理
				mediaPlayer = null;
			}
		}
	}

	/**
	 * 播放声音及震动手机
	 */
	private void playBeepSoundAndVibrate() {

		// 允许播放声音及音频播放器已经初始化
		if (playBeep && mediaPlayer != null) {
			// 播放音频
			mediaPlayer.start();
		}

		// 允许震动手机
		if (vibrate) {
			// 震动手机
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			// 设置震动频率
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * 音频播放完成监听
	 * 
	 * @author Hitoha
	 * @version 1.00 2015/04/29 新建
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {

		/**
		 * 播放完成
		 */
		public void onCompletion(MediaPlayer mediaPlayer) {
			// 音频复位
			mediaPlayer.seekTo(0);
		}
	};

	public Handler getHandler() {
		return handler;
	}

	public boolean isNeedCapture() {
		return isNeedCapture;
	}

	public void setNeedCapture(boolean isNeedCapture) {
		this.isNeedCapture = isNeedCapture;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(int cropWidth) {
		this.cropWidth = cropWidth;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}
}