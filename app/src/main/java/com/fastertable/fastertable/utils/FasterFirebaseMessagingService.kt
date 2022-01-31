package com.fastertable.fastertable.utils

//import android.util.Log
//import androidx.core.content.ContextCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class FasterFirebaseMessagingService: FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d(TAG, "From: ${remoteMessage.from}")
//
//        // TODO Step 3.5 check messages for data
//        // Check if message contains a data payload.
//        remoteMessage.data?.let {
//            Log.d(TAG, "Message data payload: " + remoteMessage.data)
//        }
//
//        // TODO Step 3.6 check messages for notification and call sendNotification
//        // Check if message contains a notification payload.
//        remoteMessage.notification?.let {
//            Log.d(TAG, "Message Notification Body: ${it.body}")
////            sendNotification(it.body!!)
//        }
//    }
//
//    override fun onNewToken(token: String) {
//        Log.d(TAG, "Refreshed token: $token")
//
//        // If you want to send messages to this application instance or
//        // manage this apps subscriptions on the server side, send the
//        // Instance ID token to your app server.
//        sendRegistrationToServer(token)
//    }
//
//    private fun sendRegistrationToServer(token: String?) {
//        // TODO: Implement this method to send token to your app server.
//    }
//
//
////    private fun sendNotification(messageBody: String) {
////        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
////        notificationManager.sendNotification(messageBody, applicationContext)
////    }
//
//    companion object {
//        private const val TAG = "FasterFirebaseMsgService"
//    }
//}


