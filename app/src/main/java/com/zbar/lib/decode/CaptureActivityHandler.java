package com.zbar.lib.decode;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.scan.R;
import com.zbar.lib.CaptureActivity;
import com.zbar.lib.camera.CameraManager;

/**
 * 扫描画面Handler
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
public final class CaptureActivityHandler extends Handler {

	/** 解析线程 */
	DecodeThread decodeThread = null;

	/** 解析画面 */
	CaptureActivity activity = null;

	/** 状态 */
	private State state;

	/**
	 * 状态枚举
	 * 
	 * @author Hitoha
	 * @version 1.00 2015/04/29 新建
	 */
	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	/**
	 * 构造方法
	 * 
	 * @param activity
	 *            解析画面
	 */
	public CaptureActivityHandler(CaptureActivity activity) {
		this.activity = activity;
		// 新建解析线程
		decodeThread = new DecodeThread(activity);
		// 线程开始
		decodeThread.start();
		// 状态设为成功
		state = State.SUCCESS;
		// 相机开始预览
		CameraManager.get().startPreview();
		// 预览与二维码解析
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		switch (message.what) {
		// 相机自动对焦时
		case R.id.auto_focus:
			// 解析状态为预览时
			if (state == State.PREVIEW) {
				// 自动对焦
				CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
			}
			break;
		// 二维码解析中
		case R.id.restart_preview:
			// 预览解析
			restartPreviewAndDecode();
			break;
		// 二维码解析成功
		case R.id.decode_succeeded:
			// 状态设为解析成功
			state = State.SUCCESS;
			// 解析成功，回调
			activity.handleDecode((String) message.obj);
			Log.i("Test","xxx:success");
			break;
		// 二维码解析失败
		case R.id.decode_failed:
			// 解析状态设为预览时
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			break;
		}

	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
		removeMessages(R.id.decode);
		removeMessages(R.id.auto_focus);
	}

	/**
	 * 开始解析二维码
	 */
	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
		}
	}

}
