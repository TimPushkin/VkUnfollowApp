package me.timpushkin.vkunsubapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunsubapp.ui.theme.VkUnsubAppTheme

@Composable
fun CounterButton(
    number: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            content()

            Spacer(modifier = Modifier.width(5.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.onPrimary,
                        shape = CircleShape
                    )
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = maxOf(placeable.width, placeable.height)
                        val height = placeable.height
                        layout(width, height) {
                            placeable.placeRelative(
                                (width - placeable.width) / 2,
                                (height - placeable.height) / 2
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(10),
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp),
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun CounterButtonPreview() {
    VkUnsubAppTheme {
        CounterButton(number = 5) {
            Text(text = "Testing text")
        }
    }
}
