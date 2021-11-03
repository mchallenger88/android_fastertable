package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class PaymentType {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVER,
    DEBIT,
    GIFT,
    EBT,
    LEVELUP,
    VOYAGER,
    WEX,
    JCB,
    CUP,
    UNKNOWN
}

enum class EntryType{
    SWIPE,
    PROXIMITY,
    BARCODE,
    MANUAL
}

enum class TransactionType{
    SALE,
    AUTHORIZATION,
    REFUND,
    ADDVALUE,
    BALANCEINQUIRY
}

enum class ResponseType{
    SINGLE,
    MULTI,
    COMPOUND
}

@Parcelize
data class CayanCardTransaction (
    val merchantName: String,
    val merchantSiteId: String,
    val merchantKey: String,
    val request: CayanRequest,
    val requestWithTip: RequestWithTip?,
): Parcelable

@Parcelize
data class CayanRequest(
    val transactionType: String,
    val amount: Double,
    val clerkId: String,
    val orderNumber: String,
    val dba: String,
    val softwareName: String = "fastertable",
    val softwareVersion: String = "1.18.0.0",
    val transactionId: String,
    val forceDuplicate: Boolean,
    val taxAmount: Double,
    val terminalId: String,
    val ticketItems: TransactionDetails?,
    val enablePartialAuthorization: Boolean,
): Parcelable

@Parcelize
data class RequestWithTip(
    val transactionType: String,
    val amount: Double,
    val clerkId: String,
    val orderNumber: String,
    val dba: String,
    val softwareName: String = "fastertable",
    val softwareVersion: String = "1.18.0.0",
    val transactionId: String,
    val forceDuplicate: Boolean,
    val taxAmount: Double,
    val tipDetails: TipDetails,
    val terminalId: String,
    val ticketItems: TransactionDetails,
    val enablePartialAuthorization: Boolean,
): Parcelable

@Parcelize
data class TransactionDetails(
    val ticketItems: ArrayList<TicketItem>
): Parcelable

@Parcelize
data class TipDetails(
    val eligibleAmount: Double,
): Parcelable

@Parcelize
data class StageResponse(
    val transportKey: String,
    val validationKey: String,
    val messages: String,
): Parcelable

@Parcelize
data class TerminalResponse(
    val Status: String,
    val ResponseMessage: String,
    val AdditionalParameters: String,
): Parcelable
@Parcelize
data class GiftTerminalResponse(
    val Status: String,
    val responseMessage:String,
    val additionalParameters: String,
): Parcelable
@Parcelize
data class MessageList(
    val field: String,
    val information: String,
): Parcelable
@Parcelize
data class CayanTransaction(
    val AccountNumber: String,
    val AdditionalParameters: AdditionalParameters?,
    val AmountApproved: String,
    val AuthorizationCode: String,
    val Cardholder: String,
    val EntryMode: String,
    val ErrorMessage: String,
    val PaymentType: String,
    val ResponseType: String,
    val Status: String,
    val TipDetails: TipDetails?,
    val Token: String,
    val TransactionDate: String?,
    val TransactionType: String,
    val ValidationKey: String,
): Parcelable

@Parcelize
data class NewCayanTransaction(
    val AccountNumber: String,
    val AmountApproved: String,
    val AuthorizationCode: String,
    val Cardholder: String,
    val EntryMode: String,
    val ErrorMessage: String,
    val PaymentType: String,
    val ResponseType: String,
    val Status: String,
    val TipDetails: TipDetails,
    val Token: String,
    val TransactionDate: String?,
    val TransactionType: String,
    val ValidationKey: String,
): Parcelable

