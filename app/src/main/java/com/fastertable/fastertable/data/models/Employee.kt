package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee
(
    val id: String,
    val person: Person,
    val employeeDetails: EmployeeDetails,
    val user: User,
    val locations: ArrayList<String>,
    val active: Boolean,
    val companyId: String,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?
): Parcelable{
    fun fullName(): String{
        return person.firstName + " " + person.lastName
    }
}

@Parcelize
data class EmployeeDetails
(
    val employeeNumber: Int,
    val employeeType: String,
    val payType: String,
    val payRate: Double,
    val payFrequency: String,
    val department: String,
    val startDate: String,
    val ssn: String,
    val birthDate: String,
    val gender: String,
): Parcelable

@Parcelize
data class TimeSheetSummary(
    val employee: Employee,
    val totalHours: Double,
): Parcelable

@Parcelize
data class TimeSheets(
    val day: Long,
    val clock: UserClock
): Parcelable

@Parcelize
data class PayrollSummary(
    val employee: Employee,
    val totalHours: Double,
    val overTimeHours: Double,
    val regHours: Double,
    val payRate: Double,
    val totalPay: Double
): Parcelable

@Parcelize
data class OverTime(
    val employeeId: String,
    val overTimeHours: Double
): Parcelable

@Parcelize
data class ConfirmEmployee(
    val checkoutDate: Long,
    val employeeName: String,
    val employeeId: String,
    val shifts: UserClock?,
    val orders: ArrayList<Order>?,
    var openOrders: Boolean,
    val payments: ArrayList<Payment>?,
    var payTickets: ArrayList<PayTicket>,
    val approvals: ArrayList<Approval>,
    var orderTotal: Double,
    var allTickets: List<Ticket>,
    var paidTickets: List<Ticket>,
//    var cashSales: List<Ticket>,
//    var creditSales: List<Ticket>,
    var voidTotal: Double,
    var discountTotal: Double,
    var barShare: Double,
    var busShare: Double,
    var paymentTotal: Double,
    var cashSalesTotal: Double,
    var creditSalesTotal: Double,
    var creditTips: Double,
    var totalOwed: Double,
    var totalNegative: Boolean,
    val tipDiscount: Double?,
    val tipSettlementPeriod: String
): Parcelable

@Parcelize
data class ConfirmTotal(
    val orderTotal: Double,
    val paidTotal: Double,
    val cashSalesTotal: Double,
    val creditSalesTotal: Double,
    val totalTips: Double,
    val totalOwed: Double,
    val totalNegative: Boolean,
): Parcelable

@Parcelize
data class UpdateClockRequest(
    val companyId: String,
    val locationId: String,
    val day: Long,
    val originalClockInTime: Long,
    val employeeId: String,
    val clockInTime: Long,
    val clockOutTime: Long,
): Parcelable

@Parcelize
data class EmployeeTimeSheet(
    val employee: Employee,
    val timesheets: ArrayList<TimeSheets>
): Parcelable

@Parcelize
data class LocationDailyClocks(
    val companyId: String,
    val day: Long,
    val userClocks: ArrayList<UserClock>,
    val locationId: String,
    val archived: Boolean,
    val type: String,
    val _rid: String?,
    val _self: String?,
    val _etag: String?,
    val _attachments: String?,
    val _ts: Long?

): Parcelable

@Parcelize
data class ConfirmEmployeeCheckout(
    val confirmEmployee: ConfirmEmployee,
    val midnight: Long,
): Parcelable

@Parcelize
data class CheckoutRequest(
    val companyId: String,
    val locationId: String,
    val userId: String,
    val midnight: Long,
    val clockInTime: Long?,
): Parcelable

@Parcelize
data class EmployeeTips(
    val employeeId: String,
    val employee: Employee,
    val tickets: ArrayList<SortedTicket>,
    val tipSum: Double,
    val dateList: ArrayList<Long>
): Parcelable

@Parcelize
data class  CompanyTimeBasedRequest(
    val midnight: Long,
    val locationId: String,
    val companyId: String
): Parcelable

@Parcelize
data class GetEmployee(
    val cid: String,
    val eid: String
): Parcelable