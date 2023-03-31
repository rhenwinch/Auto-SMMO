package com.xcape.simplemmomod.ui.autotravelui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchWithLabel(
    label: String,
    isChecked: Boolean,
    onStateChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                // This is for removing ripple when Row is clicked
                indication = null,
                role = Role.Switch,
                onClick = {
                    onStateChange(!isChecked)
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {

        Text(
            text = label,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
        )
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = {
                onStateChange(it)
            },
            modifier = Modifier.scale(0.7F)
        )
    }
}