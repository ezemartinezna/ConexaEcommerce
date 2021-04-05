package com.conexworkshop.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val productsFragment = ProductsFragment()
        val shopFragment = ShoppingFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(productsFragment)
        bottom_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.destination_products -> makeCurrentFragment(productsFragment)
                R.id.destination_shop -> makeCurrentFragment(shopFragment)
                R.id.destination_settings -> makeCurrentFragment(settingsFragment)
            }
            true
        }

    }

    private fun makeCurrentFragment(fragment : Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            commit()
        }
}


class Productos(
    val id : Int,
    val title: String,
    val price: Double,
    val description : String,
    val category : String,
    val image: String,
    var isShop : Boolean = false
)

