package com.example.auctionsapp.core.domain
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.auctionsapp.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuctionCategory {
    @SerialName("electronics") ELECTRONICS,
    @SerialName("home_garden") HOME_GARDEN,
    @SerialName("fashion") FASHION,
    @SerialName("sports") SPORTS,
    @SerialName("automotive") AUTOMOTIVE,
    @SerialName("toys_games") TOYS_GAMES,
    @SerialName("books_media") BOOKS_MEDIA,
    @SerialName("beauty_health") BEAUTY_HEALTH,
    @SerialName("baby_kids") BABY_KIDS,
    @SerialName("property") PROPERTY,
    @SerialName("pets") PETS,
    @SerialName("other") OTHER;

    companion object {
        fun fromString(value: String?): AuctionCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }

    val color: androidx.compose.ui.graphics.Color
        get() = when (this) {
            ELECTRONICS -> Color(0xFF2196F3)
            HOME_GARDEN -> Color(0xFF4CAF50)
            FASHION -> Color(0xFFE91E63)
            SPORTS -> Color(0xFFFF9800)
            AUTOMOTIVE -> Color(0xFF607D8B)
            TOYS_GAMES -> Color(0xFFFFC107)
            BOOKS_MEDIA -> Color(0xFF9C27B0)
            BEAUTY_HEALTH -> Color(0xFFFF4081)
            BABY_KIDS -> Color(0xFF00BCD4)
            PROPERTY -> Color(0xFF795548)
            PETS -> Color(0xFFFFEB3B)
            OTHER -> Color(0xFF9E9E9E)
        }

    private val iconRes: Int
        get() = when (this) {
            ELECTRONICS -> R.drawable.ic_category_electronics
            HOME_GARDEN -> R.drawable.ic_category_home_garden
            FASHION -> R.drawable.ic_category_fashion
            SPORTS -> R.drawable.ic_category_sports
            AUTOMOTIVE -> R.drawable.ic_category_automotive
            TOYS_GAMES -> R.drawable.ic_category_toys_games
            BOOKS_MEDIA -> R.drawable.ic_category_books_media
            BEAUTY_HEALTH -> R.drawable.ic_category_beauty_health
            BABY_KIDS -> R.drawable.ic_category_baby_kids
            PROPERTY -> R.drawable.ic_category_property
            PETS -> R.drawable.ic_category_pets
            OTHER -> R.drawable.ic_category_other
        }

    @Composable
    fun iconPainter(): Painter = painterResource(id = iconRes)
}

@Composable
fun CategoryChip(
    category: AuctionCategory,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    iconSize: Dp = 18.dp,
    horizontalPadding: Dp = 18.dp,
    cornerRadius: Dp = 20.dp,
    chipHeight: Dp = 30.dp
) {
    val chipContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .height(chipHeight)
        ) {
            Icon(
                painter = category.iconPainter(),
                contentDescription = category.name,
                tint = category.color,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name.replace('_', '/'),
                    color = category.color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (onClick != null) {
        Surface(
            color = category.color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier
                .height(chipHeight)
                .clickable { onClick() }
        ) {
            chipContent()
        }
    } else {
        Surface(
            color = category.color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(chipHeight)
        ) {
            chipContent()
        }
    }
}
