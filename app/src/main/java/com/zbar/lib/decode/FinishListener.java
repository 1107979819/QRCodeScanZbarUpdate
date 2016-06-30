package com.zbar.lib.decode;

import android.app.Activity;
import android.content.DialogInterface;

/**
 * 解析完成回调
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
public final class FinishListener implements DialogInterface.OnClickListener,
		DialogInterface.OnCancelListener, Runnable {

	private final Activity activityToFinish;

	public FinishListener(Activity activityToFinish) {
		this.activityToFinish = activityToFinish;
	}

	public void onCancel(DialogInterface dialogInterface) {
		run();
	}

	public void onClick(DialogInterface dialogInterface, int i) {
		run();
	}

	public void run() {
		activityToFinish.finish();
	}

}
