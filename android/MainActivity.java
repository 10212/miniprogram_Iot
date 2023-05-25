package com.example.homework_button;

import androidx.appcompat.app.AppCompatActivity;
//引入AiotMqttOption class方法2
import com.example.homework_button.AiotMqttOption;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
//import android.view.Gravity;
import android.view.View;
//import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

        private static final String TAG = MainActivity.class.getSimpleName();

        private TextView msgTextView;

        private String productKey = "a1DqiQYN32N";// 产品key
        private String deviceName = "We_Chat_Post";//已经注册的设备id
        private String deviceSecret = "65d54c17893d95106a77a84ad9f009a9";//设备秘钥


         String action_diy="8"; //initial
        //property post topic
        private final String payloadJson = "{\"id\":%s,\"params\":{\"action\":%s},\"method\":\"thing.event.property.post\"}";
        private MqttClient mqttClient = null;

        final int POST_DEVICE_PROPERTIES_SUCCESS = 1002;
        final int POST_DEVICE_PROPERTIES_ERROR = 1003;
        //handler异步类
        //消息异步消息--
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case POST_DEVICE_PROPERTIES_SUCCESS:
                        showToast("发送数据成功");
                        break;
                    case POST_DEVICE_PROPERTIES_ERROR:
                        showToast("post数据失败");
                        break;
                }
            }
        };

        private String responseBody = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            final EditText ac_diy= (EditText)findViewById(R.id.editTextTextPersonNamen);
            msgTextView = findViewById(R.id.textView2);

            findViewById(R.id.button_on).setOnClickListener((l) -> {
                new Thread(() -> initAliyunIoTClient()).start();
                //新建立个线程来启动客户端
            });

            //按钮触发post事件
            findViewById(R.id.button).setOnClickListener((l) -> {
                action_diy=ac_diy.getText().toString();
                mHandler.postDelayed(() -> postDeviceProperties(action_diy), 1000);
            });


            findViewById(R.id.button_off).setOnClickListener((l) -> {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            });
            findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="2";
                }
            });
            findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="1";
                }
            });
            findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="3";
                }
            });
            findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="5";
                }
            });
            findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="8";
                }
            });
            findViewById(R.id.button9).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="9";
                }
            });
            findViewById(R.id.button7).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    action_diy="7";
                }
            });
        }


        /**
         * 初始化链接参数
         * productKey，deviceName，deviceSecret 三元组
         */
        private void initAliyunIoTClient() {

            try {
                String clientId = "androidthings" + System.currentTimeMillis();

                Map<String, String> params = new HashMap<String, String>(16);
                params.put("productKey", productKey);
                params.put("deviceName", deviceName);
                params.put("clientId", clientId);
                String timestamp = String.valueOf(System.currentTimeMillis());
                params.put("timestamp", timestamp);

                // cn-shanghai
                String targetServer = "tcp://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
                //securemode=3一机一密，一型一密预注册认证方式：使用设备证书
                String mqttclientId = clientId + "|securemode=3,signmethod=hmacsha1,timestamp=" + timestamp + "|";
                String mqttUsername = deviceName + "&" + productKey;
                String mqttPassword = AliyunIoTSignUtil.sign(params, deviceSecret, "hmacsha1");

                connectMqtt(targetServer, mqttclientId, mqttUsername, mqttPassword);


            } catch (Exception e) {
                e.printStackTrace();
                responseBody = e.getMessage();
                mHandler.sendEmptyMessage(POST_DEVICE_PROPERTIES_ERROR);
            }
        }
        //mqtt建立连接方法，
        //program：
        //@url:host:1883到阿里云链接
        //@clientId:拼接形成的客户端设备名
        //@mqttUsername:拼接形成的用户名
        //@mqttPassword：securemode=3；设备证书哈希形成的密码
        public void connectMqtt(String url, String clientId, String mqttUsername, String mqttPassword) throws Exception {

            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(url, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // MQTT 3.1.1
            connOpts.setMqttVersion(4);
            connOpts.setAutomaticReconnect(true);
            connOpts.setCleanSession(true);

            connOpts.setUserName(mqttUsername);
            connOpts.setPassword(mqttPassword.toCharArray());
            connOpts.setKeepAliveInterval(60);

            mqttClient.connect(connOpts);
            Log.d(TAG, "connected " + url);

        }

        /**
         * post 数据
         * 此处使用的是json格式数据，填充于payload中
         */
        private void postDeviceProperties(String actions) {

            try {

                Random random = new Random();

                //上报数据
                //String.format用于填充数据，有当前时间即时间戳和动作序号
                String payload = String.format(payloadJson, String.valueOf(System.currentTimeMillis()), actions);
                responseBody = payload;
                MqttMessage message = new MqttMessage(payload.getBytes("utf-8"));
                message.setQos(1);

                //对应阿里云上的topic内容了
                String pubTopic = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
                mqttClient.publish(pubTopic, message);
                Log.d(TAG, "publish topic=" + pubTopic + ",payload=" + payload);
                mHandler.sendEmptyMessage(POST_DEVICE_PROPERTIES_SUCCESS);

                mHandler.postDelayed(() -> postDeviceProperties(action_diy), 1 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
                responseBody = e.getMessage();
                mHandler.sendEmptyMessage(POST_DEVICE_PROPERTIES_ERROR);
                Log.e(TAG, "postDeviceProperties error " + e.getMessage(), e);
            }
        }

        private void showToast(String msg) {
        //Toast.makeText(this,msg + "\n" + responseBody,Toast.LENGTH_SHORT).show();
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        }

}



