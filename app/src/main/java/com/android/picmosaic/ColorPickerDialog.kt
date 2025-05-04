package com.android.picmosaic.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import com.android.picmosaic.R

class ColorPickerDialog(context: Context, initialColor: Int, private val onColorSelected: (Int) -> Unit) : Dialog(context) {

    init {
        setContentView(R.layout.dialog_color_picker)
        setTitle("Pick a Color")

        // Set dialog width to 300dp to make it "fatter"
        window?.setLayout((300 * context.resources.displayMetrics.density).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        val hueBar = findViewById<SeekBar>(R.id.hueBar)
        val saturationBar = findViewById<SeekBar>(R.id.saturationBar)
        val valueBar = findViewById<SeekBar>(R.id.valueBar)
        val colorPreview = findViewById<View>(R.id.colorPreview)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        val hsv = FloatArray(3)
        Color.colorToHSV(initialColor, hsv)

        hueBar.max = 360
        saturationBar.max = 100
        valueBar.max = 100

        hueBar.progress = hsv[0].toInt()
        saturationBar.progress = (hsv[1] * 100).toInt()
        valueBar.progress = (hsv[2] * 100).toInt()

        fun updateColor() {
            val h = hueBar.progress.toFloat()
            val s = saturationBar.progress / 100f
            val v = valueBar.progress / 100f
            val color = Color.HSVToColor(floatArrayOf(h, s, v))
            colorPreview.setBackgroundColor(color)
        }

        hueBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColor()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saturationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColor()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        valueBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColor()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateColor()

        confirmButton.setOnClickListener {
            val h = hueBar.progress.toFloat()
            val s = saturationBar.progress / 100f
            val v = valueBar.progress / 100f
            val color = Color.HSVToColor(floatArrayOf(h, s, v))
            onColorSelected(color)
            dismiss()
        }
    }
}