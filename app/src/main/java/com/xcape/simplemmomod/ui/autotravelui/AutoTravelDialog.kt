package com.xcape.simplemmomod.ui.autotravelui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTravelDialog(
    errorMessage: String,
    onConsumeError: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConsumeError
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error:",
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier.padding(bottom = 15.dp, top = 10.dp)
                )

                TextField(
                    value = errorMessage,
                    onValueChange = {  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(
                            bottom = 30.dp,
                            end = 15.dp,
                            start = 15.dp
                        ),
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal
                    ),
                    readOnly = true
                )

                Button(
                    onClick = onConsumeError,
                    modifier = Modifier.align(Alignment.End)
                        .wrapContentSize()
                ) {
                    Text(
                        text = "Confirm",
                        style = TextStyle(
                            textAlign = TextAlign.Justify,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAlertDialog() {
    SimpleMMOModTheme {
        AutoTravelDialog("Error", {})
    }
}