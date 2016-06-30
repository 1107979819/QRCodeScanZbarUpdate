package com.zbar.lib.decode;

import java.util.concurrent.CountDownLatch;

import com.zbar.lib.CaptureActivity;

import android.os.Handler;
import android.os.Looper;

/**
 * 二维码解析线程
 * 
 * @author Hitoha
 * @version 1.00 2015/04/2 新建
 */
final class DecodeThread extends Thread {

	CaptureActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeThread(CaptureActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
