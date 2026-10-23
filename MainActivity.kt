
package com.example.spendwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spendwise.ui.theme.SpendWiseTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendWiseTheme {
                SpendWiseApp()
            }
        }
    }
}

@Composable
fun SpendWiseApp() {
    var dailyLimit by remember { mutableStateOf("500") }
    var expenseAmount by remember { mutableStateOf("") }
    var totalSpent by remember { mutableStateOf(0.0) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val limit = dailyLimit.toDoubleOrNull() ?: 0.0
    val remaining = limit - totalSpent

    val progress = if (limit > 0) {
        (totalSpent / limit).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseAmount = ""
                    errorMessage = ""
                    showExpenseDialog = true
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SpendWise",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your Personal Finance Assistant",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Today's Spending",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₹${formatMoney(totalSpent)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Daily budget: ₹${dailyLimit}")

                        Spacer(modifier = Modifier.height(4.dp))

                        if (remaining < 0) {
                            Text(
                                text = "Budget exceeded by ₹${formatMoney(-remaining)}",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Remaining: ₹${formatMoney(remaining)}")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Set Daily Budget",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = dailyLimit,
                    onValueChange = { input ->
                        if (input.isEmpty() ||
                            input.matches(Regex("^\\d*\\.?\\d{0,2}$"))
                        ) {
                            dailyLimit = input
                        }
                    },
                    label = { Text("Daily limit (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = { showExpenseDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Expense")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Summary",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Total expenses: ₹${formatMoney(totalSpent)}")

                        Text(
                            text = if (limit > 0 && totalSpent >= limit) {
                                "Your daily budget limit has been reached."
                            } else {
                                "Keep tracking your daily expenses."
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Next: Salary, EMI, SIP and expense history",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = {
                showExpenseDialog = false
                errorMessage = ""
            },
            title = {
                Text("Add Expense")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { input ->
                            expenseAmount = input
                            errorMessage = ""
                        },
                        label = { Text("Amount (₹)") },
                        singleLine = true
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = expenseAmount.toDoubleOrNull()

                        if (amount == null || !amount.isFinite() || amount <= 0) {
                            errorMessage = "Enter a valid amount."
                        } else {
                            totalSpent += amount
                            showExpenseDialog = false
                            expenseAmount = ""
                            errorMessage = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExpenseDialog = false
                        errorMessage = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatMoney(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}
