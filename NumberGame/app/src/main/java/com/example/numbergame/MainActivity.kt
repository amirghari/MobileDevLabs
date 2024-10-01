package com.example.numbergame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numbergame.ui.theme.NumberGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumberGameTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    )
                {
                    Main()
                }
            }
        }
    }
}

@Composable
fun Main( modifier: Modifier = Modifier) {
    var amountInput by remember { mutableStateOf("") }
    var gameResult by remember { mutableStateOf("") }
    val nums = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val correctGuess by remember { mutableStateOf(nums.random()) }

        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "Hello, Guess a number between 1 to 10.",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            NumberInput(value = amountInput,
                onValueChange = { amountInput = it })
            Spacer(modifier = Modifier.padding(10.dp))
            Button(onClick = {
                gameResult = start(amountInput, correctGuess)
            }
            )
            {
                Text(text = "Guess")
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = gameResult,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,)
            )
        }

        }



@Composable
fun NumberInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String)-> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Enter a number") },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
fun start(amountInput: String, correctGuess: Int): String {


    if (amountInput.isNotEmpty()) {  // Check for empty input
        val input = amountInput.toInt()
        if (input == correctGuess) {
            return "Correct"
        } else if (input < correctGuess) {
            return "Too low"
        } else {
            return "Too high"
        }
    } else {
        return "" // Return an empty string ifno input
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NumberGameTheme {
        Main()
    }
}
