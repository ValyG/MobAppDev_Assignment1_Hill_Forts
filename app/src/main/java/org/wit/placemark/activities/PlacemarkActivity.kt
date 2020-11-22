package org.wit.placemark.activities

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_placemark.*
import org.jetbrains.anko.*
import org.wit.placemark.R
import org.wit.placemark.helpers.readImage
import org.wit.placemark.helpers.readImageFromPath
import org.wit.placemark.helpers.showImagePicker
import org.wit.placemark.main.MainApp
import org.wit.placemark.models.Location
import org.wit.placemark.models.PlacemarkModel

class PlacemarkActivity : AppCompatActivity(), AnkoLogger {

  var placemark = PlacemarkModel()
  lateinit var app: MainApp
  val IMAGE_REQUEST1 = 1
  val IMAGE_REQUEST2 = 2
  val LOCATION_REQUEST = 3

    var edit  = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_placemark)
    toolbarAdd.title = title
    setSupportActionBar(toolbarAdd)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbarAdd.setNavigationOnClickListener {
      onBackPressed()
    }

    info("Hill Fort Activity started..")

    app = application as MainApp


    if (intent.hasExtra("placemark_edit")) {
      edit = true
      placemark = intent.extras?.getParcelable<PlacemarkModel>("placemark_edit")!!
      placemarkTitle.setText(placemark.title)
      description.setText(placemark.description)
      //placemarkImage.setImageBitmap(readImageFromPath(this, placemark.image[0]))
      placemark.image.forEachIndexed { index, image ->
        when(index){
          0->placemarkImage.setImageBitmap(readImageFromPath(this, image))
          1->placemarkImage2.setImageBitmap(readImageFromPath(this, image))
        }
      }
      when(placemark.image.size) {
        1->chooseImage.setText(R.string.change_placemark_image1)
        2->{
          chooseImage.setText(R.string.change_placemark_image1)
          chooseImage2.setText(R.string.change_placemark_image2)
        }
      }
      btnAdd.setText(R.string.save_placemark)
    }

    btnAdd.setOnClickListener() {
      placemark.title = placemarkTitle.text.toString()
      placemark.description = description.text.toString()
      if (placemark.title.isEmpty()) {
        toast(R.string.enter_placemark_title)
      } else {
        if (edit) {
          app.placemarks.update(placemark.copy())
        } else {
          app.placemarks.create(placemark.copy())
        }
        info("add Button Pressed: $placemarkTitle")
        setResult(AppCompatActivity.RESULT_OK)
        finish()
      }
    }

    chooseImage.setOnClickListener {
      showImagePicker(this, IMAGE_REQUEST1)
    }
    chooseImage2.setOnClickListener{
      showImagePicker(this,IMAGE_REQUEST2)
    }

    placemarkLocation.setOnClickListener {
      val location = Location(52.245696, -7.139102, 15f)
      if (placemark.zoom != 0f) {
        location.lat =  placemark.lat
        location.lng = placemark.lng
        location.zoom = placemark.zoom
      }
      startActivityForResult(intentFor<MapActivity>().putExtra("location", location), LOCATION_REQUEST)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    if(edit)
      menuInflater.inflate(R.menu.menu_placemark_edit, menu)
    else
      menuInflater.inflate(R.menu.menu_placemark, menu)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.item_cancel -> {
        finish()
      }
      R.id.item_delete ->{
        alert("Are you sure want to delete?") {
          title = "Alert"
          positiveButton("Delete"){
            app.placemarks.delete(placemark)
            Toast.makeText(applicationContext,"Deleted!",Toast.LENGTH_LONG).show()
            setResult(AppCompatActivity.RESULT_OK)
            finish()
          }
          negativeButton("Cancel"){}
        }.show()
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      IMAGE_REQUEST1 -> {
        if (data != null) {
            if(data.getData()!=null) {
                placemark.image.add(0,data.getData().toString())
                placemarkImage.setImageBitmap(readImage(this, resultCode, data))
                chooseImage.setText(R.string.change_placemark_image1)
            }
        }
      }
      IMAGE_REQUEST2->{
        if (data != null) {
          if(data.getData()!=null) {
            if(placemark.image.size>=1) {
              if(placemark.image.size>1)
                placemark.image.removeAt(1)
              placemark.image.add(1, data.getData().toString())
              placemarkImage2.setImageBitmap(readImage(this, resultCode, data))
              chooseImage2.setText(R.string.change_placemark_image2)
            }else if(placemark.image.size<1){
              Toast.makeText(applicationContext,"Please select Image1 first!",Toast.LENGTH_LONG).show()
            }
          }
        }
      }
      LOCATION_REQUEST -> {
        if (data != null) {
          val location = data.extras?.getParcelable<Location>("location")!!
          placemark.lat = location.lat
          placemark.lng = location.lng
          placemark.zoom = location.zoom
        }
      }
    }
  }
}

