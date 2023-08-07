package my.hanitracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import my.hanitracker.R

val Merriweather = FontFamily(
    Font(R.font.light, FontWeight.Light),
    Font(R.font.regular, FontWeight.Normal),
    Font(R.font.medium, FontWeight.Medium),
    Font(R.font.semi_bold, FontWeight.SemiBold),
    Font(R.font.bold, FontWeight.Bold),
    Font(R.font.extra_bold, FontWeight.ExtraBold)
)

val displayLargeTextStyle = TextStyle(
    fontSize = 32.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = Merriweather
)

val displayMediumTextStyle = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = Merriweather
)

val displaySmallTextStyle = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = Merriweather
)

val headlineLargeTextStyle = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = Merriweather
)

val headlineMediumTextStyle = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = Merriweather
)

val headlineSmallTextStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = Merriweather
)

val titleLargeTextStyle = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = Merriweather
)

val titleMediumTextStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = Merriweather
)

val titleSmallTextStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = Merriweather
)

val bodyLargeTextStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = Merriweather
)

val bodyMediumTextStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = Merriweather
)

val bodySmallTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = Merriweather
)

val labelLargeTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = Merriweather
)

val labelMediumTextStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = Merriweather
)

val labelSmallTextStyle = TextStyle(
    fontSize = 10.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = Merriweather
)


val Typography = Typography(
    displayLarge = displayLargeTextStyle,
    displayMedium = displayMediumTextStyle,
    displaySmall = displaySmallTextStyle,
    headlineLarge = headlineLargeTextStyle,
    headlineMedium = headlineMediumTextStyle,
    headlineSmall = headlineSmallTextStyle,
    titleLarge = titleLargeTextStyle,
    titleMedium = titleMediumTextStyle,
    titleSmall = titleSmallTextStyle,
    bodyLarge = bodyLargeTextStyle,
    bodyMedium = bodyMediumTextStyle,
    bodySmall = bodySmallTextStyle,
    labelLarge = labelLargeTextStyle,
    labelMedium = labelMediumTextStyle,
    labelSmall = labelSmallTextStyle,
)