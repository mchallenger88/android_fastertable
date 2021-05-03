package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.Payment
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import java.io.File
import javax.inject.Inject

class PaymentRepository @Inject constructor(private val app: Application) {

    fun createNewPayment(order: Order, terminal: Terminal): Payment{

        val payment = Payment(
            id = order.id.replace("O", "P"),
            orderId = order.id,
            orderNumber = order.orderNumber,
            orderType = order.orderType,
            tableNumber = order.tableNumber,
            timeStamp = GlobalUtils().getNowEpoch(),
            orderStartTime = order.startTime,
            orderCloseTime = order.closeTime,
            guestCount = order.guests?.size!!,
            splitType = "",
            splitTicket = null,
            employeeId = order.employeeId!!,
            userName = order.userName,
            terminalId =  terminal.terminalId.toString(),
            tickets = arrayListOf(order.createSingleTicket()),

            statusApproval = null,
            newApproval = null,
            closed = false,
            locationId = order.locationId,
            archived = false,
            type = "",
            _rid = "",
            _self = "",
            _etag = "",
            _attachments = "",
            _ts = null)

        val gson = Gson()
        val jsonString = gson.toJson(payment)
        val file= File(app.filesDir, "payment.json")
        if (file.exists()){
            file.delete()
        }
        file.writeText(jsonString)
        return payment
    }
}