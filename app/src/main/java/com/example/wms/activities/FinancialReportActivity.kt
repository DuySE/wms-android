package com.example.wms.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.example.wms.adapters.TransactionAdapter
import com.example.wms.apis.TransactionRepository
import com.example.wms.databinding.ActivityFinancialReportBinding
import com.example.wms.models.Transaction
import java.util.Calendar
import java.util.Locale

class FinancialReportActivity : DrawerActivity() {
    private lateinit var financialReportBinding: ActivityFinancialReportBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var txtViewReportSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        financialReportBinding = ActivityFinancialReportBinding.inflate(layoutInflater)
        setContentView(financialReportBinding.root)
        allocateActivityTitle("Financial Report")

        // Initialize text view
        txtViewReportSummary = financialReportBinding.txtReportSummary

        // Initialize transaction repository with context
        transactionRepository = TransactionRepository(this)

        transactions = mutableListOf<Transaction>()
        transactionAdapter = TransactionAdapter(transactions)

        // Filter by date
        val editTextDate: EditText = financialReportBinding.editTextDate
        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view1: DatePicker?, year1: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = String.format(
                        Locale.US,
                        "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth
                    )
                    editTextDate.setText(selectedDate)
                    transactionRepository.getTransaction(
                        editTextDate.text.toString(), onSuccess = { data ->
                            Log.d("FinancialReportActivity", "onSuccess: $data")
                            transactionAdapter.setFilteredList(data.toMutableList())
                            txtViewReportSummary.text = generateReport(transactions)
                        }, onError = { error ->
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        })
                }, year, month, day
            )
            datePickerDialog.show()
        }
        // Setup search view
        setUpSearchView()
    }

    private fun setUpSearchView() {
        val searchView = financialReportBinding.transactionSearchView
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(text: String) {
        val filteredList: MutableList<Transaction> = transactions.filter { product ->
            product.name.lowercase(Locale.getDefault())
                .contains(text.lowercase(Locale.getDefault()))
        }.toMutableList()
        transactionAdapter.setFilteredList(filteredList)
    }

    private fun generateReport(transactions: MutableList<Transaction>): String {
        val totalAmount = transactions.sumOf { it.price }
        val averageAmount = transactions.map { it.price }.average()
        val count = transactions.size
        val reportBuffer = StringBuffer()
        reportBuffer.appendLine("Financial Report for 2024-12-03")
        reportBuffer.appendLine("Total Transactions: 1")
        reportBuffer.appendLine("Total Amount: ${"$%.2f".format(2000)}")
        reportBuffer.appendLine("Average Transaction Amount: ${"$%.2f".format(averageAmount)}")
        return reportBuffer.toString()
    }
}