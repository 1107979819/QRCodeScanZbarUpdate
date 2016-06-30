package com.zbar.lib.decode;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.scan.R;
import com.zbar.lib.CaptureActivity;
import com.zbar.lib.bitmap.PlanarYUVLuminanceSource;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * 解析Handler
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
final class DecodeHandler extends Handler {

	CaptureActivity activity = null;

	DecodeHandler(CaptureActivity activity) {
		this.activity = activity;
	}
	private ImageScanner mImageScanner = null;

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode:
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	/**
	 * 二维码解析
	 * 
	 * @param data
	 *            图片数据
	 * @param width
	 *            原始宽度
	 * @param height
	 *            原始高度
	 */
	private void decode(byte[] data, int width, int height) {
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}

		// Here we are swapping, that's the difference to #11
		int tmp = width;
		width = height;
		height = tmp;
//
//		// ZBar管理器
//		ZbarManager manager = new ZbarManager();
//		// 进行解码
//		String result = manager.decode(rotatedData, width, height, true,
//				activity.getX(), activity.getY(), activity.getCropWidth(),
//				activity.getCropHeight());
//		Log.i("Test","xxx:decode");

		Log.i("Test","xxx:decode");

		Image barcode = new Image(width, height, "Y800");
		barcode.setData(rotatedData);
		barcode.setCrop(activity.getX(),activity.getY(), activity.getCropWidth(),
				activity.getCropHeight());


		mImageScanner = new ImageScanner();
		mImageScanner.setConfig(0, Config.X_DENSITY, 3);
		mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
		int result = mImageScanner.scanImage(barcode);
		Log.i("Test","result:"+result);
		String resultStr = null;
		if (result != 0) {
			SymbolSet syms = mImageScanner.getResults();
			for (Symbol sym : syms) {
				resultStr = sym.getData();
			}
		}

		if (!TextUtils.isEmpty(resultStr)) {
			Log.i("Test","resultStr:"+resultStr);
			if (null != activity.getHandler()) {
				Message msg = new Message();
				msg.obj = resultStr;
				msg.what = R.id.decode_succeeded;
				activity.getHandler().sendMessage(msg);
			}
		} else {
			if (null != activity.getHandler()) {
				activity.getHandler().sendEmptyMessage(R.id.decode_failed);
			}
		}

//		if (result != null) {
//			// 需要保存扫描的二维码图片
//			if (activity.isNeedCapture()) {
//				// 生成bitmap
//				PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
//						rotatedData, width, height, activity.getX(),
//						activity.getY(), activity.getCropWidth(),
//						activity.getCropHeight(), false);
//				int[] pixels = source.renderThumbnail();
//				int w = source.getThumbnailWidth();
//				int h = source.getThumbnailHeight();
//				Bitmap bitmap = Bitmap.createBitmap(pixels, 0, w, w, h,
//						Bitmap.Config.ARGB_8888);
//				try {
//					// 保存二维码图片
//					String rootPath = Environment.getExternalStorageDirectory()
//							.getAbsolutePath() + "/Qrcode/";
//					File root = new File(rootPath);
//					if (!root.exists()) {
//						root.mkdirs();
//					}
//					File f = new File(rootPath + "Qrcode.jpg");
//					if (f.exists()) {
//						f.delete();
//					}
//					f.createNewFile();
//
//					FileOutputStream out = new FileOutputStream(f);
//					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//					out.flush();
//					out.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			// 向Activity发一条消息
//			if (null != activity.getHandler()) {
//				Message msg = new Message();
//				msg.obj = result;
//				msg.what = R.id.decode_succeeded;
//				activity.getHandler().sendMessage(msg);
//			}
//		} else {
//			if (null != activity.getHandler()) {
//				activity.getHandler().sendEmptyMessage(R.id.decode_failed);
//			}
//		}
	}

}
