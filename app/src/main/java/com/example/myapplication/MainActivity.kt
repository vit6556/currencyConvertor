package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currencies = resources.getStringArray(R.array.currencies)
        val spinnerFrom = findViewById<Spinner>(R.id.currencyFromSpinner)
        val spinnerTo = findViewById<Spinner>(R.id.currencyToSpinner)
        val currencyFrom = findViewById<TextView>(R.id.currencyFromTextView)
        val currencyTo = findViewById<TextView>(R.id.currencyToTextView)
        val inputNumber = findViewById<EditText>(R.id.inputNumber)

        if (spinnerFrom != null) createSpinnerAdapter(spinnerFrom, currencyFrom, currencies)
        if (spinnerTo != null) createSpinnerAdapter(spinnerTo, currencyTo, currencies)

        val button: Button = findViewById(R.id.convert)
        button.setOnClickListener {
            try {
                inputNumber.text.toString().toFloat()
                getRate(currencyFrom.text.toString(), currencyTo.text.toString())
            } catch (e: NumberFormatException) {
                showError("Input error", "Invalid input value")
            }
        }
    }

    private fun showError(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which -> }
        builder.show()
    }

    private fun createSpinnerAdapter(spinner: Spinner, currency: TextView, currencies: Array<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                currency.text = currencies[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getRate(currencyFrom: String, currencyTo: String) {
        val URL = "https://api.exchangeratesapi.io/latest?base=$currencyFrom"
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(URL).build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    showError("Networ error", "Check your internet connection")
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json = JSONObject(response?.body()?.string().toString())
                val coef = JSONObject(json.get("rates").toString()).get(currencyTo).toString().toFloat()
                val resultString = (inputNumber.text.toString().toFloat() * coef).toString()
                runOnUiThread {
                    result.text = Editable.Factory.getInstance().newEditable(resultString)
                }
            }
        })
    }
}
