package com.example.leospring.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    Button login;
    String qzId = "5417";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button)findViewById(R.id.login);
        Button cancle = (Button)findViewById(R.id.quit);

        login.setOnClickListener(new LoginListener());
        cancle.setOnClickListener(new LoginoutListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoginListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView account = (TextView)findViewById(R.id.account);
            TextView pass = (TextView)findViewById(R.id.pwd);
            TextView memberId = (TextView)findViewById(R.id.memberId);

            String encrypt = getEncryptedAttentance();
            Log.i("Test","Encrypt ..." + encrypt);
            sendRequest(account.getText().toString(), pass.getText().toString(), memberId.getText().toString(), qzId);
        }
    }

    private class LoginoutListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            setContentView(R.layout.activity_main);
        }
    }

    private void sendRequest(String account, String password, final String memberId, final String qzId) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Log.i("Test","encrypt ...");
                String encrypt = getEncryptedAttentance();
                Log.i("Test","after encrypt ..." + encrypt);


                Log.i("Test","start send reqeust...");
                HttpClient client = new DefaultHttpClient();
                String url = "https://ezone.yonyoucloud.com/signin/index/webLogin?" + "qzId=" + qzId + "&memberId=" + memberId;
                HttpGet get = new HttpGet(url);
                try {
                    Log.i("Test","start get token info");
                    HttpResponse response = client.execute(get);
                    Log.i("Test","token response:" + response);
                    Log.i("Test","after get token info");
                    Log.i("Test",EntityUtils.toString(response.getEntity(),"utf-8"));
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        String s = EntityUtils.toString(entity, "UTF-8");
                        JSONObject obj = new JSONObject(s);
                        String token = obj.getString("data");
                        Toast.makeText(MainActivity.this, "gen-token:" + token, Toast.LENGTH_SHORT).show();
                        HttpClient client2 = new DefaultHttpClient();
                        String url2 = "https://ezone.yonyoucloud.com/signin/attentance/encryptSignIn?token=" + token + "&clientV=1-5.3.0-1-1";
                        HttpPost post = new HttpPost(url2);
                        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("encryptedAttentance", getEncryptedAttentance()));
                        post.setEntity(new UrlEncodedFormEntity(params));
                        msg.obj = response;
//                        HttpResponse postResponse = client2.execute(post);
//                        if(postResponse.getStatusLine().getStatusCode() == 200) {
//                            Toast toast = Toast.makeText(MainActivity.this, "打卡成功", Toast.LENGTH_SHORT);
//                            toast.show();
//                        }
                    }else {
                        Toast.makeText(MainActivity.this, "打卡失败" + EntityUtils.toString(response.getEntity(),"UTF-8"), Toast.LENGTH_SHORT);
                    }
                } catch (Throwable e) {
                    Log.e("Test",e.getLocalizedMessage());
                    Toast.makeText(MainActivity.this, "打卡失败" + e.getLocalizedMessage(), Toast.LENGTH_SHORT);
                } finally {

                }
                handler.sendMessage(msg);
            }
        }).start();


    }

    private String getEncryptedAttentance() {
        JSONObject json = new JSONObject();
        //{"longitude":"116.236514","latitude":"40.067553","address":"北京市海淀区永腾南路靠近新道学院","wifiMac":"76:df:bf:3a:ae:db","wifiName":"LieBaoWiFi275","accountId":4606955,"szId":5417,"signTime":1514985739000,
        // "imei":"06275d2736329748f85e7c38cfbd79c9","deviceModel":"MI 5s Plus","deviceName":"小米","isRoot":0}
        try {
            json.put("longitude", "116.236514");
            json.put("latitude", "40.067553");
            json.put("address", "北京市海淀区永腾南路靠近新道学院");
            json.put("wifiMac", "76:df:bf:3a:ae:db");
            json.put("wifiName", "LieBaoWiFi275");
            json.put("accountId", 4606955);
            json.put("szId", 5417);
            json.put("signTime", System.currentTimeMillis());
            json.put("imei", "06275d2736329748f85e7c38cfbd79c9");
            json.put("deviceModel", "MI 5s Plus");
            json.put("deviceName", "小米");
            json.put("isRoot", 0);
            Log.i("Test",json.toString());

            Map<String,String> params = new HashMap<>();
            params.put("data",json.toString());
            params.put("type","aes");
            params.put("arg","m=ecb_pad=pkcs5_block=128_p=light-app-123456_i=255_o=1_s=utf-8_t=0");
            String response = HttpUtils.submitPostData("http://tool.chacuo.net/cryptaes", params, "UTF-8");
            JSONObject obj = new JSONObject(response);
            String token = obj.getJSONArray("data").get(0).toString();
            Toast toast = Toast.makeText(MainActivity.this, "加密结果：" + token, Toast.LENGTH_SHORT);
            toast.show();
            return token;
//            HttpClient client = new DefaultHttpClient();
//            HttpPost post = new HttpPost("http://tool.chacuo.net/cryptaes");
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("data", json.toString()));
//            params.add(new BasicNameValuePair("type", "aes"));
//            params.add(new BasicNameValuePair("arg", "m=ecb_pad=pkcs5_block=128_p=light-app-123456_i=255_o=1_s=utf-8_t=0"));
//            post.setEntity(new UrlEncodedFormEntity(params));
//            HttpResponse response = client.execute(post);
//            Log.i("Test",EntityUtils.toString(response.getEntity(),"utf-8"));
//            if (response.getStatusLine().getStatusCode() == 200) {
//                HttpEntity entity = response.getEntity();
//                String s = EntityUtils.toString(entity, "UTF-8");
//                JSONObject obj = new JSONObject(s);
//                String token = obj.getJSONArray("data").get(0).toString();
//                Toast toast = Toast.makeText(MainActivity.this, "加密结果：" + token, Toast.LENGTH_SHORT);
//                toast.show();
//                return token;
//            } else {
//                Toast.makeText(MainActivity.this, "打卡失败" + EntityUtils.toString(response.getEntity(),"UTF-8"), Toast.LENGTH_SHORT);
//            }
        } catch (Throwable e) {
            Log.e("Test",e.getLocalizedMessage());
            Toast toast = Toast.makeText(MainActivity.this, "加密失败" + e.getLocalizedMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

}
