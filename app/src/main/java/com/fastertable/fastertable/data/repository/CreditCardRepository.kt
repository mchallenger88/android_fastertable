package com.fastertable.fastertable.data.repository


import android.app.Application
import com.fastertable.fastertable.api.*
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.round
import javax.inject.Inject

class StartCredit @Inject constructor(private val startCreditUseCase: StartCreditUseCase){
    suspend fun startCreditProcess(url: String): TerminalResponse {
        val result = startCreditUseCase.startCredit(url)
        if (result is StartCreditUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class CancelCredit @Inject constructor(private val startCreditUseCase: StartCreditUseCase){
    suspend fun cancelCreditProcess(url: String): TerminalResponse {
        val result = startCreditUseCase.cancelCredit(url)
        if (result is StartCreditUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class ManualCreditAuthorization @Inject constructor(private val manualCreditAuthorizationUseCase: ManualCreditAuthorizationUseCase){
    suspend fun authorize(authorizationRequest: AuthorizationRequest): TransactionResponse45 {
        val result = manualCreditAuthorizationUseCase.authorize(authorizationRequest)
        if (result is ManualCreditAuthorizationUseCase.Result.Success){
            return result.response
        }else{
            throw java.lang.RuntimeException("fetch failed")
        }
    }
}

class StageTransaction @Inject constructor(private val stageResponseUseCase: StageResponseUseCase){
    suspend fun stageTransaction(transaction: CayanCardTransaction): Any{
        val result = stageResponseUseCase.stageTransaction(transaction)
        if (result is StageResponseUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class InitiateCreditTransaction @Inject constructor(private val initiateCreditTransactionUseCase: InitiateCreditTransactionUseCase){
    suspend fun initiateTransaction(url: String): CayanTransaction{
        val result = initiateCreditTransactionUseCase.initiateTransaction(url)
        if (result is InitiateCreditTransactionUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class AdjustTipTransaction @Inject constructor(private val adjustTipUseCase: AdjustTipUseCase){
    suspend fun adjustTip(request: AdjustTipTest): Any{
        val result = adjustTipUseCase.tipAdjust(request)
        println(result)
        if (result is AdjustTipUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}

class CaptureTicketTransaction @Inject constructor(private val captureRequestUseCase: CaptureRequestUseCase){
    suspend fun capture(request: CaptureRequest): TransactionResponse45{
        val result = captureRequestUseCase.capture(request)
        if (result is CaptureRequestUseCase.Result.Success){
            return result.response
        }else{
            throw RuntimeException("fetch failed")
        }
    }
}


class CreditCardRepository @Inject constructor(private val app: Application){

    fun createCayanTransaction(order: Order, ticket: Ticket, settings: Settings, terminal: Terminal, amountBeingPaid: Double): CayanCardTransaction {
            val salesType: String = "PREAUTH"
        // transactionType = "SALE",

            return CayanCardTransaction(
                merchantName = settings.merchantCredentials.MerchantName,
                merchantSiteId = settings.merchantCredentials.MerchantSiteId,
                merchantKey = settings.merchantCredentials.MerchantKey,
                request = CayanRequest(
                transactionType = salesType,
                amount = amountBeingPaid.round(2),
                clerkId = order.employeeId!!,
                orderNumber = order.orderNumber.toString(),
                dba = "fastertable",
                softwareName = "fastertable",
                softwareVersion = "1.18.0.0",
                transactionId = "",
                forceDuplicate = true,
                taxAmount = ticket.tax,
                terminalId = terminal.terminalId.toString(),
                ticketItems = null,
                enablePartialAuthorization = true
                ),
                requestWithTip = null,
        )}

    fun createCreditCardTransaction(ticket: Ticket, cayanTransaction: CayanTransaction): CreditCardTransaction{

        if (cayanTransaction.AdditionalParameters?.EMV != null){
            val e = EMV(
                    ApplicationInformation = null,
                    cardInformation = null,
                    applicationCryptogram = null,
                    cvmResults = null,
                    issuerApplicationData = null,
                    terminalVerificationResults = null,
                    unpredictableNumber = null,
                    amount = null,
                    posEntryMode = null,
                    terminalInformation = null,
                    transactionInformation = null,
                    cryptogramInformationData = null,
                    PINStatement = null,
                    cvmMethod = null,
                    issuerActionCodeDefault = null,
                    issuerActionCodeDenial = null,
                    issuerActionCodeOnline = null,
                    authorizationResponseCode = null
            )
            cayanTransaction.AdditionalParameters.EMV = e;

        };

        return CreditCardTransaction(
                ticketId = ticket.id,
                captureTotal = null,
                creditTotal = ticket.total,
                refundTotal = null,
                voidTotal = null,
                captureTransaction = null,
                creditTransaction = cayanTransaction,
                refundTransaction = null,
                voidTransaction = null,
                tipTransaction = null
        )
    }

    fun createManualCreditTransaction(response: TransactionResponse45, t: Ticket): CayanTransaction{
        val ct = CayanTransaction (
            AccountNumber = response.CardNumber,
            AdditionalParameters = null,
            AmountApproved = response.Amount,
            AuthorizationCode = response.AuthorizationCode,
            Cardholder = response.Cardholder,
            EntryMode = "Keyed",
            ErrorMessage = response.ErrorMessage,
            PaymentType = response.CardType,
            ResponseType = "",
            Status = response.ApprovalStatus,
            TipDetails = null,
            Token = response.Token,
            TransactionDate = response.TransactionDate,
            TransactionType = "AUTHORIZATION",
            ValidationKey = ""
        )

        val cc = CreditCardTransaction(
            ticketId = t.id,
            creditTotal = response.Amount.toDouble(),
            captureTotal = null,
            refundTotal = null,
            voidTotal = null,
            creditTransaction = ct,
            captureTransaction = null,
            refundTransaction = null,
            voidTransaction = null,
            tipTransaction = null)

        return ct
    }
}