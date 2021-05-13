package com.fastertable.fastertable.common

object Constants {
    const val BASE_URL = "https://datadev.fastertable.com/api/"
    const val TERMINAL_ERROR =  "There was an error communicating with the card terminal."
    const val STAGING_ERROR = "Invalid credentials. Please check your settings and ensure the Name, Site ID, and Key are entered correctly."
    const val UNKNOWN_ERROR = "An unknown error occurred during processing"
    const val TRANSACTION_ERROR = "There was an error sending values to the terminal."
    const val TIMEOUT_ERROR = "The connection to the terminal is taking too long. Press continue to see if the transaction was approved."
    const val BAD_CONNECTION_ERROR = "There was an error sending values to the terminal."
    const val DECLINED = "The transaction was declined."
    const val DECLINED_MORE_INFO = "The transaction was declined. Please contact the credit card issuer for more information."
    const val BAD_VALUE = "The transaction failed because it was invalid."
    const val INSUFFICIENT_FUNDS = "Declined. Insufficient funds are available on this card."
    const val DUPLICATE = "Declined. The transaction appears to be a duplicate."
    const val COMMUNICATION_ERROR = "Failed. Communication with the processor failed."
    const val USER_CANCEL = "Cancelled. The user has cancelled this transaction."
    const val SERVER_CANCEL = "Cancelled. A cancel command was send from FasterTable."
    const val TIP_ADDED = "A tip was added to the payment. The Tip Amount is: $"
    const val APPROVED = "The transaction was approved."
    const val PARTIAL_PAYMENT = "A partial payment on the ticket was made. There is a remaining balance of: $"
    const val CREDIT_CARD_RECEIPT = "Ask Guest: Would you like a Credit Card Receipt?"
    const val GIFT_CARD_RECEIPT = "Ask Guest: Would you like a Gift Card Receipt?"
    const val GIFT_CARD_ADD1 = "The following amount has been added to the card: $"
    const val GIFT_CARD_ADD2 = "The Total Balance on the card is now: $"
    const val GIFT_CARD_BALANCE = "The balance on this card is: $"
    const val GIFT_CARD_ERROR = "There was an error processing your gift card transaction. Please contact tech support."

}