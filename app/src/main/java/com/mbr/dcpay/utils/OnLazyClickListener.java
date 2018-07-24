package com.mbr.dcpay.utils;

import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author 作者 E-mail: panhang@ehoo.cn
 * @version 创建时间：Jan 9, 2015 1:01:41 PM 
 */

public abstract class OnLazyClickListener implements OnClickListener {
	private static final String	TAG	= "OnLazyClickListener";
	
	private long mLastClick;

	@Override
	public void onClick(View v) {
		long curr = System.currentTimeMillis();
		if (curr - mLastClick < 500) {
			return;
		}
		else {
			mLastClick = curr;
			onLazyClick(v);
		}
	}
	
	public abstract void onLazyClick(View v);
}
