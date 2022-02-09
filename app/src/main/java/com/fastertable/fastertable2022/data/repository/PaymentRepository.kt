package com.fastertable.fastertable2022.data.repository

import android.app.Application
import com.fastertable.fastertable2022.api.*
import com.fastertable.fastertable2022.data.models.*
import com.fastertable.fastertable2022.utils.GlobalUtils
import com.fastertable.fastertable2022.utils.round
import com.google.gson.Gson
import java.io.BufferedReader
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
            return p
        }else{
            throw RuntimeException("fetch failure")
        }
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
            return p
        }else{
            throw RuntimeException("fetch failure")
        }
    }
}

class GetPayment @Inject constructor(private val getPaymentUseCase: GetPaymentUseCase,
                                   private val paymentRepository: PaymentRepository){

    suspend fun getPayment(id: String, lid: String): Payment?{
        val payment: Payment?
        val result = getPaymentUseCase.getPayment(id, lid)
        if (result is GetPaymentUseCase.Result.Success){
            payment = result.payment
            paymentRepository.savePayment(payment)
            return payment
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

    fun savePayment(payment: Payment?) {
        //Save payment json to file
        if (payment != null){
            val gson = Gson()
            val jsonString = gson.toJson(payment)
            val file= File(app.filesDir, "payment.json")
            file.writeText(jsonString)
        }

    }

    fun savePayments(payments: List<Payment>){
        //Save payments json to file
        val gson = Gson()
        val jsonString = gson.toJson(payments)
        val file= File(app.filesDir, "payments.json")
        file.writeText(jsonString)
    }

    fun getPayment(): Payment?{
        val gson = Gson()
        if (File(app.filesDir, "payment.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "payment.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Payment::class.java)
        }
        return null
    }

    fun clearPayment(){
        val gson = Gson()
        val file= File(app.filesDir, "payment.json")
        file.delete()
    }

    fun createNewPayment(order: Order, terminal: Terminal, fees: List<AdditionalFees>?): Payment{

        val payment = Payment(
            id = order.id.replace("O", "P"),
            orderId = order.id,
            orderNumber = order.orderNumber,
            orderType = order.orderType,
            tableNumber = order.tableNumber,
            timeStamp = GlobalUtils().getNowEpoch(),
            orderStartTime = order.startTime,
            orderCloseTime = order.closeTime,
            guestCount = order.guestCount,
            splitType = "",
            splitTicket = null,
            employeeId = order.employeeId ?: "",
            userName = order.userName,
            terminalId =  terminal.terminalId.toString(),
            tickets = arrayListOf(order.createSingleTicket(fees)),
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

    fun updatePaymentNewOrderItems(payment: Payment, order: Order): Payment{
        order.orderItems?.forEach{ item ->
            val ticket = payment.tickets?.get(0)
            if (ticket != null){
                val found = ticket.ticketItems.findLast { it.orderItemId == item.id }
                if (found == null){
                    createTicketItem(ticket, item.guestId, item)
                }
            }

        }
        payment.recalculateTotals()
        return payment
    }

    private fun createTicketItem(ticket: Ticket, guest: Int, orderItem: OrderItem){
        val i = ticket.ticketItems.size
        val t = TicketItem(
            id = i + 1,
            orderGuestNo = guest,
            orderItemId = orderItem.id,
            quantity = orderItem.menuItemPrice.quantity,
            itemName = orderItem.menuItemName,
            itemPrice = orderItem.menuItemPrice.price,
            discountPrice = null,
            priceModified = orderItem.priceAdjusted,
            approvalType = null,
            approvalId = null,
            itemMods = ArrayList(orderItem.activeModItems()),
            salesCategory = orderItem.salesCategory,
            ticketItemPrice = orderItem.getTicketExtendedPrice(ticket.taxRate).round(2),
            tax = orderItem.getSalesTax(orderItem.tax, ticket.taxRate).round(2)
        )
        ticket.ticketItems.add(t)
    }


    fun createEmptyTicket(payment: Payment, guest: Int, fees: List<AdditionalFees>): Ticket{
        val extraFees = calcExtraFees(0.00, fees)
        return Ticket(
            orderId = payment.orderId,
            id = guest,
            ticketItems = mutableListOf<TicketItem>(),
            subTotal = 0.00,
            tax = 0.00,
            total = 0.00,
            gratuity = 0.00,
            deliveryFee = 0.00,
            extraFees = extraFees,
            paymentTotal = 0.00,
            paymentList = null,
            partialPayment = false,
            uiActive = false,
            taxRate = payment.taxRate,
            paymentType = ""
        )
    }

    private fun calcExtraFees(subTotal: Double, fees: List<AdditionalFees>?): List<AdditionalFees>?{
        if (fees != null){
            for (fee in fees){
                if (fee.feeType.name == "Flat Amount"){
                    fee.checkAmount = fee.amount.round(2)
                }else{
                    fee.checkAmount = subTotal.times(fee.amount.div(100)).round(2)
                }
            }
        }
        return fees
    }
}