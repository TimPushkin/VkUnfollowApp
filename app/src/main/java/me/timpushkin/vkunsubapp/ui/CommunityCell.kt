package me.timpushkin.vkunsubapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunsubapp.R

@Composable
fun CommunityCell(
    name: String,
    image: ImageBitmap,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                bitmap = image,
                contentDescription = "Community image",
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .align(Alignment.Center)
                    .apply {
                        if (isSelected)
                            border(
                                width = 2.dp,
                                color = MaterialTheme.colors.background,
                                shape = CircleShape
                            ).border(
                                width = 1.dp,
                                color = MaterialTheme.colors.primary,
                                shape = CircleShape
                            )
                    },
                contentScale = ContentScale.Crop
            )

            Icon(
                painter = painterResource(R.drawable.ic_check_circle_on_28),
                contentDescription = "Selected",
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.background,
                        shape = CircleShape
                    )
                    .offset(x = 20.dp, y = 20.dp), // TODO: check it
                tint = MaterialTheme.colors.primary
            )
        }

        Text(
            text = name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
    }
}
