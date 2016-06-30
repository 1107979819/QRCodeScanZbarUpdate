package com.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.scan.R;
import com.zbar.lib.CaptureActivity;

/**
 * 主画面
 * 
 * @author Hitoha
 * @version 1.00 2015/04/29 新建
 */
public class MainActivity extends Activity {

	/** 扫描按钮 */
	private Button btnScan;

	/** 扫描结果展示文本框 */
	private TextView txtScanResult;

	/** Request Code */
	private static final int REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_qrcode_main);

		// 初始化控件
		btnScan = (Button) findViewById(R.id.btnScan);
		txtScanResult = (TextView) findViewById(R.id.txtScanResult);

		// 扫描按钮添加点击监听
		btnScan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转到扫描画面
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, CaptureActivity.class);

				startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}
	
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// 获取数据
				Bundle bundle = data.getExtras();
				// 显示扫描到的内容
				txtScanResult.setText(bundle.getString("qrCodeString"));
			}
		}
	}
}
