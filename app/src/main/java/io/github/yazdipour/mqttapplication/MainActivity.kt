package io.github.yazdipour.mqttapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.client.mqttv3.*
import java.util.*
import kotlin.math.log


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var txtLog: TextView? = null
    private var mqtt: MqttHandler = MqttHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtTopic = findViewById<EditText>(R.id.txt_topic)
        val txtMsg = findViewById<EditText>(R.id.txt_msg)
        val txtUrl = findViewById<EditText>(R.id.txt_url)

        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            mqtt.initMqttClient(applicationContext,
                txtUrl.text.toString(),
                UUID.randomUUID().toString(),
                object : MqttCallbackExtended {
                    override fun connectComplete(b: Boolean, s: String) {
                        logIt("connectComplete: $s")
                    }

                    override fun connectionLost(throwable: Throwable) {
                        logIt("connectionLost: ${throwable.message}")
                    }

                    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                        val msg = String(mqttMessage.payload)
                        logIt(">> $topic: $msg")
                    }

                    override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                        logIt("deliveryComplete: ${iMqttDeliveryToken.message}")
                    }
                })
        }

        findViewById<Button>(R.id.btn_disconnect).setOnClickListener {
            mqtt.disconnect(object : IMqttActionListener {
                override fun onSuccess(p0: IMqttToken?) {
                    logIt("Disconnect Successfully")
                }

                override fun onFailure(p0: IMqttToken?, p1: Throwable?) {
                    logIt("Disconnect failed: ${p1?.message}")
                }
            })
        }

        findViewById<Button>(R.id.btn_sub).setOnClickListener {
            mqtt.subscribe(
                txtTopic.text.toString(),
                object : IMqttActionListener {
                    override fun onSuccess(p0: IMqttToken?) {
                        logIt("Subscribed to ${txtTopic.text} Successfully: $p0")
                    }

                    override fun onFailure(p0: IMqttToken?, p1: Throwable?) {
                        logIt("Subscribed failed: ${p1?.message}")
                    }
                })
        }
        findViewById<Button>(R.id.btn_unsub).setOnClickListener {
            mqtt.unSubscribe(
                txtTopic.text.toString(),
                object : IMqttActionListener {
                    override fun onSuccess(p0: IMqttToken?) {
                        logIt("Unsubscribed to ${txtTopic.text} Successfully")
                    }

                    override fun onFailure(p0: IMqttToken?, p1: Throwable?) {
                        logIt("Unsubscribed failed: ${p1?.message}")
                    }
                })
        }

        findViewById<Button>(R.id.btn_pub).setOnClickListener {
            try {
                val t = mqtt.publishMessage(txtTopic.text.toString(), txtMsg.text.toString())
                logIt("Publish: ${t?.message}")
            } catch (e: Exception) {
                logIt("Publish Error: ${e.message}")
            }
        }
    }

    private fun logIt(msg: String) {
        if (txtLog == null) txtLog = findViewById(R.id.txt_log)
        val logs = txtLog?.text
        val count = (logs ?: "").split("\n").count()
        txtLog?.text = if (count > 10) msg else "$logs\n$msg"
    }
}