@Parcelize
data class EMV(
    val ApplicationInformation: ApplicationInformation?,
    val cardInformation: CardInformation?,
    val applicationCryptogram: ApplicationCryptogram?,
    val cvmResults: String?,
    val issuerApplicationData: String?,
    val terminalVerificationResults: String?,
    val unpredictableNumber: String?,
    val amount: Amount?,
    val posEntryMode: String?,
    val terminalInformation: TerminalInformation?,
    val transactionInformation: TransactionInformation?,
    val cryptogramInformationData: String?,
    val PINStatement: String?,
    val cvmMethod: String?,
    val issuerActionCodeDefault: String?,
    val issuerActionCodeDenial: String?,
    val issuerActionCodeOnline: String?,
    val authorizationResponseCode: String?,
): Parcelable
@Parcelize
data class ApplicationInformation(
    val Aid: String,
    val ApplicationLabel: String,
    val applicationExpiryDate: String?,
    val applicationEffectiveDate: String?,
    val applicationInterchangeProfile: String,
    val applicationVersionNumber: String,
    val applicationTransactionCounter: String,
    val applicationUsageControl: String,
    val applicationPreferredName: String,
    val applicationDisplayName: String,
): Parcelable
@Parcelize
data class CardInformation(
    val maskedPan: String,
    val panSequenceNumber: String,
    val cardExpiryDate: String?,
): Parcelable
@Parcelize
data class ApplicationCryptogram(
    val cryptogramType: String,
    val cryptogram: String,
): Parcelable
@Parcelize
data class Amount(
    val amountAuthorized: Double,
    val amountOther: Double,
): Parcelable
@Parcelize
data class TerminalInformation(
    val terminalType: String,
    val ifdSerialNumber: String,
    val terminalCountryCode: String,
    val terminalID: String,
    val terminalActionCodeDefault: String,
    val terminalActionCodeDenial: String,
    val terminalActionCodeOnline: String,
): Parcelable
@Parcelize
data class TransactionInformation(
    val transactionType: String,
    val transactionCurrencyCode: String,
    val transactionStatusInformation: String,
): Parcelable
@Parcelize
data class AdditionalParameters(
    val AmountDetails: AmountDetails,
    val SignatureData: String,
    val EBTDetails: EBTDetails?,
    val KeyedDetails: KeyedDetails?,
    val Loyalty: Loyalty?,
    val Survey: Survey?,
    val DebitTraceNumber: String?,
    var EMV: EMV?,
): Parcelable

@Parcelize
data class AmountDetails(
    val Cashback: String,
    val Discount: CayanDiscount?,
    val Donation: String,
    val RemainingCardBalance: String,
    val Surcharge: String,
    val UserTip: String,
): Parcelable

@Parcelize
data class CayanDiscount(
    val Total: String
): Parcelable

@Parcelize
data class EBTDetails(
    val EBTType: String,
    val FnsId: String,
    val Balances: Balances
): Parcelable
@Parcelize
data class Balances (
    val CashAvailableBalance: Double,
    val SnapAvailableBalance: Double,
    val PointsBalance: Double?,
    val AmountBalance: Double?
): Parcelable
@Parcelize
data class KeyedDetails(
    val ExpirationDate: String?,
    val AvsStreetZipCode: String,
    val AvsResponse: String,
    val CvResponse: String,
): Parcelable
@Parcelize
data class Loyalty(
    val AccountNumber: String,
    val Balances: Balances
): Parcelable
@Parcelize
data class Survey(
    val MerchantMessage: String,
    val CustomerMessage: String
): Parcelable

@Parcelize
data class MerchantCredentials(
    val MerchantName: String,
    val MerchantSiteId: String,
    val MerchantKey: String,
    val webAPIKey: String,
    val stripePrivateKey: String,
    val stripePublickey: String,
): Parcelable
@Parcelize
data class TipRequest(
    val Token: String,
    val Amount: Double,
): Parcelable
@Parcelize
data class CaptureRequest(
    val Credentials: MerchantCredentials,
    val Capture: Capture,
): Parcelable
@Parcelize
data class AuthorizationRequest(
    val Credentials: MerchantCredentials,
    val PaymentData: AuthPaymentData,
    val Request: AuthRequestData,
): Parcelable

@Parcelize
data class WebAuthorizationRequest(
    val Credentials: MerchantCredentials,
    val PaymentData: WebAuthPaymentData,
    val Request: WebAuthRequestData
): Parcelable
@Parcelize
data class AuthPaymentData(
    val source: String,
    val cardNumber: String,
    val expirationDate: String,
    val cardHolder: String,
    val avsStreetAddress: String,
    val avsZipCode: String,
    val cardVerificationValue: String,
): Parcelable

