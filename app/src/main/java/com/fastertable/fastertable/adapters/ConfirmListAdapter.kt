package com.fastertable.fastertable.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastertable.fastertable.data.models.ConfirmEmployee
import com.fastertable.fastertable.data.models.PayTicket
import com.fastertable.fastertable.data.models.Ticket
import com.fastertable.fastertable.data.models.TicketPayment
import com.fastertable.fastertable.databinding.ConfirmListLineItemBinding
import com.fastertable.fastertable.utils.round
import kotlin.math.abs

class ConfirmListAdapter(private val clickListener: ConfirmListListener, private val buttonListener: ConfirmButtonListener) : ListAdapter<ConfirmEmployee, ConfirmListAdapter.ConfirmEmployeeViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmEmployeeViewHolder {
        return ConfirmEmployeeViewHolder(ConfirmListLineItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ConfirmEmployeeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener, buttonListener)
    }


    class ConfirmListListener(val clickListener: (item: ConfirmEmployee) -> Unit) {
        fun onClick(item: ConfirmEmployee) = clickListener(item)
    }

    class ConfirmButtonListener(val buttonListener: (item: ConfirmEmployee) -> Unit){
        fun onClick(item: ConfirmEmployee) = buttonListener(item)
    }

    class ConfirmEmployeeViewHolder(private var binding: ConfirmListLineItemBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(item: ConfirmEmployee, clickListener: ConfirmListListener, buttonListener: ConfirmButtonListener){
                separateTickets(item)
                binding.emp = item
                binding.clickListener = clickListener
                binding.btnConfirm.setOnClickListener {
                    buttonListener.onClick(item)
                }
                binding.executePendingBindings()

            }

        private fun separateTickets(ce: ConfirmEmployee){
            if (ce.orders != null){
                val listPayTicket = arrayListOf<PayTicket>()
                val listTickets = arrayListOf<Ticket>()
                ce.orders.forEach { o ->
                    val pt = PayTicket(
                        order = o,
                        payment = null,
                        ticket = null
                    )

                    val p = ce.payments?.find{ it.id == o.id.replace("O_", "P_")}
                    p?.tickets?.forEach {
                        pt.payment = p
                        pt.ticket = it
                        listTickets.add(it)
                    }
                    listPayTicket.add(pt)
                }

                ce.payTickets = listPayTicket
                ce.openOrders = ce.orders.any{ it.closeTime == null }
                ce.allTickets = listTickets
                ce.paidTickets = ce.allTickets.filter{ it.paymentTotal != 0.00}
//                ce.cashSales = ce.allTickets.filter{ it.paymentType == "Cash"}
//                ce.creditSales = ce.allTickets.filter{ it.paymentType == "Credit" || it.paymentType == "Manual Credit"}
//                ce.cashSalesTotal = ce.cashSales.sumOf { it.paymentTotal }
//                ce.creditSalesTotal = ce.creditSales.sumOf { it.paymentTotal }
                ce.orderTotal =  ce.allTickets.sumOf { it.paymentTotal }
                ce.paymentTotal = ce.paidTickets.sumOf { it.paymentTotal }
                ce.cashSalesTotal = getCashSales(ce.allTickets)
                ce.creditSalesTotal = getCreditSales(ce.allTickets)
                ce.creditTips = getCreditGratuity(ce.allTickets)
                if (ce.creditTips > 0){
                    ce.tipDiscount?.let {
                        ce.creditTips = ce.creditTips.minus(ce.tipDiscount.div(100)).round(2)
                    }
                }

                ce.totalOwed = ce.cashSalesTotal.minus(ce.creditTips)
//                if (ce.tipSettlementPeriod == "Daily"){
//                    ce.totalOwed = ce.cashSalesTotal.minus(ce.creditTips)
//                }
//
//                if (ce.tipSettlementPeriod === "Weekly"){
//                    ce.totalOwed = ce.cashSalesTotal
//                }

                if (ce.totalOwed < 0){
                    ce.totalOwed = abs(ce.totalOwed)
                    ce.totalNegative = true
                }else{
                    ce.totalNegative = false
                }
            }
        }

        private fun getCashSales(list: List<Ticket>): Double{
            val payments = mutableListOf<Double>()
            val paymentsList = mutableListOf<TicketPayment>()
            for (ticket in list){
                ticket.paymentList?.forEach{ p ->
                    if (p.paymentType == "Cash" && !p.canceled){
                        paymentsList.add(p)
                        payments.add(p.ticketPaymentAmount)
                    }
                }
            }
            return payments.sumOf{it}
        }

        private fun getCreditSales(list: List<Ticket>): Double{
            val payments = mutableListOf<Double>()
            val paymentsList = mutableListOf<TicketPayment>()
            for (ticket in list){
                if (ticket.paymentList != null){
                    ticket.paymentList?.forEach{ p ->
                        if (p.paymentType == "Credit" || p.paymentType == "Manual Credit"  && !p.canceled){
                            paymentsList.add(p)
                            payments.add(p.ticketPaymentAmount)
                        }
                    }
                }else{
                    if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit"){
                        payments.add(ticket.paymentTotal)
                    }

                }

            }
            return payments.sumOf{it}
//            return paymentsList.sumOf{it.ticketPaymentAmount}
        }

        private fun getCreditGratuity(list: List<Ticket>): Double{
            val payments = mutableListOf<Double>()
            val paymentsList = mutableListOf<TicketPayment>()
            for (ticket in list){
                if (ticket.paymentList != null){
                    ticket.paymentList?.forEach{ p ->
                        if (p.paymentType == "Credit" || p.paymentType == "Manual Credit" || p.paymentType == "Gift"){
                            paymentsList.add(p)
                            payments.add(p.gratuity)
                        }
                    }
                }else{
                     if (ticket.paymentType == "Credit" || ticket.paymentType == "Manual Credit" || ticket.paymentType == "Gift"){
                         payments.add(ticket.gratuity)
                        }
                }

            }
            return payments.sumOf{it}
//            return paymentsList.sumOf{it.gratuity}
        }

    }


    companion object DiffCallback : DiffUtil.ItemCallback<ConfirmEmployee>() {
        override fun areItemsTheSame(oldItem: ConfirmEmployee, newItem: ConfirmEmployee): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ConfirmEmployee, newItem: ConfirmEmployee): Boolean {
            return oldItem.employeeId == newItem.employeeId
        }
    }




}