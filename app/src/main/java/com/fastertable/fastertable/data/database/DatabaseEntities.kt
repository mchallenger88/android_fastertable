package com.fastertable.fastertable.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

//class DatabaseEntities {
//
//    @Entity(tableName = "settings_table")
//    data class SettingsData constructor(
//        @PrimaryKey()
//        val id: String,
//        val locationId: String,
//        val taxRate: Double,
//        val dailyResetOrderTime: String,
//        val manGratuity_GuestNumber: Int,
//        val manGratuity_Percentage: Double,
//        val tipShare_Bartender: Double,
//        val tipShare_Busboy: Double,
//        val companyId: String,
//        val merchantCredentials_merchantName: String,
//        val merchantCredentials_merchantSiteId: String,
//        val merchantCredentials_merchantKey: String,
//        val merchantCredentials_webAPIKey: String,
//        val defaultState: String,
//        val deliveryService: Boolean,
//        val deliveryFee: Double,
//        val counterService: Boolean,
//        val tableService: Boolean,
//        val takeoutService: Boolean,
//        val restaurantType: String,
//        val fohTimeout: Int,
//        val backOfficeTimeout: Int,
//        val kitchenTimeout: Int,
//        val prepStation_Id: String,
//        val requireCheckoutConfirm: Boolean,
//        val useMobileCredit: Boolean,
//        val creditCardTipDiscount: Double,
//        val showCounterOrderDialog: Boolean,
//        val hoursOfOperation: OperatingHours
//    )
//
//    @Entity(tableName = "operatingHours_table")
//    data class OperatingHours(
//        @PrimaryKey()
//        val weekDay: Int,
//        val open: Long,
//        val close: Long,
//        val closed: Boolean
//    )
//
//    @Entity(tableName = "prepStation_table")
//    data class PrepStation constructor(
//        @PrimaryKey()
//        val id: String,
//        val name: String
//    )
//
//    @Entity(tableName = "kitchenTerminal_table")
//    data class KitchenTerminalData constructor(
//        @PrimaryKey()
//        val id: String,
//        val prepStation_Id: String,
//        val usePrinting: Boolean,
//        val printerId: String,
//        val master: Boolean,
//        val showWholeTicket: Boolean,
//        val showBeverages: Boolean
//    )
//
//    @Entity(tableName = "deliveryService_table")
//    data class DeliveryServiceData constructor(
//        @PrimaryKey(autoGenerate = true)
//        val id: Int,
//        val name: String
//    )
//
//    @Entity(tableName = "terminal_table")
//    data class TerminalData constructor(
//        @PrimaryKey()
//        val terminalId: Int,
//        val terminalName: String,
//        val terminalType: String,
//        val ccId: Int,
//        val defaultPrinterId: String,
//        val backupPrinterId: String
//    )
//
//    @Entity(tableName = "ccEquipment_table")
//    data class CCEquipmentData constructor(
//        @PrimaryKey(autoGenerate = true)
//        val id: Int,
//        val equipment: String,
//        val ipAddress: String
//    )
//
//    @Entity(tableName = "printers_table")
//    data class PrinterData constructor(
//        @PrimaryKey()
//        val id: String,
//        val printerName: String,
//        val printerType: String,
//        val printerModel: String,
//        val backupPrinter: String,
//        val ipAddress: String,
//        val master: Boolean
//    )
//
//    @Entity(tableName = "discount_table")
//    data class DiscountData constructor(
//        @PrimaryKey()
//        val id: String,
//        val discountName: String,
//        val discountType: String,
//        val discountAmount: Double,
//        val active: Boolean,
//        val approvalRequired: Boolean
//    )
//
//    @Entity(tableName = "order_table")
//    data class DataOrder constructor(
//        @PrimaryKey()
//        val id: String,
//        val orderType: String,
//        val orderNumber: Int,
//        val tableNumber: Int?,
//        val employeeId: String,
//        val userName: String,
//        val startTime: Long,
//        val closeTime: Long?,
//        val midnight: Long,
//        val orderStatus: String,
//        val kitchenStatus: Boolean,
//        val rush: Boolean?,
////        @Embedded
////        var guests: List<DataGuest>?,
////        var splitChecks: MutableList<Check>?,
////        var note: String?,
////
////        var customer: Customer?,
////        var takeOutCustomer: TakeOutCustomer,
////        var outsideDelivery: DeliveryCustomer?,
////
////        val orderFees: Double?,
////        val orderDiscount: Double?,
////        val pendingApproval: Boolean,
////
////        val gratuity: Double,
////        val subTotal: Double,
////        val tax: Double,
////        val total: Double,
////
////        var accepted: Boolean?,
////        var estReadyTime: Long?,
////        var estDeliveryTime: Long?,
////
////
////        val locationId: String,
////        val archived: Boolean,
////        val type: String,
////        val _rid: String?,
////        val _self: String?,
////        val _etag: String?,
////        val _attachments: String?,
////        val _ts: Long?
//
//    )
//
//    @Entity(tableName = "guest_table")
//    data class DataGuest constructor(
//        @PrimaryKey()
//        val id: Int,
//        val startTime: Long,
////        var orderItems: MutableList<OrderItem>?,
//        var subTotal: Double?,
//        var tax: Double?,
//        var gratuity: Double?,
//        var total: Double?
//    )
//}