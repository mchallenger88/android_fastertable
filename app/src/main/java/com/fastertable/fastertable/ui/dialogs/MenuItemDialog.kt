package com.fastertable.fastertable.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fastertable.fastertable.R
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.databinding.AddMiscMenuItemBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class MenuItemDialog: BaseDialog(R.layout.add_misc_menu_item) {
    private val viewModel: OrderViewModel by activityViewModels()
    private val binding: AddMiscMenuItemBinding by viewBinding()
    private var salesCategory: String = "Food"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.radioFood.setOnClickListener{
            salesCategory = "Food"
        }

        binding.radioBar.setOnClickListener {
            salesCategory = "Bar"
        }

        binding.radioInventory.setOnClickListener {
            salesCategory = "Inventory"
        }
        binding.btnAdhocItemAdd.setOnClickListener {
            val name = binding.txtAdhocName.text.toString()
            val qty = binding.txtAdhocQuantity.text.toString()
            val price = binding.txtAdhocPrice.text.toString()
            if (name == ""){
                binding.txtAdhocName.error = "Item Name is required"
            }else{
                if (qty == ""){
                    binding.txtAdhocQuantity.error = "Quantity is required"
                }else{
                    if (price == ""){
                        binding.txtAdhocPrice.error = "Price is required"
                    }else{
                        createOrderItem(qty.toInt(), name, price.toDouble())
                        dismiss()
                    }
                }
            }
        }
    }

    private fun createOrderItem(qty: Int, name: String, price: Double){
        val order = viewModel.activeOrder.value
        val id = order?.orderItems?.last()?.id?.plus(1)!!

        val p = ItemPrice(
            quantity = qty,
            price = price,
            size = "Regular",
            discountPrice = null,
            tax = "Taxable"
        )

        val printer = findPrinter(salesCategory)

        val item = OrderItem(
            id = id,
            menuItemId = "Adhoc_Menu_Item",
            menuItemName = name,
            menuItemPrice = p,
            modifiers = viewModel.activeItem.value?.modifiers,
            salesCategory = salesCategory,
            ingredients = arrayListOf<ItemIngredient>(),
            prepStation = findPrepStation(printer!!),
            printer = printer,
            priceAdjusted = false,
            menuItemDiscount = null,
            takeOutFlag = false,
            dontMake = false,
            rush = false,
            tax = p.tax,
            note = "",
            employeeId = viewModel.user?.employeeId ?: "",
            status = "Started",
        )

        viewModel.addAdHocItem(item)
    }

    override fun onStart() {
        super.onStart()
        val width = 1000
        val height= 500
        dialog?.let {
            it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun findPrepStation(printer: Printer): PrepStation? {
        viewModel.settings?.let {
            return it.getPrepStation(printer.printerName)
        }
        return null
    }

    private fun findPrinter(salesCat: String): Printer? {
        viewModel.settings?.let { settings ->
            return when (salesCat) {
                "Food" -> {settings.printers.find{it.master}}
                "Bar" -> {settings.printers.find{it.printerName.contains("Bar")}}
                "Inventory" -> {settings.printers.find{it.master}}
                else -> {settings.printers.find{it.master}}
            }
        }
        return null
    }


    companion object {
        fun newInstance(): MenuItemDialog {
            return MenuItemDialog()
        }

        const val TAG = "MenuItemDialog"
    }


}