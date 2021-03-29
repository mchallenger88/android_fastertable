package com.fastertable.fastertable.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Company(
    val id: String,
    val companyName: String,
    val address: Address,
    val adminContact: Person,
    val locations: ArrayList<Location>,
    val phones: ArrayList<Telephone>,
    val loginCode: String,
    val password: String,
    val website: String,
): Parcelable{
    fun getLocation(id: String): Location? {
        return this.locations.find { it.id == id }
    }
}

@Parcelize
data class Person(
    val firstName: String,
    val lastName: String,
    val address: Address,
    val phones: ArrayList<Telephone>,
    val emails: ArrayList<Email>,
): Parcelable

@Parcelize
data class Location(
    val id: String,
    val locationName: String,
    val address: Address,
    val phones: ArrayList<Telephone>,
    val primaryContact: Person,
    val website: String,
    val loginPin: Int,
    val description: String,
    val reservations: Boolean,
    val takeout: Boolean,
    val delivery: Boolean,
    val mainImage: String,
    val gallery: ArrayList<String>,
    val priceLevel: Int, //1-5
    val foodType: ArrayList<String>,
    val rating: Int,
    val coordinates: Coordinates,
    val companyId: String,
    val fcmToken: ArrayList<String>
): Parcelable

@Parcelize
data class Coordinates(
    val lat: Double,
    val lng: Double,
): Parcelable

@Parcelize
data class Address(
    val street1: String,
    val street2: String,
    val city: String,
    val state: String,
    val postalCode: String,
): Parcelable

@Parcelize
data class Telephone(
    val telephoneType: String, //Main, Home, Mobile, Fax
    val telephoneNumber: String,
): Parcelable

@Parcelize
data class Email(
    val emailType: String, //main, business, personal
    val emailAddress: String,
): Parcelable

@Parcelize
data class LocationMobileCredit(
    val id: String,
    val companyId: String,
    val locationId: String,
    val midnight: Long,
    val users: ArrayList<UserMobileCredit>,
    val type: String,
): Parcelable

@Parcelize
data class UserMobileCredit(
    val employeeId: String,
    val equipment: CreditCardEquipment,
): Parcelable

//@Parcelize
//data class MobileRequest(
//    val companyId: String,
//    val locationId: String,
//    val midnight: Long,
//): Parcelable
//
//data class LocationandSettings (
//    val l: Location,
//    val s: Settings,
//)
//
//data class CompanyLogin(
//    val loginCode: String,
//    val password: String,
//)
//
//data class Identifier(
//    val id: String,
//)