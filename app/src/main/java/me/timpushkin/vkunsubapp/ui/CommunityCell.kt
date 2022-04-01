package me.timpushkin.vkunsubapp.ui

import android.net.Uri
import android.view.View
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.drawee.view.SimpleDraweeView
import me.timpushkin.vkunsubapp.R
import kotlin.math.sqrt

@Composable
fun CommunityCell(
    name: String,
    photoUri: Uri,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                factory = { context ->
                    (View.inflate(context, R.layout.community_photo, null) as SimpleDraweeView)
                        .apply { setImageURI(photoUri, context) }
                },
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
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
                    }
            )

            if (isSelected) {
                val iconOffset = dimensionResource(R.dimen.community_photo_size).value / sqrt(2.0)

                Icon(
                    painter = painterResource(R.drawable.ic_check_circle_on_28),
                    contentDescription = "Selected",
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.background,
                            shape = CircleShape
                        )
                        .offset(x = iconOffset.dp, y = iconOffset.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }

        Text(
            text = name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
    }
}
