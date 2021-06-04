package com.fastertable.fastertable.data.repository

import android.app.Application
import com.fastertable.fastertable.api.GetApprovalUseCase
import com.fastertable.fastertable.api.GetApprovalsUseCase
import com.fastertable.fastertable.api.SaveApprovalUseCase
import com.fastertable.fastertable.api.UpdateApprovalUseCase
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.GlobalUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject

class SaveApproval @Inject constructor(private val saveApprovalUseCase: SaveApprovalUseCase,
                                      private val approvalRepository: ApprovalRepository){
    suspend fun saveApproval(approval: Approval): Approval{
        val p: Approval
        val result = saveApprovalUseCase.saveApproval(approval)
        if (result is SaveApprovalUseCase.Result.Success){
            p = result.approval
            approvalRepository.saveApproval(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return p
    }
}

class UpdateApproval @Inject constructor(private val updateApprovalUseCase: UpdateApprovalUseCase,
                                        private val approvalRepository: ApprovalRepository){
    suspend fun saveApproval(approval: Approval): Approval{
        val p: Approval
        val result = updateApprovalUseCase.saveApproval(approval)
        if (result is UpdateApprovalUseCase.Result.Success){
            p = result.approval
            approvalRepository.saveApproval(p)
        }else{
            throw RuntimeException("fetch failure")
        }
        return p
    }
}

class GetApproval @Inject constructor(private val getApprovalUseCase: GetApprovalUseCase,
                                     private val approvalRepository: ApprovalRepository){

    suspend fun getApproval(id: String, lid: String){
        val approval: Approval?
        val result = getApprovalUseCase.getApproval(id, lid)
        if (result is GetApprovalUseCase.Result.Success){
            approval = result.approval
            approvalRepository.saveApproval(approval)
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetApprovals @Inject constructor(private val getApprovalsUseCase: GetApprovalsUseCase,
                                      private val approvalRepository: ApprovalRepository){

    suspend fun getApprovals(timeBasedRequest: TimeBasedRequest): List<Approval>{
        val approvals: List<Approval>
        val result = getApprovalsUseCase.getApprovals(timeBasedRequest)
        if (result is GetApprovalsUseCase.Result.Success){
            approvals = result.approvals
            approvalRepository.saveApprovals(approvals)
            return approvals
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class ApprovalRepository @Inject constructor(private val app: Application) {
    fun getApproval(): Approval?{
        val gson = Gson()
        if (File(app.filesDir, "approval.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "approval.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return gson.fromJson(inputString, Approval::class.java)
        }
        return null
    }

    fun getApprovalbyId(id: String): Approval?{
        val gson = Gson()
        var approval: Approval
        if (File(app.filesDir, "approval.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "approval.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            approval = gson.fromJson(inputString, Approval::class.java)

            if (approval.id == id){
                return approval
            }else{
                return null
            }
        }
        return null
    }

    fun getApprovalsFromFile(): List<Approval>?{
        val gson = Gson()
        if (File(app.filesDir, "approvals.json").exists()){
            val bufferedReader: BufferedReader = File(app.filesDir, "approvals.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val arrayList: ArrayList<Approval> = gson.fromJson(inputString, object : TypeToken<List<Approval?>?>() {}.type)
            val list: List<Approval> = arrayList
            return list
        }
        return null
    }

    fun saveApproval(approval: Approval?) {
        //Save order json to file
        val gson = Gson()
        if (approval != null) {
            val jsonString = gson.toJson(approval)
            val file = File(app.filesDir, "approval.json")
            file.writeText(jsonString)
        }else{
            val file= File(app.filesDir, "approval.json")
            if (file.exists()){
                file.delete()
            }
        }
    }

    fun saveApprovals(approvals: List<Approval>){
        //Save approvals json to file
        val gson = Gson()
        val jsonString = gson.toJson(approvals)
        val file= File(app.filesDir, "approvals.json")
        file.writeText(jsonString)
    }

    private fun createApproval(order: Order): Approval {
        val id: String = order.id.replace("O", "A")

        val approval = Approval(
            id = id,
            order = order,
            approvalItems = arrayListOf<ApprovalItem>(),
            locationId = order.locationId,
            timeRequested = GlobalUtils().getNowEpoch(),
            archived = false,
            type = "Approval",
            _rid = "",
            _self = "",
            _etag = "",
            _attachments = "",
            _ts = null)

        val gson = Gson()
        val jsonString = gson.toJson(approval)
        val file= File(app.filesDir, "approval.json")
        if (file.exists()){
            file.delete()
        }

        file.writeText(jsonString)
        return approval
    }


    fun createVoidTicketApproval(order: Order, payment: Payment, approval: Approval?): Approval{
        val app = approval ?: createApproval(order)

        val ai = ApprovalItem(
            id = app.approvalItems.size,
            approvalType = "Void Ticket",
            discount = null,
            ticketItem = null,
            ticket = payment.activeTicket()!!,
            amount = payment.activeTicket()!!.total,
            timeRequested = GlobalUtils().getNowEpoch(),
            approved = null,
            timeHandled = null,
            managerId = null
        )
        app.approvalItems.add(ai)

        return app
    }

    fun createVoidTicketItemApproval(order: Order, payment: Payment, ticketItem: TicketItem, approval: Approval?): Approval{
        val app = approval ?: createApproval(order)

        val ai = ApprovalItem(
            id = app.approvalItems.size,
            approvalType = "Void Item",
            discount = null,
            ticketItem = ticketItem,
            ticket = payment.activeTicket()!!,
            amount = ticketItem.ticketItemPrice,
            timeRequested = GlobalUtils().getNowEpoch(),
            approved = null,
            timeHandled = null,
            managerId = null
        )
        app.approvalItems.add(ai)

        return app
    }

    fun createDiscountTicketApproval(order: Order, payment: Payment, discount: Discount, disTotal: Double, approval: Approval?): Approval{
        val app = approval ?: createApproval(order)

        val ai = ApprovalItem(
            id = app.approvalItems.size,
            approvalType = "Discount Ticket",
            discount = discount.discountName,
            ticketItem = null,
            ticket = payment.activeTicket()!!,
            amount = disTotal,
            timeRequested = GlobalUtils().getNowEpoch(),
            approved = null,
            timeHandled = null,
            managerId = null
        )
        app.approvalItems.add(ai)

        return app

    }

    fun createDiscountTicketItemApproval(order: Order, payment: Payment, ticketItem: TicketItem, discount: Discount, disTotal: Double, approval: Approval?): Approval{
        val app = approval ?: createApproval(order)

        val ai = ApprovalItem(
            id = app.approvalItems.size,
            approvalType = "Discount Item",
            discount = discount.discountName,
            ticketItem = ticketItem,
            ticket = payment.activeTicket()!!,
            amount = disTotal,
            timeRequested = GlobalUtils().getNowEpoch(),
            approved = null,
            timeHandled = null,
            managerId = null
        )
        app.approvalItems.add(ai)

        return app
    }
}