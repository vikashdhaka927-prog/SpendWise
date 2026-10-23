package com.example.spendwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("SpendWiseData", MODE_PRIVATE)

        setContent {
            var budget by remember {
                mutableStateOf(prefs.getInt("budget", 500).toString())
            }
            var amount by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }
            var expenses by remember {
                mutableStateOf(
                    try {
                        val arr = JSONArray(prefs.getString("expenses", "[]"))
                        List(arr.length()) { i ->
                            val obj = arr.getJSONObject(i)
                            obj.getString("note") to obj.getDouble("amount")
                        }
                    } catch (_: Exception) {
                        emptyList()
                    }
                )
            }

            val total = expenses.sumOf { it.second }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text("SpendWise", style = MaterialTheme.typography.headlineLarge)
                    Text("Today's Spending: ₹${"%.2f".format(total)}")

                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Daily Budget (₹)") }
                    )

                    Button(onClick = {
                        budget.toIntOrNull()?.let {
                            prefs.edit().putInt("budget", it).apply()
                        }
                    }) {
                        Text("Save Budget")
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Expense Name") }
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") }
                    )

                    Button(onClick = {
                        val value = amount.toDoubleOrNull()
                        if (value != null && value > 0) {
                            expenses = expenses + (note.ifBlank { "Expense" } to value)

                            val arr = JSONArray()
                            expenses.forEach { (name, price) ->
                                arr.put(JSONObject().apply {
                                    put("note", name)
                                    put("amount", price)
                                })
                            }

                            prefs.edit()
                                .putString("expenses", arr.toString())
                                .apply()

                            amount = ""
                            note = ""
                        }
                    }) {
                        Text("Add Expense")
                    }

                    Text(
                        if (total > (budget.toDoubleOrNull() ?: 0.0))
                            "Budget exceeded!"
                        else "Within budget"
                    )

                    Text("Expense History")
                    LazyColumn {
                        items(expenses) { expense ->
                            Text("${expense.first}: ₹${expense.second}")
                        }
                    }
                }
            }
        }
    }
}
