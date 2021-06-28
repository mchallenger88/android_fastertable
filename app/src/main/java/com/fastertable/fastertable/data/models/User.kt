package com.fastertable.fastertable.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class User
(
    val id: String,
    val pin: String,
    val backOfficePIN: String, //for Employees, Settings and Payroll
    val userName: String,
    val locked: Boolean,
    val permissions: ArrayList<Permission>,
    val locationId: String,
): Parcelable

@Parcelize
data class Permission(
    val name: String,
    val displayName: String,
    val value: Boolean,
    val type: String,
): Parcelable

@Parcelize
data class OpsAuth
(
    val userName: String,
    val employeeId: String,
    val employeeName: String,
    val bearerToken: String,
    val isAuthenticated: Boolean,
    val userClock: UserClock,
    val claims: ArrayList<UserClaim>,
    val companies: ArrayList<Company>,
    val locations: ArrayList<Location>,
): Parcelable

@Parcelize
data class UserClaim(
    val claimId: String,
    val UserId: String,
    val permission: Permission,
): Parcelable

@Parcelize
data class UserClock(
    val employeeId: String,
    val clockInTime: Long,
    val clockOutTime: Long?,
    var checkout: Boolean?,
    val checkoutApproved: Boolean,
): Parcelable

@Parcelize
data class ClockOutCredentials
(
    val employeeId: String,
    val companyId: String,
    val locationId: String,
    val time: Long,
    val midnight: Long,
): Parcelable

@Parcelize
data class PinClockOutCredentials
(
    val pin: String,
    val companyId: String,
    val locationId: String,
    val time: Long,
    val midnight: Long,
): Parcelable

@Parcelize
data class CheckoutCredentials(
    val employeeId: String,
    val companyId: String,
    val locationId: String,
    val checkout: Boolean,
    val midnight: Long,
): Parcelable

@Parcelize
data class ReopenCheckoutRequest(
    val employeeId: String,
    val companyId: String,
    val locationId: String,
    val midnight: Long,
): Parcelable

@Parcelize
data class OpsUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val salt: String,
    val companies: ArrayList<Company>,
    val locations: ArrayList<Location>,
    val cos: ArrayList<String>,
    val locs: ArrayList<String>,
    val claims: ArrayList<UserClaim>,
    val permissions: ArrayList<Permission>,
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
data class OpsLogin(
    val email: String,
    val password: String,
): Parcelable

@Parcelize
data class LoginCredentials
(
        val Pin: String,
        val CompanyId: String,
        val LocationId: String,
        val Time: Long,
        val Midnight: Long
): Parcelable
