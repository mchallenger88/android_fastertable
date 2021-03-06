package com.fastertable.fastertable.api

import android.app.Application
import android.content.Context
import android.util.Log
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.data.models.Printer
import com.fastertable.fastertable.data.models.Settings
import com.fastertable.fastertable.services.PrintTicketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import technology.master.kotlinprint.printer.Document
import technology.master.kotlinprint.printer.Epson
import technology.master.kotlinprint.printer.PrinterDriver
import technology.master.kotlinprint.printer.PrinterSettings
import javax.inject.Inject

class KitchenPrintUseCase @Inject constructor(private val app: Application){

    sealed class Result {
//        data class Success(val order: Order) : Result()
        data class Success(val boolean: Boolean) : Result()
        object Failure: Result()
    }

    suspend fun print(list: List<Document>, settings: Settings): Result.Success {
        return withContext(Dispatchers.IO){
            val context = app.applicationContext
            for (doc in list){
                printTicket(doc, context, settings)
            }
            return@withContext Result.Success(true)
        }
    }


    private fun printTicket(document: Document, context: Context, settings: Settings){
        PrinterDriver.setContext(context)
        val ip = settings.printers.find{it.master}?.ipAddress
        val s = PrinterSettings(
            address = "TCP:$ip",
            debugging = true,
            onError = { str, p -> onError(str, p) },
            context = context
        )

        s.forceDriver = 1
        val epsonPrinter = Epson(s)
        epsonPrinter.connect().then {
            epsonPrinter.print(document).then {
                epsonPrinter.close().then {
                }
            }.catch { err ->
                Log.d("Printer Error", "Printing Error: $err");
                epsonPrinter.close();
            }
        }
    }

    private fun printMasterTicket(order: Order, context: Context, settings: Settings) {
        PrinterDriver.setContext(context)
        val ip = settings.printers.find{it.master}?.ipAddress
        val s = PrinterSettings(
            address = "TCP:$ip",
            debugging = true,
            onError = { str, p -> onError(str, p) },
            context = context
        )
        s.forceDriver = 1
        val epsonPrinter = Epson(s)
        epsonPrinter.connect().then {
            val document = epsonPrinter.createDocument()
            PrintTicketService().masterTicket(document, order)
            epsonPrinter.print(document).then {
                epsonPrinter.close().then {
                }
            }.catch { err ->
                Log.d("Printer Error", "Printing Error: $err");
                epsonPrinter.close();
            }
        }
    }

    private fun printKitchenTicket(order: Order, context: Context, printer: Printer, settings: Settings){
        PrinterDriver.setContext(context)
        val ip = settings.printers.find{it.printerName == printer.printerName}?.ipAddress
        val s = PrinterSettings(
            address = "TCP:$ip",
            debugging = true,
            onError = { str, p -> onError(str, p) },
            context = context
        )
        s.forceDriver = 1
        val epsonPrinter = Epson(s)
        epsonPrinter.connect().then {
            val document = epsonPrinter.createDocument()
            PrintTicketService().kitchenTicket(document, order, printer)
            epsonPrinter.print(document).then {
                epsonPrinter.close().then {
                }
            }.catch { err ->
                Log.d("Printer Error", "Printing Error: $err");
                epsonPrinter.close();
            }
        }
    }

    private fun orderPrinterList(order: Order): List<Printer>{
        val printers = mutableListOf<Printer>()
        var printer: Printer?
        order.guests?.forEach{g ->
            if (g.orderItems != null){
                g.orderItems!!.forEach{oi ->
                    if (oi.status == "Started"){
                        printer = printers.find{it.id == oi.printer.id}
                        if (printer == null){
                            printers.add(oi.printer)
                        }}
                }}
        }
        return printers
    }

    private fun onError(s: String, p: PrinterDriver): Boolean {
        Log.d("Printer Error", s)
        return false;
    }
}