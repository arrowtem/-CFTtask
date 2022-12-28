package com.example.bank

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    private lateinit var network: TextView
    private lateinit var brand: TextView
    private lateinit var cardLength: TextView
    private lateinit var cardLuhn: TextView
    private lateinit var type: TextView
    private lateinit var prepaid: TextView
    private lateinit var country: TextView
    private lateinit var countryMap: TextView
    private lateinit var bankName: TextView
    private lateinit var bankUrl: TextView
    private lateinit var bankNumber: TextView
    private lateinit var searchButton:ImageButton
    private lateinit var binInput: AutoCompleteTextView
    private lateinit var bins: ArrayList<String>
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
         editor = prefs.edit()
        network = findViewById(R.id.networkData)
        brand = findViewById(R.id.brandData)
        cardLength = findViewById(R.id.cardLengthData)
        cardLuhn = findViewById(R.id.cardLuhnData)
        type = findViewById(R.id.typeData)
        prepaid = findViewById(R.id.prepaidData)
        country = findViewById(R.id.countryData)
        countryMap = findViewById(R.id.countryData2)
        bankName = findViewById(R.id.bankName)
        bankUrl = findViewById(R.id.bankUrl)
        bankNumber = findViewById(R.id.bankNumber)
        binInput = findViewById(R.id.textEdit)
        searchButton = findViewById(R.id.searchButton)
        binInput.threshold = 1


        bins = try {
            getList()
        } catch(e: java.lang.Exception){
            arrayListOf()
        }

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.select_dialog_item, bins)
        binInput.setOnClickListener {binInput.showDropDown()  }
        binInput.setAdapter(adapter)



        binInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Выводим выпадающий список
                binInput.showDropDown()

            }
        }

        searchButton.setOnClickListener {
            if(binInput.text!=null && binInput.text.toString()!=""  ){
            getData(binInput.text.toString() , applicationContext)
            if(!bins.contains(binInput.text.toString()) ){
                 bins.add(0,binInput.text.toString())
                if(bins.size>5)
                    bins.removeAt(bins.size - 1);
            }
            else {
                bins.remove(binInput.text.toString())
                bins.add(0,binInput.text.toString())
            }
            adapter.clear()
            for(bin in bins)
                adapter.add(bin)
            setLists(bins)
        }
        }

    }


    private fun getData(bin: String, applicationContext: Context) : Boolean {
        var flag = false
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)

        // on below line we are creating a variable for request
        // and initializing it with json object request
        var url = "https://lookup.binlist.net/$bin"
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->


            try {

                network.text = response.getString("scheme")

            } catch (e: Exception) {
                network.text = "null";
            }
            try {

                brand.text = response.getString("brand")

            } catch (e: Exception) {
                brand.text = "null";
            }

            try {

                cardLength.text = response.getJSONObject("number").getString("length")

            } catch (e: Exception) {
                cardLength.text = "null"
            }


            try {
                cardLuhn.text = when (response.getJSONObject("number").getString("luhn")) {
                    "true" -> "Yes"
                    "false" -> "No"
                    else -> {
                        "null"
                    }
                }
            }
        catch (e: Exception) {
            cardLuhn.text = "null"
        }

            try {
                type.text = response.getString("type")
            }
            catch (e: Exception) {
                type.text= "null"
            }

            try {
                prepaid.text = when (response.getString("prepaid")) {
                    "true" -> "Yes"
                    "false" -> "No"
                    else -> {
                        "null"
                    }
                }
            }
            catch (e: Exception) {
                prepaid.text= "null"
            }

            try {
                country.text = response.getJSONObject("country").getString("name")
            }
            catch (e: Exception) {
                country.text= "null"
            }
            try {
                countryMap.text = "geo:" + response.getJSONObject("country")
                    .getString("latitude") + "," + response.getJSONObject("country")
                    .getString("longitude")
            }
            catch (e: Exception) {
                countryMap.text= "null"
            }

            try {
                bankName.text = response.getJSONObject("bank")
                    .getString("name") + ", " + response.getJSONObject("bank").getString("city")
            }
            catch (e: Exception) {
                bankName.text= "null"
            }

                try {
                    bankNumber.text = response.getJSONObject("bank").getString("phone")
                }
                catch (e: Exception) {
                    bankNumber.text= "null"
                }

            try {
                bankUrl.text = response.getJSONObject("bank").getString("url")
            }
            catch (e: Exception) {
                bankUrl.text= "null"
            }


        }, { error ->
            // this method is called when we get
            // any error while fetching data from our API
            network.text = "null";
            brand.text = "null";
            cardLength.text = "null"
            cardLuhn.text = "null"
            type.text= "null"
            prepaid.text= "null"
            country.text= "null"
            countryMap.text= "null"
            bankName.text = "null"
            bankNumber.text= "null"
            bankUrl.text= "null"
                // in this case we are simply displaying a toast message.
            Toast.makeText(applicationContext, "Fail to get response", Toast.LENGTH_SHORT)
                .show()
            flag = false
        })
        // at last we are adding
        // our request to our queue.
        queue.add(request)
        return flag
    }

    private fun setLists(list:ArrayList<String>){
        val gson = Gson()
        val json = gson.toJson(list)//converting list to Json
        editor.putString("LIST",json)
        editor.commit()
    }
    //getting the list from shared preference
    private fun getList():ArrayList<String>{
        val gson = Gson()
        val json = prefs.getString("LIST",null)
        val type = object :TypeToken<ArrayList<String>>(){}.type//converting the json to list
        return gson.fromJson(json,type)//returning the list
    }

}

