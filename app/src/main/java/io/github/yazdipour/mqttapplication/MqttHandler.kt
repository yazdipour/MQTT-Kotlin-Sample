package io.github.yazdipour.mqttapplication

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHandler {

    private val classTag: String = this.javaClass.simpleName
    private var client: MqttAndroidClient? = null
    private val isConnected: Boolean
        get() = client?.isConnected ?: false

    fun initMqttClient(
        context: Context,
        url: String, // "tcp://121.0.0.1:1883"
        clientId: String,
        mqttCallbackExtended: MqttCallbackExtended
    ): MqttAndroidClient? {
        client = MqttAndroidClient(context, url, clientId)
        val token: IMqttToken = client!!.connect(getMqttConnectionOption())
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                client!!.setBufferOpts(getDisconnectedBufferOptions())
                Log.d(classTag, "Mqtt Successfully connected")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                exception.printStackTrace()
                throw exception
            }
        }
        client!!.setCallback(mqttCallbackExtended)
        return client
    }

    fun publishMessage(topic: String, msg: String, qos: Int = 1): IMqttDeliveryToken? {
        if (isConnected) return null
        val encodedPayload = msg.toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.id = 5866
        message.isRetained = true
        message.qos = qos
        return client!!.publish(topic, message)
    }

    fun subscribe(topic: String, iMqttActionListener: IMqttActionListener, qos: Int = 1) {
        if (isConnected)
            client!!.subscribe(topic, qos).actionCallback = iMqttActionListener
    }

    fun unSubscribe(topic: String, iMqttActionListener: IMqttActionListener) {
        if (isConnected)
            client!!.unsubscribe(topic).actionCallback = iMqttActionListener
    }

    fun disconnect(iMqttActionListener: IMqttActionListener) {
        if (isConnected)
            client!!.disconnect().actionCallback = iMqttActionListener
    }

    /**
     * Clean session: On connection, a client sets the “clean session” flag.If the clean session is set to false, means when the client disconnects, any subscriptions it has will remain and any subsequent QoS 1 or 2 messages will be stored until it connects again in the future. If the clean session is true, then all subscriptions will be removed from the client when it disconnects.
     * Wills: When a client connects to a broker, it may inform the broker that it has a will. This is a message that it wishes the broker to send when the client disconnects unexpectedly. The will message has a topic, QoS and retains status just the same as any other message.
     **/
    private fun getMqttConnectionOption(
        username: String? = null,
        password: String? = null
    ): MqttConnectOptions? {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.userName = username
        mqttConnectOptions.password = password?.toCharArray()
        mqttConnectOptions.setWill("Offline", "I am going offline".toByteArray(), 1, true)
        return mqttConnectOptions
    }

    private fun getDisconnectedBufferOptions(): DisconnectedBufferOptions? {
        val disconnectedBufferOptions = DisconnectedBufferOptions()
        disconnectedBufferOptions.isBufferEnabled = true
        disconnectedBufferOptions.bufferSize = 100
        disconnectedBufferOptions.isPersistBuffer = true
        disconnectedBufferOptions.isDeleteOldestMessages = false
        return disconnectedBufferOptions
    }
}