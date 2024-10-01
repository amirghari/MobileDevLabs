package com.example.lotto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lotto.ui.theme.LottoTheme
import androidx.compose.foundation.layout.Spacer as Spacer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    LottoMain()
                }
            }
        }
    }
}

@Composable
fun LottoMain (modifier: Modifier = Modifier) {
    var nums by remember { mutableStateOf((1..40).toList()) }
    var selectedNums by remember { mutableStateOf(listOf<Int>()) }
    val flag by remember { mutableStateOf(false) }
    val flagTwo by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val secret by remember { mutableStateOf((1..40).shuffled().take(7).toList()) }

    LazyColumn(modifier = modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Spacer(modifier = Modifier.padding(20.dp))
            LazyRow(modifier = Modifier
                .wrapContentHeight()
                .wrapContentWidth(),
                ){
                items(selectedNums) { num ->
                    Button( onClick = { selectedNums = selectedNums - num
                    nums = nums + num}
                    ){
                        Text(text = num.toString())
                    }
                }
            }
            if (selectedNums.size == 7) {
                Spacer(modifier = Modifier.padding(20.dp))
                Column(
                    modifier = modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                   Button(onClick = {if (selectedNums.equals(secret))
                   { flag == true }
                       if (flag) {
                          result += "Success,secret is $secret"
                       }
                       else {
                          result += "Fail  ,secret is $secret"
                       }}) {
                       Text(text = "Click")
                   }
                }
                Spacer(modifier = Modifier.padding(20.dp))

                Spacer(modifier = Modifier.padding(2.dp))
                    Text(text = "$result")


            }
            Spacer(modifier = Modifier.padding(20.dp))
        }
        items(nums) { num ->
            Spacer(modifier = Modifier.padding(5.dp))
            Button( onClick = { nums = nums - num
                if ( selectedNums.size < 7 ) selectedNums = selectedNums + num }) {
             Text(text = num.toString())
            }

        }

    }
}

