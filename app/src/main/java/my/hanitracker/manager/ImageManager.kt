package my.hanitracker.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.graphics.*
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import my.hanitracker.R
import java.io.IOException
import java.lang.Integer.min
import kotlin.math.ceil

fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
    drawableToBitmap(ContextCompat.getDrawable(context, resourceId))

private fun drawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
    if (sourceDrawable == null) {
        return null
    }
    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
        val constantState = sourceDrawable.constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

fun Bitmap.setAnnotationBitmap(context: Context) : Bitmap? = createCircularBitmapWithImage(context, this, 10.0f, Color.BLACK)

fun uriToBitmap(context: Context, uri: Uri, callback: (Bitmap?) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .load(uri)
        .apply(RequestOptions.overrideOf(150, 150))
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                callback(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                callback(null)
            }
        })
}


private fun createCircularBitmapWithImage(context: Context, bitmap: Bitmap, borderWidth: Float, borderColor: Int): Bitmap? {
    val width = bitmap.width
    val height = bitmap.height
    val borderWidthInt = ceil(borderWidth).toInt()
    val size = min(width, height)

    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)

    // Set up the paint for drawing the circle
    val paint = Paint()
    paint.isAntiAlias = true
    paint.color = borderColor
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderWidth

    // Calculate the radius and center of the circle
    val radius = size / 2f
    val centerX = width / 2f
    val centerY = height / 2f

    // Draw the circle with border
    canvas.drawCircle(centerX, centerY, radius - borderWidth, paint)

    // Set up the paint for drawing the bitmap
    val bitmapPaint = Paint()
    bitmapPaint.isAntiAlias = true
    bitmapPaint.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

    // Draw the bitmap inside the circle
    canvas.drawCircle(centerX, centerY, radius - borderWidth, bitmapPaint)

    return outputBitmap
}
