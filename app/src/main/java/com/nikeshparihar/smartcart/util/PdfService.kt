package com.nikeshparihar.smartcart.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.nikeshparihar.smartcart.model.ShoppingList
import java.io.File
import java.io.FileOutputStream

object PdfService {

    fun generatePdf(context: Context, list: ShoppingList): Uri? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Title
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.rgb(34, 139, 34) // DarkGreen approx
        canvas.drawText(list.name, 40f, 60f, titlePaint)

        // Subtitle/Count
        paint.textSize = 14f
        paint.color = Color.GRAY
        canvas.drawText("Total Items: ${list.items.size}", 40f, 90f, paint)

        // Items
        paint.color = Color.BLACK
        paint.textSize = 16f
        var yPos = 140f

        for (item in list.items) {
            val qty = item.quantity?.let { "x$it" } ?: ""
            val category = if(item.category.isNotBlank()) "[${item.category}]" else ""
            canvas.drawText("• ${item.name}   $qty  $category", 50f, yPos, paint)
            
            // Draw a small checkbox line
            paint.style = Paint.Style.STROKE
            canvas.drawRect(30f, yPos - 12f, 42f, yPos, paint)
            paint.style = Paint.Style.FILL

            yPos += 30f
            if (yPos > 800f) {
                // Simplification for the demo: cut off at 1 page
                break
            }
        }

        pdfDocument.finishPage(page)

        // Save to cache directory
        val file = File(context.cacheDir, "ShoppingList_${list.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
