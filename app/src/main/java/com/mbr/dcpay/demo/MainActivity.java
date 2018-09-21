package com.mbr.dcpay.demo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.mbr.dcpay.utils.OnLazyClickListener;
import com.mbr.dcpay.utils.network.Get;
import com.mbr.dcpay.utils.network.NetResult;
import com.mbr.pay.EnvConfig;
import com.mbr.pay.Pay;
import com.mbr.pay.PayResult;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final boolean isLogOn = true;

    // 模拟应用后台返回签名好的订单信息（实际功能由你们的后台服务器实现）
    private static final String URL_PREPAY = "http://47.100.47.200:9927/payIndex/prepay";
    private static final String CHANNEL = BuildConfig.CHANNEL;
    private static final String MERCHANT_ID = BuildConfig.MERCHANT_ID;

    private View button1;
    private View button2;
    private View button3;

    // 示例商品信息数据结构
    private static class Goods {
        String coinId;
        String amount;
        String coinName;
        Goods(String coinId, String amount, String coinName) {
            this.coinId = coinId;
            this.amount = amount;
            this.coinName = coinName;
        }
        String getDesc() {
            return "Pay " + amount + " " + coinName;
        }
    }

    // 计费点配置
    private static final Goods[] goods = new Goods[] {
            new Goods("34190899187000","0.01", "ETH"),
            new Goods("7739138616000", "1", "PH"),
            new Goods("89899349280000", "1", "TK"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    private void initListeners() {
        button1.setOnClickListener(new OnLazyClickListener() {
            @Override
            public void onLazyClick(View view) {
                pay(goods[0]); // 开始支付
            }
        });
        button2.setOnClickListener(new OnLazyClickListener() {
            @Override
            public void onLazyClick(View view) {
                pay(goods[1]); // 开始支付
            }
        });
        button3.setOnClickListener(new OnLazyClickListener() {
            @Override
            public void onLazyClick(View view) {
                pay(goods[2]); // 开始支付
            }
        });
    }

    private PrePayTask preTask;
    private void pay(Goods goods) {
        if (preTask != null) {
            Toast.makeText(MainActivity.this, "前一次支付正在执行，请稍后。。。", Toast.LENGTH_SHORT).show();
            return;
        }

        preTask = new PrePayTask(goods);
        preTask.execute();
    }

    private class PrePayTask extends AsyncTask<Void, Void, String> {
        private Goods goods;
        PrePayTask(Goods goods) {
            this.goods = goods;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String url = URL_PREPAY + "?channel="+CHANNEL+"&merchantId="+MERCHANT_ID+"&coinId="+goods.coinId+"&amount="+goods.amount;
                NetResult netResult = Get.get(url,null); // 连接你们的后台服务器获取签名好的订单信息字符串

                JSONObject jsonObject = new JSONObject(netResult.mData);
                String orderInfo = jsonObject.getString("data");
                return orderInfo;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String orderInfo) {
            preTask = null;
            hideProgress();
            if (orderInfo == null) {
                showMessage("获取订单信息失败");
                return;
            }

            callPaySdk(orderInfo); // 使用得到的签名好的订单信息字符串唤起支付APP
        }
    }

    private void callPaySdk(String orderInfo) {
        //设置当前开发环境，根据环境的不同会下载不同的apk，默认为生产环境
        EnvConfig.setEnv(EnvConfig.PRODUCT);
        // 串唤起支付APP
        Pay.pay(this, orderInfo, new Pay.OnPayListener() {
            @Override
            public void onPayResult(PayResult result) {
                if (result != null && result.isSuccess()) {
                    // 支付请求发送成功

                    Log.i(TAG, "onPayResult: success");
                    showMessage("success");
                }
                else {
                    // 支付请求发送失败

                    String errorMsg = null;
                    if (result != null) errorMsg = result.getErrorMessage();
                    Log.w(TAG, "onPayResult: " + errorMsg);
                    showMessage("failed");
                }
            }
        });
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Message")
                .setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    private void initViews() {
        button1 = findViewById(R.id.include1);
        button2 = findViewById(R.id.include2);
        button3 = findViewById(R.id.include3);

        RelativeLayout layout1 = (RelativeLayout) button1;
        TextView name1 = layout1.findViewById(R.id.coinName);
        TextView goodsDesc1 = layout1.findViewById(R.id.goodsDesc);
        name1.setText(goods[0].coinName);
        goodsDesc1.setText(goods[0].getDesc());

        RelativeLayout layout2 = (RelativeLayout) button2;
        TextView name2 = layout2.findViewById(R.id.coinName);
        TextView goodsDesc2 = layout2.findViewById(R.id.goodsDesc);
        name2.setText(goods[1].coinName);
        goodsDesc2.setText(goods[1].getDesc());

        RelativeLayout layout3 = (RelativeLayout) button3;
        TextView name3 = layout3.findViewById(R.id.coinName);
        TextView goodsDesc3 = layout3.findViewById(R.id.goodsDesc);
        name3.setText(goods[2].coinName);
        goodsDesc3.setText(goods[2].getDesc());
    }

    private Dialog progressDialog;
    private void showProgress() {
        if (progressDialog != null) return;
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
    }
    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class ProgressDialog extends Dialog {

        public ProgressDialog(@NonNull Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            RelativeLayout root = new RelativeLayout(context);
            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            RelativeLayout.LayoutParams params;
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            root.addView(progressBar, params);
            setContentView(root);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            setCanceledOnTouchOutside(false);
        }
    }
}
