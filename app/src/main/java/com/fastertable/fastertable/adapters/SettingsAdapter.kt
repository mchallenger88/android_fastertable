package com.fastertable.fastertable.adapters

import android.app.Application
import com.beust.klaxon.Klaxon
import com.fastertable.fastertable.data.*
import com.google.gson.Gson
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SettingsAdapter() {

    fun saveSettings(app: Application, settings: Settings){
        val gson = Gson()
        val jsonString = gson.toJson(settings)
        val file= File(app.filesDir, "settings.json")
        file.writeText(jsonString)
    }


}



data class SettingsTesting (
        val id: String,
        val locationid: String,
        val discounts: ArrayList<Discount>,
        val taxRate: TaxRate,
        val printers: ArrayList<Printer>,
        val ccEquipment: ArrayList<CreditCardEquipment>,
        val terminals: ArrayList<Terminal>,
        val dailyResetOrderTime: Date?,
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
)