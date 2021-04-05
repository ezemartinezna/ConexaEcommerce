package com.conexworkshop.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_products.*
import kotlinx.android.synthetic.main.item_product.view.*
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type


class ProductsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadFilterCategory()

        loadMemoryArrayList()

        buttonSearch.setOnClickListener {
            filterAndSearch(" - ",searchText.text.toString())
        }

        buttonOrder.setOnClickListener {

            val newArrayShop = getArrayList("arrayListShopping")

            if (newArrayShop != null) {
                newArrayShop.sortBy { it.title?.toString() }
                saveArrayList(newArrayShop,"arrayListShopping")
            }
            filterAndSearch(" - ","")
        }

    }

    private fun loadMemoryArrayList() {


        val newArrayShop = getArrayList("arrayListShopping")
        if (newArrayShop == null) {
            loadProducts()
        }else{
            val groupAdapter = GroupAdapter<GroupieViewHolder>()
                for (producto in newArrayShop) {
                    groupAdapter.add(ProductItem(producto))
                }
                activity?.runOnUiThread{

                    recyclerView_products.adapter = groupAdapter

                }

                groupAdapter.setOnItemClickListener { item, view ->

                    val arrayBuy = getArrayList("arrayListShopBuy")

                    val allProducts = item as ProductItem
                    val isTrue = allProducts.allProducts.isShop
                    allProducts.allProducts.isShop = !isTrue
                    if (isTrue) {
                        if (arrayBuy != null) {
                            for (buy in arrayBuy) {
                                if (buy.id == allProducts.allProducts.id) {
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

                        Toast.makeText(context,"REMOVIDO DE CARRITO", Toast.LENGTH_SHORT).show()
                    }else{
                        if (arrayBuy.isNullOrEmpty()) {
                            val newArrayBuy = mutableListOf<Productos>()
                           newArrayBuy.add(allProducts.allProducts)
                            saveArrayList(newArrayBuy,"arrayListShopBuy")
                        }else{
                            arrayBuy?.add(allProducts.allProducts)
                            saveArrayList(arrayBuy,"arrayListShopBuy")
                        }

                        Toast.makeText(context,"AGREGADO A CARRITO", Toast.LENGTH_SHORT).show()
                    }

                    saveArrayList(newArrayShop,"arrayListShopping")

                    groupAdapter.notifyDataSetChanged();

                }
        }

    }

    private fun loadFilterCategory() {

        val url = "https://fakestoreapi.com/products"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response?.body?.string()
                val gson = GsonBuilder().create()

                val collectionType: Type =
                    object : TypeToken<MutableList<Productos?>?>() {}.type
                val productos: MutableList<Productos> = gson.fromJson(body, collectionType)

                val arrayList = arrayListOf<String>()
                arrayList.add(" - ")

                for (producto in productos) {
                    val category = producto.category
                    arrayList.add(category)
                }

                val arrayListNotDuplicate = arrayList.distinct()

                val spinner = view?.findViewById<Spinner>(R.id.spinnerCategory)
                val arrayAdapter =
                    context?.let { ArrayAdapter<String>(it, android.R.layout.simple_spinner_item, arrayListNotDuplicate) }
                arrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                activity?.runOnUiThread{

                    spinner?.adapter = arrayAdapter

                }

                spinner?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val tutorialsName = parent.getItemAtPosition(position).toString()
                        filterAndSearch(tutorialsName,"")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

        })

    }

    private fun filterAndSearch(filter : String, searching : String) {

        val newArrayShop = getArrayList("arrayListShopping")
        val groupAdapter = GroupAdapter<GroupieViewHolder>()
        if (newArrayShop != null) {
            for (producto in newArrayShop) {
                if (filter == " - " && searching == "") {
                    groupAdapter.add(ProductItem(producto))
                }else{
                    if (filter != " - ") {
                        if (filter == producto.category) {
                            groupAdapter.add(ProductItem(producto))
                        }
                    }
                    if (searching != ""){
                        if (producto.title.contains(searching)) {
                            groupAdapter.add(ProductItem(producto))
                        }
                    }
                }
            }
            saveArrayList(newArrayShop,"arrayListShopping")
            activity?.runOnUiThread{

                recyclerView_products.adapter = groupAdapter

            }

            groupAdapter.setOnItemClickListener { item, view ->

                val arrayBuy = getArrayList("arrayListShopBuy")

                val allProducts = item as ProductItem
                val isTrue = allProducts.allProducts.isShop
                allProducts.allProducts.isShop = !isTrue

                if (isTrue) {
                    if (arrayBuy != null) {
                        for (buy in arrayBuy) {
                            if (buy.id == allProducts.allProducts.id) {
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

                    Toast.makeText(context,"REMOVIDO DE CARRITO", Toast.LENGTH_SHORT).show()
                }else{
                    if (arrayBuy.isNullOrEmpty()) {
                        val newArrayBuy = mutableListOf<Productos>()
                        newArrayBuy.add(allProducts.allProducts)
                        saveArrayList(newArrayBuy,"arrayListShopBuy")
                    }else{
                        arrayBuy?.add(allProducts.allProducts)
                        saveArrayList(arrayBuy,"arrayListShopBuy")
                    }

                    Toast.makeText(context,"AGREGADO A CARRITO", Toast.LENGTH_SHORT).show()
                }


                saveArrayList(newArrayShop,"arrayListShopping")

                groupAdapter.notifyDataSetChanged();

            }

        }


    }

    private fun loadProducts() {

        val url = "https://fakestoreapi.com/products"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error Fakestore")
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response?.body?.string()
                val gson = GsonBuilder().create()

                val collectionType: Type =
                    object : TypeToken<MutableList<Productos?>?>() {}.type
                val productos: MutableList<Productos> = gson.fromJson(body, collectionType)

                val groupAdapter = GroupAdapter<GroupieViewHolder>()

                for (producto in productos) {
                    groupAdapter.add(ProductItem(producto))
                }

                saveArrayList(productos,"arrayListShopping")

                loadMemoryArrayList()

            }

        })

    }


    fun saveArrayList(list: MutableList<Productos>, key: String?) {
        val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = prefs?.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor?.putString(key, json)
        editor?.apply()
    }

    fun getArrayList(key: String?): MutableList<Productos>? {
        val prefs = view?.context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = prefs?.getString(key, null)
        val type: Type = object : TypeToken<MutableList<Productos>>() {}.type
        return gson.fromJson(json, type)
    }

}

class ProductItem(val allProducts: Productos) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.product_title.text = allProducts.title
        viewHolder.itemView.product_price.text = "$" + allProducts.price.toString()

        Picasso.get().load(allProducts.image).into(viewHolder.itemView.product_image)

        if (allProducts.isShop) {
            viewHolder.itemView.image_shopping.setImageResource(R.drawable.shop_check)
        }else{
            viewHolder.itemView.image_shopping.setImageResource(R.drawable.shop_add)
        }

    }

    override fun getLayout(): Int {
        return R.layout.item_product
    }

}
