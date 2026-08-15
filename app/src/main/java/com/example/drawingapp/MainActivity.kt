package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import yuku.ambilwarna.AmbilWarnaDialog
import  com.example.drawingapp.R
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import java.util.*
import android.content.ContentValues

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var drawingView : DrawingView
    private lateinit var brush_btn : ImageButton
    private lateinit var red_btn : ImageButton
    private lateinit var yellow_btn : ImageButton
    private lateinit var green_btn : ImageButton
    private lateinit var blue_btn : ImageButton
    private lateinit var white_btn : ImageButton
    private lateinit var undo_btn: ImageButton
    private lateinit var color_picker: ImageButton
    private lateinit var  gallery_btn: ImageButton
    private lateinit var save_btn : ImageButton


    private val opengalleryLauncher: ActivityResultLauncher<Intent>  =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            findViewById<ImageView>(R.id.gallery_img).setImageURI(result.data?.data)
        }

    val requestPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value

                if(isGranted && permissionName== android.Manifest.permission.READ_MEDIA_IMAGES){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    opengalleryLauncher.launch(pickIntent)

                }else{
                    if(permissionName == android.Manifest.permission.READ_MEDIA_IMAGES ){
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        brush_btn= findViewById(R.id.brush_btn)
        red_btn = findViewById(R.id.red_btn)
        white_btn = findViewById(R.id.white_btn)
        blue_btn = findViewById(R.id.blue_btn)
        green_btn = findViewById(R.id.green_btn)
        yellow_btn = findViewById(R.id.yellow_btn)
        undo_btn = findViewById(R.id.undo_btn)
        color_picker= findViewById(R.id.color_picker_btn)
        gallery_btn = findViewById(R.id.gallery_btn)
        save_btn = findViewById(R.id.save_btn)

        drawingView.changeBrushSize(23.toFloat())

        brush_btn.setOnClickListener {
            ShowBrushChoice()
        }

        red_btn.setOnClickListener(this)
        yellow_btn.setOnClickListener(this)
        blue_btn.setOnClickListener(this)
        green_btn.setOnClickListener(this)
        white_btn.setOnClickListener(this)

        undo_btn.setOnClickListener (this)
        color_picker.setOnClickListener(this)
        gallery_btn.setOnClickListener(this)
        save_btn.setOnClickListener  (this)

    }
    private fun ShowBrushChoice(){
        val brushdialog = Dialog(this@MainActivity)
        brushdialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress = brushdialog.findViewById<SeekBar>(R.id.dialog_seekbar)
        val showProgressTv = brushdialog.findViewById<TextView>(R.id.dialog_seekbar_text)
        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                       drawingView.changeBrushSize(progress.toFloat())
                       showProgressTv.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                brushdialog.dismiss()
            }

        })
        brushdialog.show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.white_btn ->{
                drawingView.setColor(getColor(R.color.white))
            }
            R.id.red_btn ->{
                drawingView.setColor(getColor(R.color.red))
            }
            R.id.yellow_btn ->{
                drawingView.setColor(getColor(R.color.yellow))
            }
            R.id.blue_btn ->{
                drawingView.setColor(getColor(R.color.blue))
            }
            R.id.green_btn ->{
                drawingView.setColor(getColor(R.color.green))
            }
            R.id.undo_btn->{
                drawingView.undoPath()
            }
            R.id.color_picker_btn->{
                showColorPickerDialog()
            }
            R.id.gallery_btn->{
                 if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES )
                     != PackageManager.PERMISSION_GRANTED){
                     reqStoragePermission()
                 }else{
                     val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                     opengalleryLauncher.launch(pickIntent)

                 }

            }
            R.id.save_btn -> {
                val layout = findViewById<ConstraintLayout>(R.id.C1)
                val bitmap = getBitmapFromView(layout)

                saveImageToGallery(bitmap)
            }

        }
    }


    private fun showColorPickerDialog(){
        val dialog = AmbilWarnaDialog(this,Color.RED,object: AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
            }
        })
        dialog.show()

    }



    private fun reqStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
            this,
                android.Manifest.permission.READ_MEDIA_IMAGES
        )
            ){
            showRationalDialog()
        }else{
            requestPermission.launch(
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES) )
        }
    }


    private fun showRationalDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage permission")
            .setMessage("We need it to access internal storage")
            .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                requestPermission.launch(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                )
                dialog.dismiss()
            }
        builder.create().show()
    }


    private fun getBitmapFromView(view: View): Bitmap{
        val bitmap = Bitmap.createBitmap(view.width,view.height,
        Bitmap.Config.ARGB_8888)
        val  canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveImageToGallery(bitmap: Bitmap) {

        val filename = "DrawingApp_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingApp")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            val outputStream = contentResolver.openOutputStream(it)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                Toast.makeText(this, "Image Saved Successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
            }
            outputStream?.close()

            Toast.makeText(this, "Image Saved Successfully!", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Failed to Save Image", Toast.LENGTH_SHORT).show()
        }
    }
}
