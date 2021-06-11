package com.fastertable.fastertable.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastertable.fastertable.api.GetCheckoutUseCase
import com.fastertable.fastertable.common.base.BaseViewModel
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.data.repository.GetCheckout
import com.fastertable.fastertable.data.repository.LoginRepository
import com.fastertable.fastertable.utils.GlobalUtils
import com.fastertable.fastertable.utils.round
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs

@HiltViewModel
class CheckoutViewModel @Inject constructor (
    private val loginRepository: LoginRepository,
    private val getCheckout: GetCheckout
        ): BaseViewModel() {

    val settings: Settings = loginRepository.getSettings()!!

    private val _activeDate = MutableLiveData<LocalDate>()
    val activeDate: LiveData<LocalDate>
        get() = _activeDate

    private val _checkout = MutableLiveData<ConfirmEmployee>()
    val checkout: LiveData<ConfirmEmployee>
        get() = _checkout

    private val _checkoutNull = MutableLiveData<Boolean>()
    val checkoutNull: LiveData<Boolean>
        get() = _checkoutNull

    private val _showOrders = MutableLiveData<Boolean>()
    val showOrders: LiveData<Boolean>
        get() = _showOrders

//    private val _showPayments = MutableLiveData<Boolean>()
//    val showPayments: LiveData<Boolean>
//        get() = _showPayments

    private val midnight: Long = GlobalUtils().getMidnight()

    init{
        _activeDate.value = LocalDate.now()
        getCheckout()
        _showOrders.value = true
    }

    private fun getCheckout(){
        viewModelScope.launch {
            val user = loginRepository.getOpsUser()
            val rollingMidnight = GlobalUtils().unixMidnight(_activeDate.value!!)
            if (midnight == rollingMidnight){
                val request = CheckoutRequest(
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    userId = user?.employeeId!!,
                    midnight = midnight,
                    clockInTime = user.userClock.clockInTime
                )
                _checkout.postValue(getCheckout.getCheckout(request))
            }else{
                val request = CheckoutRequest(
                    companyId = settings.companyId,
                    locationId = settings.locationId,
                    userId = user?.employeeId!!,
                    midnight = rollingMidnight,
                    clockInTime = null
                )
                _checkout.postValue(getCheckout.getCheckout(request))
            }


        }
    }

    fun separateTickets(ce: ConfirmEmployee){
        if (ce.orders != null){
            _checkoutNull.value = false
            val listPayTicket = arrayListOf<PayTicket>()
            val listTickets = arrayListOf<Ticket>()
            ce.payments?.forEach{ it ->
                it.tickets.forEach{t ->
                    val pt = PayTicket(
                        p = it,
                        t = t )
                    listPayTicket.add(pt)
                    listTickets.add(t)
                }}
            ce.payTickets = listPayTicket
            ce.openOrders = ce.orders.any{ it.closeTime == null }
            ce.allTickets = listTickets
            ce.paidTickets = ce.allTickets.filter{ it.paymentTotal != 0.00}
            ce.cashSales = ce.allTickets.filter{ it.paymentType == "Cash"}
            ce.creditSales = ce.allTickets.filter{ it.paymentType == "Credit" || it.paymentType == "Manual Credit"}

            ce.orderTotal =  ce.allTickets.sumByDouble { it.paymentTotal }
            ce.paymentTotal = ce.paidTickets.sumByDouble { it.paymentTotal }
            ce.cashSalesTotal = ce.cashSales.sumByDouble { it.paymentTotal }
            ce.creditSalesTotal = ce.creditSales.sumByDouble { it.paymentTotal }
            ce.creditTips = ce.creditSales.sumByDouble { it.gratuity }
            val tipDiscount = ce.creditTips.times(settings.creditCardTipDiscount).round(2)
            ce.creditTips = ce.creditTips.minus(tipDiscount).round(2)

            if (settings.tipSettlementPeriod == "Daily"){
                ce.totalOwed = ce.cashSalesTotal.minus(ce.creditTips)
            }

            if (settings.tipSettlementPeriod === "Weekly"){
                ce.totalOwed = ce.cashSalesTotal
            }

            if (ce.totalOwed < 0){
                ce.totalOwed = abs(ce.totalOwed);
                ce.totalNegative = true;
            }else{
                ce.totalNegative = false;
            }

            val listApprovalItems = ce.approvals.flatMap{it.approvalItems}

            val voidTickets = listApprovalItems.filter{ it.approvalType == "Void Ticket" && it.approved == true}
            val voidItems = listApprovalItems.filter{ it.approvalType == "Void Item" && it.approved == true}
            val discountTickets = listApprovalItems.filter{ it.approvalType == "Discount Ticket" && it.approved == true}
            val discountItems = listApprovalItems.filter{ it.approvalType == "Discount Item" && it.approved == true}

            val voidTicketTotal = voidTickets.sumByDouble { it.amount }
            val voidItemTotal = voidItems.sumByDouble { it.amount }
            val discountItemTotal = discountItems.sumByDouble { it.amount }
            val discountTicketTotal = discountTickets.sumByDouble { it.amount }

            ce.voidTotal = voidTicketTotal.plus(voidItemTotal)
            ce.discountTotal = discountItemTotal.plus(discountTicketTotal)

            val listGuests = ce.orders.flatMap { it.guests!! }
            val orderItems = listGuests.flatMap { it.orderItems!! }
            //TODO: Not precise because an item might be discounted in the Payment
            val barItems = orderItems.filter{it.salesCategory == "Bar"}
            val barSales = barItems.sumByDouble { it.menuItemPrice.price }

            ce.busShare = ce.orderTotal.times(settings.tipShare.busboy.div(100));
            ce.barShare = barSales.times(settings.tipShare.bartender.div(100))
        }else{
            _checkoutNull.value = true
        }

    }

    fun dateForward(){
        if (_activeDate.value?.plusDays(1)?.atStartOfDay()!! <= LocalDate.now().atStartOfDay()){
            _activeDate.value = _activeDate.value?.plusDays(1)
            getCheckout()
        }
    }

    fun dateBack(){
        _activeDate.value = _activeDate.value?.minusDays(1)
        getCheckout()
    }

    fun showOrders(){
        _showOrders.value = true
    }

    fun showPayments(){
        _showOrders.value = false
    }
}