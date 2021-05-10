package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.Payment
import com.fastertable.fastertable.data.models.Terminal
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject

class SavePayment @Inject constructor(private val savePaymentUseCase: SavePaymentUseCase,
                                    private val paymentRepository: PaymentRepository){
    suspend fun savePayment(payment: Payment): Payment{
        val p: Payment
        val result = savePaymentUseCase.savePayment(payment)
        if (result is SavePaymentUseCase.Result.Success){
            p = result.payment
            paymentRepository.savePayment(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return p
    }
}

class UpdatePayment @Inject constructor(private val updatePaymentUseCase: UpdatePaymentUseCase,
                                      private val paymentRepository: PaymentRepository){
    suspend fun savePayment(payment: Payment): Payment{
        val p: Payment
        val result = updatePaymentUseCase.savePayment(payment)
        if (result is UpdatePaymentUseCase.Result.Success){
            p = result.payment
            paymentRepository.savePayment(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return p
    }
}

class GetPayment @Inject constructor(private val getPaymentUseCase: GetPaymentUseCase,
                                   private val paymentRepository: PaymentRepository){

    suspend fun getPayment(id: String, lid: String){
        val payment: Payment
        val result = getPaymentUseCase.getPayment(id, lid)
        if (result is GetPaymentUseCase.Result.Success){
            payment = result.payment
            paymentRepository.savePayment(payment)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetPayments @Inject constructor(private val getPaymentsUseCase: GetPaymentsUseCase,
                                    private val paymentRepository: PaymentRepository){

    suspend fun getPayments(midnight: Long, rid: String){
        val payments: List<Payment>
        val result = getPaymentsUseCase.getPayments(midnight, rid)
        if (result is GetPaymentsUseCase.Result.Success){
            payments = result.payments
            paymentRepository.savePayments(payments)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class PaymentRepository @Inject constructor(private val app: Application) {

    fun savePayment(payment: Payment) {
        //Save payment json to file
        val gson = Gson()
        val jsonString = gson.toJson(payment)
        val file= File(app.filesDir, "payment.json")
        file.writeText(jsonString)
    }

    fun savePayments(payments: List<Payment>){
        //Save payments json to file
        val gson = Gson()
        val jsonString = gson.toJson(payments)
        val file= File(app.filesDir, "payments.json")
        file.writeText(jsonString)
    }

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
            taxRate = order.taxRate,
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