package com.scan.activity;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.scan.R;

/**
 * 识别图片（图片-》转bitmap-》YUV420sp-》ZbarManager.decode）
 * @author Dell
 *
 */
public class ImageActivity extends Activity {
	/** 扫描按钮 */
	private Button btnScan;

	/** 扫描结果展示文本框 */
	private TextView txtScanResult;
	
	private ImageView iv;

	/** Request Code */
	private static final int REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_qrcode_scan_image);

		// 初始化控件
		btnScan = (Button) findViewById(R.id.btnScan);
		txtScanResult = (TextView) findViewById(R.id.txtScanResult);
		iv = (ImageView)findViewById(R.id.imageView1);

		// 扫描按钮添加点击监听
		btnScan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				decodeImageQRCode();
			}
		});
	}
	
	
	public  void decodeImageQRCode()
	{
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.test2);
//		iv.setImageBitmap(bm);
//		ZbarManager zm = new ZbarManager();
//		String re = zm.decode( Bmp2YUV.getYUV420sp(bm.getWidth(),bm.getHeight(), bm), bm.getWidth(),bm.getHeight(),false, 0,0,  bm.getWidth(),bm.getHeight());
//		 txtScanResult.setText(bm.getWidth()+" x "+bm.getHeight()+" result:"+re);
		
	}
	
  
}