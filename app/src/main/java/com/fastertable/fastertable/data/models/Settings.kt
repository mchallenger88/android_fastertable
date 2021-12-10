package com.fastertable.fastertable.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlin.collections.ArrayList

@Parcelize
data class Settings (
    val id: String,
    @SerializedName("locationid")
    val locationId: String,
    val discounts: ArrayList<Discount>,
    val taxRate: TaxRate,
    val printers: ArrayList<Printer>,
    val ccEquipment: ArrayList<CreditCardEquipment>,
    val terminals: ArrayList<Terminal>,
    val dailyResetOrderTime: String?,
    val mandatoryGratuity: MandatoryGratuity,
    val tipShare: TipShare,
    val deliveryServices: ArrayList<DeliveryService>,
    val companyId: String,
    val merchantCredentials: MerchantCredentials,
    val defaultState: String,
    val deliveryService: Boolean,
    val deliveryFee: Double,
    val counterService: Boolean,
    val tableService: Boolean,
    val takeoutService: Boolean,
    val restaurantType: String,
    val timeouts: Timeouts,
    val kitchenModule: Boolean,
    val kitchenPrinting: Boolean,
    val kitchenTicketPrinting: Boolean,
    val kitchenTerminals: ArrayList<KitchenTerminal>,
    val prepStations: ArrayList<PrepStation>,
    val requireCheckoutConfirm: Boolean,
    val useMobileCredit: Boolean,
    val creditCardTipDiscount: Double,
    val showCounterOrderDialog: Boolean,
    val hoursOfOperation: OperatingHours,
    val tipSettlementPeriod: String,
    val additionalFees: ArrayList<AdditionalFees>?
): Parcelable {
    fun getPrepStation(printerName: String): PrepStation{
        val newStation = prepStations.find{it.stationName == printerName}
        return newStation ?: prepStations[0]
    }
}

@Parcelize
data class AdditionalFees(
    val id: Int,
    val name: String,
    val feeType: FeeType,
    val amount: Double,
    var checkAmount: Double?,
    val checkMessage: String
): Parcelable

enum class FeeType{
    FlatFee, Percentage
}

@Parcelize
data class OperatingHours(
    val sunday: OpeningClosing,
    val monday: OpeningClosing,
    val tuesday: OpeningClosing,
    val wednesday: OpeningClosing,
    val thursday: OpeningClosing,
    val friday: OpeningClosing,
    val saturday: OpeningClosing,
): Parcelable

@Parcelize
data class OpeningClosing(
    val open: Long,
    val close: Long,
    val closed: Boolean,
): Parcelable

@Parcelize
data class KitchenTerminal(
    val id: String,
    val prepStation: PrepStation,
    val usePrinting: Boolean,
    val printer: Printer,
    val master: Boolean,
    val showWholeTicket: Boolean,
    val showBeverages: Boolean,
): Parcelable

@Parcelize
data class  Discount(
    val id: String,
    val discountName: String,
    val discountType: String,  //Flat Amount or Percentage
    val discountAmount: Double,
    val active: Boolean,
    val approvalRequired: Boolean,
): Parcelable

@Parcelize
data class TaxRate(
    val rate: Double,
): Parcelable

@Parcelize
data class Timeouts(
    val fohTimeout: Int,
    val backofficeTimeout: Int,
    val kitchenTimeout: Int,
): Parcelable

@Parcelize
data class Printer(
    val id: String,
    val printerName: String,
    val printerType: String, //receipt/kitchen/label
    val printerModel: String,
    val backupPrinter: String,
    val ipAddress: String,
    val master: Boolean,
): Parcelable

@Parcelize
data class  MandatoryGratuity(
    val guestNumber: Int,
    val gratuityPercentage: Double,
): Parcelable

@Parcelize
data class TipShare(
    val bartender: Double,
    val busboy: Double,
): Parcelable

@Parcelize
data class DeliveryService(
    val name: String,
): Parcelable

@Parcelize
data class CreditCardEquipment(
    val id: Int,
    val equipment: String,
    val ipAddress: String,
): Parcelable

@Parcelize
data class Terminal(
    val terminalId: Int,
    val terminalName: String,
    val terminalType: String,
    val ccEquipment: CreditCardEquipment,
    val defaultPrinter: Printer,
    val backupPrinter: Printer,
): Parcelable

@Parcelize
data class PrinterUpdateTransaction(
    val printer: Printer,
    val locationId: String,
    val companyId: String
): Parcelable

@Parcelize
data class PrinterUpdate(
    val settings: Settings,
    val oldPrinter: Printer,
    val newPrinter: Printer,
): Parcelable

@Parcelize
data class PrepStation(
    val id: String,
    val stationName: String
): Parcelable