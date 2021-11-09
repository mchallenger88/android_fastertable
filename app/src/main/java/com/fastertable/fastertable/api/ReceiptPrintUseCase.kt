package com.fastertable.fastertable.api

import android.app.Application
import android.content.Context
import android.util.Log
import com.fastertable.fastertable.data.models.Printer
import com.fastertable.fastertable.data.models.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import technology.master.kotlinprint.printer.Document
import technology.master.kotlinprint.printer.Epson
import technology.master.kotlinprint.printer.PrinterDriver
import technology.master.kotlinprint.printer.PrinterSettings
import javax.inject.Inject

class ReceiptPrintUseCase @Inject constructor(private val app: Application) {
    sealed class Result {
        data class Success(val boolean: Boolean) : Result()
        object Failure: Result()
    }

    suspend fun print(document: Document, printer: Printer, settings: Settings): ReceiptPrintUseCase.Result.Success {
        return withContext(Dispatchers.IO){
            val context = app.applicationContext
            printTicket(document, printer, context, settings)
            return@withContext Result.Success(true)
        }
    }

    private fun printTicket(document: Document, printer: Printer, context: Context, settings: Settings){
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
            epsonPrinter.print(document).then {
                epsonPrinter.close().then {
                }
            }.catch { err ->
                Log.d("Printer Error", "Printing Error: $err");
                epsonPrinter.close();
            }
        }
    }
    private fun onError(s: String, p: PrinterDriver): Boolean {
        Log.d("Printer Error", s)
        return false;
    }

}