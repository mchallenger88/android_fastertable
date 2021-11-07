package com.fastertable.fastertable.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.fastertable.fastertable.adapters.OrderItemTransferAdapter
import com.fastertable.fastertable.adapters.TransferItemOrdersAdapter
import com.fastertable.fastertable.adapters.TransferItemOrdersHeaderAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.databinding.TransferOrderItemFragmentBinding
import com.fastertable.fastertable.ui.home.HomeViewModel

class TransferItemFragment: BaseFragment() {
    private val viewModel: OrderViewModel by activityViewModels()
    private lateinit var binding: TransferOrderItemFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = TransferOrderItemFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel

        val orderAdapter = OrderItemTransferAdapter(OrderItemTransferAdapter.TransferAddListener {
                viewModel.transferAddItem(it)
        }, OrderItemTransferAdapter.TransferRemoveListener {
                viewModel.transferRemoveItem(it)
        })

        val ordersHeaderAdapter = TransferItemOrdersHeaderAdapter()

        val ordersAdapter = TransferItemOrdersAdapter(TransferItemOrdersAdapter.TransferOrdersListListener {
            viewModel.transferItemsToOrder(it)
        })

        val concatAdapter = ConcatAdapter(ordersHeaderAdapter, ordersAdapter)

        binding.recyclerTransferItem.adapter = orderAdapter
        binding.recyclerOrderTransfer.adapter = concatAdapter

        viewModel.activeOrder.observe(viewLifecycleOwner, {
            if (it != null){
                orderAdapter.submitList(it.getAllOrderItems())
                orderAdapter.notifyDataSetChanged()
            }
        })

        viewModel.orders.observe(viewLifecycleOwner, {
            if (it != null){
                val list = it.filter { it.closeTime ==  null } as MutableList<Order>
                val o = viewModel.activeOrder.value!!
                list.remove(o)
                ordersAdapter.submitList(list)
                ordersAdapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}