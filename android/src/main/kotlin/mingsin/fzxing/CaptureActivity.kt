package mingsin.fzxing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
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
    private var continue_button : RelativeLayout ?= null
    private val list = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        val isContinuous = intent.extras[keyIsContinuous] as Boolean
        val isBeep = intent.getBooleanExtra(Intents.Scan.BEEP_ENABLED, true)
        val interval = intent.extras[keyContinuousInterval] as? Int ?: 1000
        var lastTime = System.currentTimeMillis()
        val beepManager = BeepManager(this)
        scannerView = findViewById(R.id.scanner_view)
        mobile_number = findViewById(R.id.mobile_number)
        continue_button = findViewById(R.id.continue_button)
        continue_button!!.setOnClickListener({
            var mobile_no = mobile_number!!.text.toString()
            if (mobile_no.length < 10){
                Toast.makeText(this,"Enter 10 digit mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            list.add(mobile_no)
            setResult()
            finish()
        })
        scannerView.setStatusText("")
        list.clear()

        if (isContinuous) {
            scannerView.decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.text?.let {
                        val now = System.currentTimeMillis()
                        if (now - lastTime < interval && lastBarcode == it) {
                            return
                        }
                        if (isBeep) {
                            beepManager.playBeepSound()
                        }
                        lastBarcode = it
                        list.add(it)
                        lastTime = System.currentTimeMillis()
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