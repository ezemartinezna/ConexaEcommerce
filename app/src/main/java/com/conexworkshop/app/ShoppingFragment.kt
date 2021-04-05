package com.conexworkshop.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_shopping.*
import java.lang.reflect.Type


class ShoppingFragment : Fragment(), ProductItemAdapter.AdapterOnClick {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadProductsInShop()

        buttonClearAll.setOnClickListener {

            val arrayBuy = getArrayList("arrayListShopping")
            if (arrayBuy != null) {
                for (i in 0 until arrayBuy.count()) {
                    if (arrayBuy[i].isShop) {
                        arrayBuy[i].isShop = false
                    }
                }
                saveArrayList(arrayBuy,"arrayListShopping")
            }

            val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
           prefs?.edit()?.remove("arrayListShopBuy")?.apply()
            loadProductsInShop()
        }

        buttonConfirmPay.setOnClickListener {
            Toast.makeText(context,"PAGADO CORRECTAMENTE", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadProductsInShop() {


        val arrayBuy = getArrayList("arrayListShopBuy")
        var co = 0
        var total = 0.0
        if (arrayBuy != null) {
            for (producto in arrayBuy) {
                total += producto.price
                co += 1
            }
            textCantidad.text = "Cantidad de Productos: $co"
            textTotal.text = "Total en Pesos: $$total"
        }else{
            textCantidad.text = ""
            textTotal.text = ""
        }
        activity?.runOnUiThread{
            if (arrayBuy != null) {
                recylerView_Shopping.adapter = ProductItemAdapter(arrayBuy,this)
            }else{
                val mutableList = mutableListOf<Productos>()
                recylerView_Shopping.adapter = ProductItemAdapter(mutableList,this)
            }
        }

    }

    private fun saveArrayList(list: MutableList<Productos>, key: String?) {
        val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = prefs?.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor?.putString(key, json)
        editor?.apply()
    }

    private fun getArrayList(key: String?): MutableList<Productos>? {
        val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = prefs?.getString(key, null)
        val type: Type = object : TypeToken<MutableList<Productos>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun onClick(id : Int) {

        val arrayBuy = getArrayList("arrayListShopBuy")
        val arrayShop = getArrayList("arrayListShopping")

        if (arrayBuy != null) {
            for (buy in arrayBuy) {
                if (id == buy.id) {
                    arrayBuy.remove(buy)
                    break
                }
            }
            if (arrayBuy.isEmpty()) {
                val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                prefs?.edit()?.remove("arrayListShopBuy")?.apply()
            }else{
                saveArrayList(arrayBuy,"arrayListShopBuy")
            }

        }

        if (arrayShop != null) {
            for (i in 0 until arrayShop.count()) {
                if (id == arrayShop[i].id) {
                    arrayShop[i].isShop = false
                }
            }
            saveArrayList(arrayShop,"arrayListShopping")
        }
        loadProductsInShop()


    }


}


class ProductItemAdapter(val allProducts: MutableList<Productos>, val adapterOnClick: AdapterOnClick) : RecyclerView.Adapter<ProductItemAdapter.ProductItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductItemAdapter.ProductItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.item_product_shopping,parent,false)
        return ProductItemViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: ProductItemAdapter.ProductItemViewHolder, position: Int) {
        holder.bind(allProducts[position])

    }

    override fun getItemCount(): Int {
        return allProducts.size
    }

    interface AdapterOnClick {
        fun onClick(id : Int)
    }

    inner class ProductItemViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val product_title = view.findViewById<TextView>(R.id.product_title_shopping)
        private val product_price = view.findViewById<TextView>(R.id.product_price_shopping)
        private val product_image = view.findViewById<ImageView>(R.id.product_image_shopping)
        private val button_remove = view.findViewById<Button>(R.id.button_remove_shopping)

        fun bind(productos: Productos) {

            product_title.text = productos.title
            product_price.text = productos.price.toString()
            Picasso.get().load(productos.image).into(product_image)
        }
        init {

            button_remove.setOnClickListener{
                adapterOnClick.onClick(allProducts[position].id)
            }
        }

    }


}
