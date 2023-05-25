package com.example.homework_button;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 哎呀！乱码了不过也不总要了
 *
 */
class AiotMqttOption {
    private String username = "";
    private String password = "";
    private String clientId = "";


    public String getUsername() { return this.username;}
    public String getPassword() { return this.password;}
    public String getClientId() { return this.clientId;}

    /**
     *
     * @param productKey
     * @param deviceName
     * @param deviceSecret
     * @return AiotMqttOption
     */
    public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
        if (productKey == null || deviceName == null || deviceSecret == null) {
            return null;
        }

        try {
            String timestamp = Long.toString(System.currentTimeMillis());

            // clientId
            this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                    ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";
            //认证方式不同
            // userName
            this.username = deviceName + "&" + productKey;

            // password
            String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                    deviceName + "productKey" + productKey + "timestamp" + timestamp;
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
            mac.init(secretKeySpec);
            byte[] macRes = mac.doFinal(macSrc.getBytes());
            password = String.format("%064x", new BigInteger(1, macRes));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return this;
    }
}