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
import java.util.UUID

class SaveApproval @Inject constructor(private val saveApprovalUseCase: SaveApprovalUseCase,
                                      private val approvalRepository: ApprovalRepository){
    suspend fun saveApproval(approval: Approval): Approval{
        val p: Approval
        val result = saveApprovalUseCase.saveApproval(approval)
        if (result is SaveApprovalUseCase.Result.Success){
            p = result.approval
            approvalRepository.saveApproval(p)
            return p
        }else{
            throw RuntimeException("fetch failure")
        }
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
            return result.approval
        }else{
            throw RuntimeException("fetch failure")
        }
    }
}

class GetApprovalsById @Inject constructor(private val getApprovalUseCase: GetApprovalUseCase,
                                           private val approvalRepository: ApprovalRepository){

    suspend fun getAllPaymentApprovals(id: String, lid: String): List<Approval>?{
        val approvals: List<Approval>?
        val result = getApprovalUseCase.getApprovalsById(id, lid)
        if (result is GetApprovalUseCase.Result.Success){
            approvals = result.approvals
            approvalRepository.savePaymentApprovals(approvals)
            return approvals
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class GetApprovals @Inject constructor(private val getApprovalsUseCase: GetApprovalsUseCase,
                                      private val approvalRepository: ApprovalRepository){

    suspend fun getApprovals(timeBasedRequest: TimeBasedRequest): List<ApprovalOrderPayment>{
        val approvals: List<ApprovalOrderPayment>
        val result = getApprovalsUseCase.getApprovals(timeBasedRequest)
        if (result is GetApprovalsUseCase.Result.Success){
            approvals = result.approvals
            approvalRepository.saveApprovalOrderPayments(approvals)
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

    fun getApprovalOrderPaymentsFromFile(): List<ApprovalOrderPayment>?{
        val gson = Gson()
        if (File(app.filesDir, "approvalorderpayments.json").exists()) {
            val bufferedReader: BufferedReader =
                File(app.filesDir, "approvalorderpayments.json").bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            val arrayList: ArrayList<ApprovalOrderPayment> = gson.fromJson(
                inputString,
                object : TypeToken<List<ApprovalOrderPayment?>?>() {}.type
            )
            return arrayList
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

    fun updateApprovalOrderPayment(a: ApprovalOrderPayment){
        val approvals = getApprovalOrderPaymentsFromFile() as MutableList<ApprovalOrderPayment>
        val aop = approvals.find { it.approval.id == a.approval.id }
        val index = approvals.indexOf(aop)
        if (aop != null){
            approvals[index] = aop
            saveApprovalOrderPayments(approvals)
        }
    }

    fun savePaymentApprovals(approvals: List<Approval>?){
        //Save approvals json to file
        val gson = Gson()
        val jsonString = gson.toJson(approvals)
        val file= File(app.filesDir, "paymentApprovals.json")
        file.writeText(jsonString)
    }

    fun saveApprovals(approvals: List<Approval>){
        //Save approvals json to file
        val gson = Gson()
        val jsonString = gson.toJson(approvals)
        val file= File(app.filesDir, "approvals.json")
        file.writeText(jsonString)
    }

    fun saveApprovalOrderPayments(approvals: List<ApprovalOrderPayment>){
        //Save approvals json to file
        val gson = Gson()
        val jsonString = gson.toJson(approvals)
        val file= File(app.filesDir, "approvalorderpayments.json")
        file.writeText(jsonString)
    }

    fun createApproval(payment: Payment, approvalType: String, ticket: Ticket, ticketItem: TicketItem?, newItemPrice: Double?, discount: Discount?, user: String): Approval {
        val id = UUID.randomUUID()

        var discountName = ""
        if (discount != null){
            discountName = discount.discountName
        }

        val approval = Approval(
            id = id.toString(),
            approvalType = approvalType,
            ticketId = ticket.id,
            ticketItemId = ticketItem?.id,
            whoRequested = user,
            timeRequested = GlobalUtils().getNowEpoch(),
            newItemPrice = newItemPrice,
            discount = discountName,
            approved = null,
            timeHandled = null,
            managerId = null,
            paymentId = payment.id,
            type = "Approval",
            locationId = payment.locationId,
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
}