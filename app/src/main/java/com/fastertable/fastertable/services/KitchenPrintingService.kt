package com.fastertable.fastertable.services

import android.util.Log
import com.beust.klaxon.internal.firstNotNullResult
import com.fastertable.fastertable.api.KitchenPrintUseCase
import com.fastertable.fastertable.api.ReceiptPrintUseCase
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.utils.round
import technology.master.kotlinprint.printer.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class PrintTicketService {
    fun masterTicket(document: Document, order: Order){
        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))

        document.alignment("center")
            .text("MASTER TICKET", TextSettings(bold = true))
            .newLine()
            .newLine()
            .text("--- " + order.orderType.uppercase(Locale.getDefault()) + " ORDER ---", TextSettings(bold = true))
            .newLine()

        if(order.orderType == "Delivery"){
            val telephone = order.outsideDelivery?.telephone
            document
                .text(order.outsideDelivery?.deliveryService?.uppercase(Locale.getDefault()), TextSettings(bold = true))
                .text("Customer: " + order.outsideDelivery?.name)
                .text("Telephone: " + formatPhone(telephone))
                .text("Order No.: " + order.outsideDelivery?.orderNumber)
        }


        if(order.orderType == "Takeout"){
            document
                .alignment("center").text("Name: " + order.takeOutCustomer?.name?.uppercase(Locale.getDefault()), TextSettings(bold = true))

            if(order.takeOutCustomer?.telephone != ""){
                document.newLine()
                    .alignment("center").text(formatPhone(order.takeOutCustomer?.telephone))
            }

            if (order.takeOutCustomer?.notes != ""){
                document.newLine()
                    .alignment("center").text(order.takeOutCustomer?.notes.toString())
            }

            document
                .newLine()
        }

        document
            .newLine()
            .alignment("right").text(date)
            .newLine()
            .alignment("right").text(time)
            .newLine()
            .alignment("left").text("Order: " + order.orderNumber.toString(), TextSettings(bold = true))
            .newLine()
            .text("Server: " + order.userName)

        if (order.tableNumber != null){
            document
                .text("Table: " + order.tableNumber.toString())
                .newLine()
        }else{
            document
                .newLine()
        }
        document
            .newLine()

        var g: String
        for (i in 1..order.guestCount){
            document
                .color(PrinterDriver.COLOR.BLACK)
                .alignment("center")
                .text("GUEST $i")
                .newLine()
            val list = order.orderItems?.filter { it.guestId == i }
            list?.let{ oiList ->
                for (item in oiList){
                if (item.status == "Started" && item.salesCategory == "Food"){
                    document
                        .alignment("left")

                    addMenuItemLine(document, item, order.orderType)

                    var ing: String
                    if (item.activeModItems().isNotEmpty()){
                        item.activeModItems().forEach{ m ->
                            document
                                .color(PrinterDriver.COLOR.RED)
                                .text("  " + m.itemName.uppercase(Locale.getDefault()), TextSettings(bold = true))
                                .color(PrinterDriver.COLOR.BLACK)
                                .newLine()
                        }
                    }
                        item.ingredients?.forEach{ i ->
                            ing =
                                when (i.orderValue){
                                    0 -> "NO " + i.name.uppercase(Locale.getDefault())
                                    2 -> "EXTRA " + i.name.uppercase(Locale.getDefault())
                                    else -> ""
                                }
                            if (ing != ""){
                                document
                                    .color(PrinterDriver.COLOR.RED)
                                    .text("  $ing", TextSettings(bold = true))
                                    .color(PrinterDriver.COLOR.BLACK)
                                    .newLine()
                            }
                        }
                    item.note?.let {
                        if (it != ""){
                            document
                                .text("  Notes: " + item.note)
                                .newLine()
                        }
                    }
                    document.newLine()
                }}}
            }

        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
    }

    fun resendMasterTicket(document: Document, order: Order){
        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))

        document.alignment("center")
            .text("MASTER TICKET", TextSettings(bold = true))
            .newLine()
            .newLine()
            .text("--- " + order.orderType.uppercase(Locale.getDefault()) + " ORDER ---", TextSettings(bold = true))
            .newLine()

        if(order.orderType == "Delivery"){
            val telephone = order.outsideDelivery?.telephone
            document
                .text(order.outsideDelivery?.deliveryService?.uppercase(Locale.getDefault()), TextSettings(bold = true))
                .text("Customer: " + order.outsideDelivery?.name)
                .text("Telephone: " + formatPhone(telephone))
                .text("Order No.: " + order.outsideDelivery?.orderNumber)
        }


        if(order.orderType == "Takeout"){
            document
                .alignment("center").text("Name: " + order.takeOutCustomer?.name?.uppercase(Locale.getDefault()), TextSettings(bold = true))

            if(order.takeOutCustomer?.telephone != ""){
                document.newLine()
                    .alignment("center").text(formatPhone(order.takeOutCustomer?.telephone))
            }

            if (order.takeOutCustomer?.notes != ""){
                document.newLine()
                    .alignment("center").text(order.takeOutCustomer?.notes.toString())
            }

            document
                .newLine()
        }

        document
            .newLine()
            .alignment("right").text(date)
            .newLine()
            .alignment("right").text(time)
            .newLine()
            .alignment("left").text("Order: " + order.orderNumber.toString(), TextSettings(bold = true))
            .newLine()
            .text("Server: " + order.userName)

        if (order.tableNumber != null){
            document
                .text("Table: " + order.tableNumber.toString())
                .newLine()
        }else{
            document
                .newLine()
        }
        document
            .newLine()

        var g: String
        for (i in 1..order.guestCount){
            document
                .color(PrinterDriver.COLOR.BLACK)
                .alignment("center")
                .text("GUEST $i")
                .newLine()
            val list = order.orderItems?.filter { it.guestId == i }
            list?.let { oiList ->
            for (item in oiList){
                if (item.salesCategory == "Food"){
                    document
                        .alignment("left")

                    addMenuItemLine(document, item, order.orderType)

                    var ing: String
                    if (item.activeModItems().isNotEmpty()){
                        item.activeModItems().forEach{ m ->
                            document
                                .color(PrinterDriver.COLOR.RED)
                                .text("  " + m.itemName.uppercase(Locale.getDefault()), TextSettings(bold = true))
                                .color(PrinterDriver.COLOR.BLACK)
                                .newLine()
                        }
                    }
                    item.ingredients?.forEach{ i ->
                        ing =
                            when (i.orderValue){
                                0 -> "NO " + i.name.uppercase(Locale.getDefault())
                                2 -> "EXTRA " + i.name.uppercase(Locale.getDefault())
                                else -> ""
                            }
                        if (ing != ""){
                            document
                                .color(PrinterDriver.COLOR.RED)
                                .text("  $ing", TextSettings(bold = true))
                                .color(PrinterDriver.COLOR.BLACK)
                                .newLine()
                        }
                    }

                    item.note?.let{
                        if (it != "") {
                            document
                                .text("  Notes: " + item.note)
                                .newLine()
                        }
                    }
                    document.newLine()
                }}}
        }

        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
    }

    private fun addMenuItemLine(document: Document, item: OrderItem, orderType: String) {
        var line = ""
        if (item.menuItemPrice.quantity > 1){
            line += "Qty " + item.menuItemPrice.quantity + "x "
        }

        if (item.menuItemPrice.size != "Regular"){
            line += item.menuItemPrice.size.uppercase(Locale.getDefault()) + " "
        }

        line += item.menuItemName.uppercase(Locale.getDefault())

        if (orderType == "Takeout" || item.takeOutFlag){
            line += " (To Go)"
        }

        if (item.rush){
            line += " (RUSH)"
        }

        if (item.dontMake){
            line += " (No Make)"
        }
        document.color(PrinterDriver.COLOR.BLACK)
        document.text(line, TextSettings(size = 24f, bold = true))
            .newLine()
    }

    private fun addAdditionalFees(document: Document, ticket: Ticket){
        ticket.extraFees?.let{ list ->
            for (fee in list){
                fee.checkAmount?.let {
                    document
                        .alignment("right")
                        .text(formatDouble("${fee.name}:", it))
                        .newLine()
                }

            }
        }

    }

    fun kitchenTicket(document: Document, order: Order, printer: Printer): Document {
        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))

        document
            .alignment("center")
            .text("*** " + printer.printerName.uppercase(Locale.getDefault()) + " ***", TextSettings(bold = true))
            .newLine()
            .text("--- " + order.orderType.uppercase(Locale.getDefault()) + " ORDER ---", TextSettings(bold = true))
            .newLine()

        if(order.orderType == "Delivery"){
            document
                .text(order.outsideDelivery?.deliveryService?.uppercase(Locale.getDefault()), TextSettings(bold = true))
                .text("Customer: " + order.outsideDelivery?.name)
                .text("Telephone: " + formatPhone(order.outsideDelivery?.telephone))
                .text("Order No.: " + order.outsideDelivery?.orderNumber)
                .newLine()
        }


        if(order.orderType == "Takeout"){
            document
                .alignment("center").text("Name: " + order.takeOutCustomer?.name?.uppercase(Locale.getDefault()), TextSettings(bold = true))

            if(order.takeOutCustomer?.telephone != null){
                document
                    .alignment("center").text(this.formatPhone(order.takeOutCustomer?.telephone))
            }

            document
                .newLine()
        }


        document
            .newLine()
            .alignment("right").text(date)
            .newLine()
            .alignment("right").text(time)
            .newLine()
            .alignment("left").text("Order: " + order.orderNumber.toString(), TextSettings(bold = true))
            .newLine()
            .text("Server: " + order.userName)
            .newLine()

        if (order.tableNumber != null){
            document
                .text("Table: " + order.tableNumber.toString())
                .newLine()
        }else{
            document
                .newLine()
        }
        document
            .newLine()

        for (i in 1..order.guestCount){
            val list = order.orderItems?.filter { it.guestId == i && it.printer.printerName == printer.printerName }

            list?.let{
                document
                    .color(PrinterDriver.COLOR.BLACK)
                    .alignment("center").text("GUEST $i")
                    .newLine()
                for (item in list){
                    if (item.status == "Started"){
                        if (item.printer.id == printer.id){
                            document
                                .alignment("left")
                            this.addMenuItemLine(document, item, order.orderType)


                            var ing: String

                            item.activeModItems().forEach{ m ->
                                document
                                    .color(PrinterDriver.COLOR.RED)
                                    .text("  " + m.itemName, TextSettings(bold = true))
                                    .color(PrinterDriver.COLOR.BLACK)
                                    .newLine()
                            }

                            item.ingredients?.forEach{ i ->
                                ing =
                                    when (i.orderValue){
                                        0 -> "NO " + i.name.uppercase(Locale.getDefault())
                                        2 -> "EXTRA " + i.name.uppercase(Locale.getDefault())
                                        else -> ""
                                    }
                                if (ing != ""){
                                    document
                                        .color(PrinterDriver.COLOR.RED)
                                        .text("  $ing", TextSettings(bold = true))
                                        .color(PrinterDriver.COLOR.BLACK)
                                        .newLine()
                                }
                            }

                            item.note?.let{
                                if (it != "") {
                                    document
                                        .text("  Notes: " + item.note)
                                        .newLine()
                                }
                            }
                            document.newLine()
                        }}
                }
            }}

            order.note?.let{
                if (it != "") {
                    document.text("NOTES:")
                        .newLine()
                        .text(order.note)
                }
            }
        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
        return document
    }

    fun resendKitchenTicket(document: Document, order: Order, printer: Printer): Document {
        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(order.startTime))

        document
            .alignment("center")
            .text("*** " + printer.printerName.uppercase(Locale.getDefault()) + " ***", TextSettings(bold = true))
            .newLine()
            .text("--- " + order.orderType.uppercase(Locale.getDefault()) + " ORDER ---", TextSettings(bold = true))
            .newLine()

        if(order.orderType == "Delivery"){
            document
                .text(order.outsideDelivery?.deliveryService?.uppercase(Locale.getDefault()), TextSettings(bold = true))
                .text("Customer: " + order.outsideDelivery?.name)
                .text("Telephone: " + formatPhone(order.outsideDelivery?.telephone))
                .text("Order No.: " + order.outsideDelivery?.orderNumber)
                .newLine()
        }


        if(order.orderType == "Takeout"){
            document
                .alignment("center").text("Name: " + order.takeOutCustomer?.name?.uppercase(Locale.getDefault()), TextSettings(bold = true))

            if(order.takeOutCustomer?.telephone != null){
                document
                    .alignment("center").text(this.formatPhone(order.takeOutCustomer?.telephone))
            }

            document
                .newLine()
        }


        document
            .newLine()
            .alignment("right").text(date)
            .newLine()
            .alignment("right").text(time)
            .newLine()
            .alignment("left").text("Order: " + order.orderNumber.toString(), TextSettings(bold = true))
            .newLine()
            .text("Server: " + order.userName)
            .newLine()

        if (order.tableNumber != null){
            document
                .text("Table: " + order.tableNumber.toString())
                .newLine()
        }else{
            document
                .newLine()
        }
        document
            .newLine()

        for (i in 1..order.guestCount){
            val list = order.orderItems?.filter { it.guestId == i && it.printer.printerName == printer.printerName }

            list?.let{
                document
                    .color(PrinterDriver.COLOR.BLACK)
                    .alignment("center").text("GUEST $i")
                    .newLine()
                for (item in list){
                        if (item.printer.id == printer.id){
                            document
                                .alignment("left")
                            this.addMenuItemLine(document, item, order.orderType)


                            var ing: String

                            item.activeModItems().forEach{ m ->
                                document
                                    .color(PrinterDriver.COLOR.RED)
                                    .text("  " + m.itemName, TextSettings(bold = true))
                                    .color(PrinterDriver.COLOR.BLACK)
                                    .newLine()
                            }


                            item.ingredients?.forEach{ i ->
                                ing =
                                    when (i.orderValue){
                                        0 -> "NO " + i.name.uppercase(Locale.getDefault())
                                        2 -> "EXTRA " + i.name.uppercase(Locale.getDefault())
                                        else -> ""
                                    }
                                if (ing != ""){
                                    document
                                        .color(PrinterDriver.COLOR.RED)
                                        .text("  $ing", TextSettings(bold = true))
                                        .color(PrinterDriver.COLOR.BLACK)
                                        .newLine()
                                }
                            }

                            item.note?.let{
                                if (it != "") {
                                    document
                                        .text("  Notes: " + item.note)
                                        .newLine()
                                }
                            }
                            document.newLine()
                        }
                }
            }}

        order.note?.let{
            if (it != "") {
                document.text("NOTES:")
                    .newLine()
                    .text(order.note)
            }
        }
        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
        return document
    }

    private fun formatPhone(telephone: String?): String{
        telephone?.let {
            return it.substring(0, 3) + "-" + it.substring(3, 6) + "-" + it.substring(6, 10)
        }
        return ""
    }


    fun creditCardReceipt(document: Document, payment: Payment, ticket: Ticket, location: Location): List<Document>{
        document.fontText( "firacoderegular.ttf")
        val list = mutableListOf<Document>()
        val phone: String = formatPhone(location.phones[0].telephoneNumber)
        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))

        val creditTransaction = ticket.activePayment()?.creditCardTransactions?.last()
        val addParams = creditTransaction?.creditTransaction?.AdditionalParameters
        var appInfo: ApplicationInformation? = null

        if (addParams != null){
            if (addParams.EMV != null && addParams.EMV != null){
                if (addParams.EMV?.ApplicationInformation != null && addParams.EMV?.ApplicationInformation != null){
                    appInfo = creditTransaction.creditTransaction?.AdditionalParameters?.EMV?.ApplicationInformation
                }
            }
        }


        for (i in 1..2) {
            document
                .alignment("center")
                .text("Thank you!")
                .newLine()
                .text(location.locationName)
                .newLine()
                .text(location.address.street1)
                .newLine()
                .text(location.address.city + " " + location.address.state + " " + location.address.postalCode)
                .newLine()
                .text(phone)
                .newLine()
                .newLine()
                .text("*** Credit Card Receipt ***", TextSettings(bold = true))
                .newLine()
                .newLine()
                .alignment("left")
                .text("$date $time")
                .newLine()
                .text("Server: " + payment.userName)
                .newLine()
                .text("Order: " + payment.orderNumber.toString())
                .newLine()
            if (payment.tableNumber != null){
                document.text("Table: " +  payment.tableNumber.toString())
                    .newLine()
            }
            document
                .newLine()
                .text(creditTransaction?.creditTransaction?.PaymentType)
                .newLine()
                .text("Card: " + (creditTransaction?.creditTransaction?.AccountNumber))
                .newLine()
                .text("Cardholder: " + (creditTransaction?.creditTransaction?.Cardholder))
                .newLine()
                .text("Approval: " + creditTransaction?.creditTransaction?.AuthorizationCode)
                .newLine()
            if (appInfo != null){
                document
                    .text("AID: " + creditTransaction?.creditTransaction?.AdditionalParameters?.EMV?.ApplicationInformation?.Aid)
                    .newLine()
                    .text("App Lbl: " + creditTransaction?.creditTransaction?.AdditionalParameters?.EMV?.ApplicationInformation?.ApplicationLabel)
                    .newLine()
                    .text("PIN Stmt: " + creditTransaction?.creditTransaction?.AdditionalParameters?.EMV?.PINStatement)
                    .newLine()
            }

            val t18 = String.format("%.2f", creditTransaction?.creditTotal?.times(.18))
            val t20 = String.format("%.2f", creditTransaction?.creditTotal?.times(.20))
            val t22 = String.format("%.2f", creditTransaction?.creditTotal?.times(.22))

            document
                .newLine()
                .alignment("center")
                .text("Details", TextSettings(underline = true))
                .newLine()
                .newLine()
                .alignment("right")
                .text("Amount:  $" + creditTransaction?.creditTransaction?.AmountApproved)
                .newLine()
                .newLine()
                .text("Tip: ____________")
                .newLine()
                .newLine()
                .text("Total: _____________")
                .newLine()
                .newLine()
                .alignment("center")
                .text("I agree to pay the above total")
                .newLine()
                .text("amount according to the card")
                .newLine()
                .text("issuer agreement")
                .newLine()
                .newLine()
                .newLine()
                .text("X:_______________________________________")
                .newLine()
                .alignment("center")
                .text("Signature")
                .newLine()
                .newLine()
                .text("Tip Calculator:")
                .newLine()
                .text("(18%) = $$t18 (20%) = $$t20 (22%) = $$t22")
                .newLine()
                .newLine()

            if (i == 1){
                document.text("**Merchant Copy**")
            }else{
                document.text("**Guest Copy**")
            }

            document
                .newLine()
                .newLine()
                .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
            list.add(document)
        }
        return list
    }
    
    fun ticketReceipt(document: Document, order: Order, payment: Payment, ticket: Ticket, location: Location){
        document.fontText( "firacoderegular.ttf")
        val padStr = " "

        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))

        var itemPrice: Double
        var subtotal = 0.00
        var tax = 0.00
        var itemName: String
        document
            .alignment("center")
            .text("*** Receipt ***", TextSettings(bold = true))
            .newLine()
            .text("Thank you!")
            .newLine()
            .text(location.locationName)
            .newLine()
            .text("--- " + payment.orderType.uppercase(Locale.getDefault()) + " ORDER ---")
            .newLine()

        if(payment.orderType == "Takeout"){
            document
                .text("Name: " + order.takeOutCustomer?.name?.uppercase(Locale.getDefault()))
                .newLine()
            if (order.takeOutCustomer?.telephone != null){
                document
                    .text(order.takeOutCustomer?.telephone)
                    .newLine()
            }
            document
                .newLine()
        }

        document
            .newLine()
            .alignment("left")
            .text("$date $time")
            .newLine()
            .text("Server: " + payment.userName)
            .newLine()
            .text("Order: " + payment.orderNumber.toString())
            .newLine()
        if (payment.tableNumber != null){
            document.text("Table: " +  payment.tableNumber.toString())
                .newLine()
        }
        document
            .newLine()
            .alignment("center")
            .text("SALE")
            .newLine()

        var price: Double
        var padding = 0
        var concat: String

        ticket.ticketItems.forEachIndexed{index, item ->
            itemPrice = item.ticketItemPrice

            if (item.ticketItemPrice != 0.00){
                price = itemPrice

                if (item.itemName.length > 25){
                    itemName = item.itemName.substring(0, 25)
                }else{
                    itemName = item.itemName
                }

                if (item.quantity == 1){
                    padding = (41 - itemName.length - price.toString().length)
                    concat = padStr.repeat(padding)
                    document
                        .alignment("right")
                        .text(item.itemName + concat + String.format("%.2f", price))
                        .newLine()
                }else{
                    itemName = "${item.quantity}x $itemName"
                    padding = (41 - itemName.length - price.toString().length)
                    concat = padStr.repeat(padding)
                    document
                        .alignment("right")
                        .text("$itemName $concat" + String.format("%.2f", price))
                        .newLine()
                }
            }
        }

        document
            .newLine()
            .alignment("right")
            .text(formatDouble("Subtotal:", ticket.subTotal))
            .newLine()
            .text(formatDouble("Tax:", ticket.tax))
            .newLine()

        addAdditionalFees(document, ticket)

        document
            .text(formatDoublePrice("Total:", ticket.total), TextSettings(bold = true))
            .newLine()

        addAdditionalFeesText(document, ticket)

        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)

    }

    private fun addAdditionalFeesText(document: Document, ticket: Ticket){
        if (ticket.extraFees !=  null){
            for (fee in ticket.extraFees){
                document
                    .newLine()
                    .newLine()
                    .alignment("center")
                    .text("      ${fee.checkMessage}      ", TextSettings(size = 26f))
            }
        }
    }

    fun paidCashReceipt(document: Document, payment: Payment, ticket: Ticket, location: Location){
        document.fontText( "firacoderegular.ttf")
        val padStr: String = " "

        val date: String = DateTimeFormatter.ofPattern("MM/dd/yyyy").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))
        val time: String =  DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(payment.timeStamp))

        var itemPrice: Double
        var subtotal: Double = 0.00
        var itemName: String
        var price: Double
        var padding = 0
        var concat: String

        document
            .triggerPeripheral(PrinterDriver.PERIPHERAL.DEVICE1)
            .alignment("center")
            .text("Thank you!")
            .newLine()
            .text(location.locationName)
            .newLine()
            .text("*** Receipt ***", TextSettings(bold = true))
            .newLine()
            .newLine()
            .alignment("left")
            .text("$date $time")
            .newLine()
            .text("Server: " + payment.userName)
            .newLine()
            .text("Order: " + payment.orderNumber.toString())
            .newLine()
        if (payment.tableNumber != null){
            document.text("Table: " +  payment.tableNumber.toString())
                .newLine()
        }
        document
            .newLine()
            .alignment("center")
            .text("SALE")
            .newLine()

        ticket.ticketItems.forEachIndexed{index, item ->
            if (item.ticketItemPrice != 0.00){
                price = item.ticketItemPrice

//                subtotal += itemPrice
//                price = itemPrice

                itemName = if (item.itemName.length > 25){
                    item.itemName.substring(0, 25).trim()
                }else{
                    item.itemName.trim()
                }

                if (item.quantity == 1){
                    padding = (41 - itemName.length - price.toString().length)
                    concat = padStr.repeat(padding)
                    document
                        .alignment("right")
                        .text(item.itemName + concat + String.format("%.2f", price))
                        .newLine()
                }else{
                    padding = (38 - itemName.length - price.toString().length)
                    concat = padStr.repeat(padding)
                    document
                        .alignment("right")
                        .text("${item.quantity}x $itemName $concat" + String.format("%.2f", price))
                        .newLine()
                }

            }
        }

        var change = 0.00
        if (ticket.paymentTotal >= ticket.total){
            change = (ticket.paymentTotal - ticket.total)
        }

        document
            .newLine()
            .alignment("right")
            .text(formatDouble("Subtotal:", ticket.subTotal))
            .newLine()
            .text(formatDouble("Tax:", ticket.tax))
            .newLine()

        val p = ticket.paymentList?.find { it.uiActive }
        val tendered = p?.ticketPaymentAmount

        addAdditionalFees(document, ticket)
        document
            .text(formatDoublePrice("Total:", ticket.total), TextSettings(bold = true))
            .newLine()
            if (tendered != null){
                document.text(formatDoublePrice("Cash Tendered:", tendered))
            }else{
                document.text(formatDoublePrice("Cash Tendered:", ticket.paymentTotal))
            }
            .newLine()
            .newLine()
            .text(formatDoublePrice("Change:", change))


        document
            .newLine()
            .newLine()
            .cutPaper(PrinterDriver.CUTPAPER.PARTIALCUTWITHFEED)
    }

    private fun formatDouble(text: String, price: Double): String{
        return when {
            price < 1.00 -> {
                text + "     " + String.format("%.2f", price)
            }
            price >= 1.00 && price < 10.00 -> {
                text + "     " + String.format("%.2f", price)
            }
            price >= 10.00 && price < 100.00 -> {
                text + "    " + String.format("%.2f", price)
            }
            price >= 100.00 && price < 1000.00 -> {
                text + "   " + String.format("%.2f", price)
            }
            price >= 1000.00 && price < 10000.00 -> {
                text + "  " + String.format("%.2f", price)
            }
            else -> {
                text + "    " + String.format("%.2f", price)
            }
        }
    }

    private fun formatDoublePrice(text: String, price: Double): String{
        return when {
            price < 1.00 -> {
                text + "    $" + String.format("%.2f", price)
            }
            price >= 1.00 && price < 10.00 -> {
                text + "    $" + String.format("%.2f", price)
            }
            price >= 10.00 && price < 100.00 -> {
                text + "   $" + String.format("%.2f", price)
            }
            price >= 100.00 && price < 1000.00 -> {
                text + "  $" + String.format("%.2f", price)
            }
            price >= 1000.00 && price < 10000.00 -> {
                text + " $" + String.format("%.2f", price)
            }
            else -> {
                text + "   $" + String.format("%.2f", price)
            }
        }
    }

}

class KitchenPrintingService @Inject constructor(private val kitchenPrintUseCase: KitchenPrintUseCase) {
    suspend fun printKitchenTickets(list: List<Document>, settings: Settings): KitchenPrintUseCase.Result.Success{
        return kitchenPrintUseCase.print(list, settings)
    }
}

class ReceiptPrintingService @Inject constructor(private val receiptPrintUseCase: ReceiptPrintUseCase){
    suspend fun printTicketReceipt(document: Document, printer: Printer, settings: Settings):ReceiptPrintUseCase.Result.Success{
        return receiptPrintUseCase.print(document, printer, settings)
    }
}
