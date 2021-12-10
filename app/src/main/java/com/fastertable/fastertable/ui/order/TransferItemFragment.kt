package com.fastertable.fastertable.ui.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.adapters.OrderItemTransferAdapter
import com.fastertable.fastertable.adapters.TransferItemOrdersAdapter
import com.fastertable.fastertable.adapters.TransferItemOrdersHeaderAdapter
import com.fastertable.fastertable.common.base.BaseFragment
import com.fastertable.fastertable.data.models.Order
import com.fastertable.fastertable.databinding.TransferOrderItemFragmentBinding

class TransferItemFragment: BaseFragment(R.layout.transfer_order_item_fragment) {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: TransferOrderItemFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        viewModel.orders.observe(viewLifecycleOwner, { list ->
            if (list != null){
                val newList = list.filter { it.closeTime == null } as MutableList<Order>
                viewModel.activeOrder.value?.let { o ->
                    newList.remove(o)
                }
                ordersAdapter.submitList(newList)
                ordersAdapter.notifyDataSetChanged()
            }
        })
    }

}