@Parcelize
data class WebAuthPaymentData(
    val Source: String,
    val VaultToken: String,
): Parcelable
@Parcelize
data class WebAuthRequestData(
    val Amount: Double,
    val InvoiceNumber: String,
    val registerNumber: String,
    val merchantTransactionId: String,
    val cardAcceptorTerminalId: String,
    val CustomerEmailAddress: String,
): Parcelable
@Parcelize
data class AuthRequestData(
    val amount: Double,
    val invoiceNumber: String,
    val registerNumber: String,
    val merchantTransactionId: String,
    val cardAcceptorTerminalId: String,

): Parcelable
@Parcelize
data class Capture(
    val Token: String,
    val Amount: Double,
    val InvoiceNumber: String,
    val RegisterNumber: String,
    val MerchantTransactionId: String,
    val CardAcceptorTerminalId: String?,
): Parcelable
@Parcelize
data class TransactionResponse45(
    var ApprovalStatus: String,
    var Token: String,
    var AuthorizationCode: String,
    var TransactionDate: String,
    var Amount: String,
    var RemainingCardBalance: String,
    var CardNumber: String,
    var Cardholder: String,
    var CardType: String,
    var FsaCard: String,
    var ReaderEntryMode: String,
    var AvsResponse: String,
    var CvResponse: String,
    var ErrorMessage: String,
    var ExtraData: String,
    var FraudScoring: String?,
    var Rfmiq: String?,
): Parcelable

@Parcelize
data class RefundRequest(
    val credentials: MerchantCredentials,
    val paymentData: PaymentData,
    val request: RefundRequestData,
): Parcelable
@Parcelize
data class PaymentData(
    val source: String,
    val token: String,
): Parcelable
@Parcelize
data class RefundRequestData(
    val amount: Double,
    val invoiceNumber: String,
    val registerNumber: String,
    val merchantTransactionId: String,
    val cardAcceptorTerminalId: String?,
): Parcelable
@Parcelize
data class LineItem(
    val commodityCode: String,
    val description: String,
    val upc: String,
    val quantity: Int,
    val unitOfMeasure: String,
    val unitCost: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val taxAmount: Double,
    val extendedAmount: Double,
    val debitOrCreditIndicator: String,
    val netOrGrossIndicator: String,
): Parcelable
@Parcelize
data class DetailsTransportKeyRequest(
    val name: String,
    val siteID: String,
    val key: String,
    val transportKey: String,
): Parcelable
@Parcelize
data class DetailsTransportKeyResponse(
    val status: String,
    val errorMessage: String,
    val totalAmountApproved: String,
    val requestedAmount: String,
    val responseType: String,
    val paymentDetails: PaymentDetails,

    ): Parcelable
@Parcelize
data class PaymentDetails(
    val paymentDetail: PaymentDetail,
): Parcelable
@Parcelize
data class PaymentDetail(
    val paymentType: String,
    val status: String,
    val errorMessage: String,
    val transactionType: String,
    val token: String,
    val authorizationCode: String,
    val customer: String,
    val email: String,
    val phoneNumber: String,
    val accountNumber: String,
    val expirationDate: String,
    val entryMode: String,
    val transactionDate: String,
    val amountDetail: AmountDetail,
    val signatureDetail: SignatureDetail,
    val giftDetail: GiftDetail,
    val loyaltyDetail: LoyaltyDetail,
    val additionalResponseParameters: AdditionalResponseParameters,
): Parcelable
@Parcelize
data class AmountDetail(
    val amountApproved: String,
    val amountCharged: String,
    val taxAmount: String,
    val tipAmount: String,
    val userTipAmount: String,
    val discountAmount: String,
    val voucherAmount: String,
    val remainingCardBalance: String,
): Parcelable
@Parcelize
data class SignatureDetail(
    val signatureType: String,
    val signature: String,
): Parcelable
@Parcelize

data class GiftDetail(
    val balance: String,
): Parcelable

@Parcelize
data class LoyaltyDetail(
    val visits: String,
    val lastVisit: String,
    val lifetimeSpend: String,
): Parcelable

@Parcelize
data class AdditionalResponseParameters(
    val fsaCard: String,
): Parcelable

@Parcelize
data class AdjustTip(
    val credentials: MerchantCredentials,
    val tipRequest: TipRequest,
): Parcelable

@Parcelize
data class AdjustTipTest(
    val MerchantName: String,
    val MerchantSiteId: String,
    val MerchantKey: String,
    val Token: String,
    val Amount: String,
): Parcelable

@Parcelize
data class PaymentIntentRequest(
    val key: String,
    val payment: Payment,
): Parcelable