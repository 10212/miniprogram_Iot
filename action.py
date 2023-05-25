import paho.mqtt.client as mqtt
import time
import hashlib
import hmac
import random
import json
import Serial_Servo_Running as SSR //机器人上的动作执行程序对应部分
options = {
    'productKey': 'a1SkFDN7vZ9',
    'deviceName': 'e9mUK8ZMacpX4NrNowLD',
    'deviceSecret': '1770930a1215c963b341bfa6b6db492d',//三元组
    'regionId': 'cn-shanghai'
}

HOST = options['productKey'] + '.iot-as-mqtt.' + options['regionId'] + '.aliyuncs.com'	//组合行程host名
PORT = 1883	//mqtt 1883
SUB_TOPIC = "/sys/" + options['productKey'] + "/" + options['deviceName'] + "/thing/service/property/set";	
//topic组合方法对应流转过程中的物模型下发中的Topic/sys/whiteRobot_Receive/thing/service/property/set




# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    client.subscribe(SUB_TOPIC)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    msg=json.loads(msg.payload)
    #print(msg['items'])
    action=msg['items']['action']['value']
    SSR.runAction(action)



def hmacsha1(key, msg):
    return hmac.new(key.encode(), msg.encode(), hashlib.sha1).hexdigest()


def getAliyunIoTClient():
    timestamp = str(int(time.time()))
    CLIENT_ID = "paho.py|securemode=3,signmethod=hmacsha1,timestamp=" + timestamp + "|"
    CONTENT_STR_FORMAT = "clientIdpaho.pydeviceName" + options['deviceName'] + "productKey" + options[
        'productKey'] + "timestamp" + timestamp
    # set username/password.
    USER_NAME = options['deviceName'] + "&" + options['productKey']
    PWD = hmacsha1(options['deviceSecret'], CONTENT_STR_FORMAT)
    client = mqtt.Client(client_id=CLIENT_ID, clean_session=False)
    client.username_pw_set(USER_NAME, PWD)
    return client


if __name__ == '__main__':
    client = getAliyunIoTClient()
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(HOST, 1883, 300)

    client.loop_forever()