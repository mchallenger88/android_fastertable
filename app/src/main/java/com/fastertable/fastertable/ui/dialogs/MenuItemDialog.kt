package com.fastertable.fastertable.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.fastertable.fastertable.data.models.*
import com.fastertable.fastertable.databinding.AddMiscMenuItemBinding
import com.fastertable.fastertable.ui.order.OrderViewModel

class MenuItemDialog: BaseDialog() {
    private val viewModel: OrderViewModel by activityViewModels()
    private var salesCategory: String = "Food"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AddMiscMenuItemBinding.inflate(inflater)

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

        return binding.root
    }

    private fun createOrderItem(qty: Int, name: String, price: Double){
        val g = viewModel.activeOrder.value!!.guests!!.find { it.uiActive }
        var id = 0
        if (!g!!.orderItems.isNullOrEmpty()){
            id = g.orderItems!!.size
        }
        val p = ItemPrice(
            price = price,
            size = "Regular",
            discountPrice = null,
            tax = "Taxable"
        )

        val printer = findPrinter(salesCategory)

        val item = OrderItem(
            id = id,
            quantity = qty,
            menuItemId = "Adhoc_Menu_Item",
            menuItemName = name,
            menuItemPrice = p,
            orderMods = arrayListOf<ModifierItem>(),
            salesCategory = salesCategory,
            ingredientList = null,
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
            employeeId = viewModel.user.employeeId,
            status = "Started",
        )

        viewModel.addAdHocItem(item)
    }

    override fun onStart() {
        super.onStart()
        val width = 1000
        val height= 500
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun findPrepStation(printer: Printer): PrepStation {
        return viewModel.settings.getPrepStation(printer.printerName)
    }

    private fun findPrinter(salesCat: String): Printer? {
        return when (salesCat) {
            "Food" -> {viewModel.settings.printers.find{it.master}}
            "Bar" -> {viewModel.settings.printers.find{it.printerName.contains("Bar")}}
            "Inventory" -> {viewModel.settings.printers.find{it.master}}
            else -> {viewModel.settings.printers.find{it.master}}
        }
    }


    companion object {
        fun newInstance(): MenuItemDialog {
            return MenuItemDialog()
        }

        const val TAG = "MenuItemDialog"
    }
}