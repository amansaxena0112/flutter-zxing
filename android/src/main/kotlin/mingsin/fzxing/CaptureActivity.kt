package mingsin.fzxing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CaptureActivity : Activity() {
    private var lastBarcode = "INVALID_STRING_STATE"
    private lateinit var scannerView: DecoratedBarcodeView
    private var mobile_number: EditText ?= null
    private var barcode_number: EditText ?= null
    private var total_items: TextView ?= null
    private var title_text: TextView ?= null
    private var scanned_items: TextView ?= null
    private var items_layout: LinearLayout ?= null
    private var add_button: LinearLayout ?= null
    private var back_layout: LinearLayout ?= null
    private var number_layout: LinearLayout ?= null
    private var barcode_layout: LinearLayout ?= null
    private var continue_button : RelativeLayout ?= null
    private var bottom_layout : RelativeLayout ?= null
    private val list = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        val isContinuous = intent.extras[keyIsContinuous] as Boolean
        val isBeep = intent.getBooleanExtra(Intents.Scan.BEEP_ENABLED, true)
        val interval = intent.extras[keyContinuousInterval] as? Int ?: 1000
        val refNumber = intent.extras[keyRefNumber] as? String ?: ""
        var lastTime = System.currentTimeMillis()
        val beepManager = BeepManager(this)
        scannerView = findViewById(R.id.scanner_view)
        scanned_items = findViewById(R.id.scanned_items)
        mobile_number = findViewById(R.id.mobile_number)
        barcode_number = findViewById(R.id.barcode_number)
        total_items = findViewById(R.id.total_items)
        title_text = findViewById(R.id.title_text)
        continue_button = findViewById(R.id.continue_button)
        bottom_layout = findViewById(R.id.bottom_layout)
        items_layout = findViewById(R.id.items_layout)
        number_layout = findViewById(R.id.number_layout)
        barcode_layout = findViewById(R.id.barcode_layout)
        add_button = findViewById(R.id.add_button)
        back_layout = findViewById(R.id.back_layout)
        var formatRefNumber = refNumber.replace("[","")
        var formatedRefNumber = formatRefNumber.replace("]","")
        var results: List<String> = formatedRefNumber.split(",").map { it.trim() }

        back_layout!!.setOnClickListener({
            finish()
        })

        continue_button!!.setOnClickListener({
            if (!isContinuous) {
                var mobile_no = mobile_number!!.text.toString()
                if (mobile_no.length < 10) {
                    Toast.makeText(this, "Enter 10 digit mobile number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                list.add(mobile_no)
            }
            setResult()
            finish()
        })
        if (isContinuous){
            number_layout!!.visibility = View.GONE
            barcode_layout!!.visibility = View.VISIBLE
            items_layout!!.visibility = View.VISIBLE
            total_items!!.text = results.size.toString()
            scanned_items!!.text = list.size.toString()
            title_text!!.text = "SCAN ITEMS"
        }

        add_button!!.setOnClickListener({
            var barcodeNumber = barcode_number!!.text.toString()
            if(formatedRefNumber.contains(barcodeNumber)  && lastBarcode != barcodeNumber) {
                if (isBeep) {
                    beepManager.playBeepSound()
                }
                lastBarcode = barcodeNumber
                list.add(barcodeNumber)
            }else{
                Toast.makeText(this@CaptureActivity, "Enter valid package", Toast.LENGTH_SHORT).show()
            }
        })
        scannerView.setStatusText("")
        list.clear()

        if (isContinuous) {
            scannerView.decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.text?.let {
                        val now = System.currentTimeMillis()
                        if (now - lastTime < interval && lastBarcode == it) {
                            //Toast.makeText(this@CaptureActivity, "Item already added", Toast.LENGTH_SHORT).show()
                            return
                        }
                        lastBarcode = it
                        lastTime = System.currentTimeMillis()
                        if(results.contains(it) && !list.contains(it)) {

                            if (isBeep) {
                                beepManager.playBeepSound()
                            }
                            list.add(it)
                            scanned_items!!.text = list.size.toString()
                        }else{
                            Toast.makeText(this@CaptureActivity, "Enter valid package", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
                }
            })
        } else {
            scannerView.decodeSingle(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.text?.let {
                        if (isBeep) {
                            beepManager.playBeepSound()
                        }
                        list.add(it)
                        setResult()
                        finish()
                    }
                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
                }
            })
        }
    }

    private fun setResult() {
        val data = Intent()
        data.putExtra("result", list)
        setResult(RESULT_OK, data)
    }

    override fun onBackPressed() {
        setResult()
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        scannerView.resume()
    }

    override fun onPause() {
        super.onPause()
        scannerView.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return scannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }


}