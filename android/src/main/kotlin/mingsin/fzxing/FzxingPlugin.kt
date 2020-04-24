package mingsin.fzxing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.zxing.integration.android.IntentIntegrator
import com.tbruyelle.rxpermissions2.RxPermissions
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

const val keyIsContinuous = "isContinuous"
const val keyIsShipment = "isShipment"
const val keyScan = "scan"
const val keyIsBeep = "isBeep"
const val keyContinuousInterval = "continuousInterval"
const val keyRefNumber = "refNumber"
const val keyOrderNumber = "orderNumber"
const val keyScannedNumber = "scannedRefNumber"
const val keyIsBlowhorn = "isBlowhorn"

class FzxingPlugin(private val registrar: Registrar) : MethodCallHandler {

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "fzxing")
            val plugin = FzxingPlugin(registrar)
            channel.setMethodCallHandler(plugin)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            keyScan -> {
                handleScan(call, result)
            }
            else -> result.notImplemented()
        }
    }


    private fun handleScan(call: MethodCall, result: Result) {
        val argumentsMap = call.arguments as Map<*, *>
        val isBeep = argumentsMap[keyIsBeep] as? Boolean ?: true
        val isContinuous = argumentsMap[keyIsContinuous] as? Boolean ?: false
        val isBlowhorn = argumentsMap[keyIsBlowhorn] as? Boolean ?: true
        val isShipment = argumentsMap[keyIsShipment] as? Boolean ?: false
        val scanInterval = argumentsMap[keyContinuousInterval] as? Int ?: 1000
        val refNumber = argumentsMap[keyRefNumber] as? List<String> ?: ""
        val orderNumber = argumentsMap[keyOrderNumber] as? List<String> ?: ""
        val scannedNumber = argumentsMap[keyScannedNumber] as? List<String> ?: ""
        val config = Config(isBeep, isContinuous, isShipment , scanInterval, refNumber.toString(),scannedNumber.toString(), orderNumber.toString(), isBlowhorn)
        Log.d(keyIsBeep, isBeep.toString())
        Log.d(keyIsContinuous, isContinuous.toString())
        Log.d(keyIsBlowhorn, isBlowhorn.toString())
        Log.d(keyIsShipment, isShipment.toString())
        Log.d(keyScannedNumber, scannedNumber.toString())
        Log.d(keyScannedNumber, argumentsMap.toString())
        startCapture(config, result)
    }

    private fun startCapture(config: Config, result: Result) {
        val activity = registrar.activity()
        RxPermissions(activity)
                .request(Manifest.permission.CAMERA)
                .subscribe({ granted ->
                    if (granted) {
                        startScan(result, activity, config)
                    } else {
                        result.error("", "", "")
                    }
                })
    }

    private fun startScan(result: Result, activity: Activity?, config: Config) {
        if (listener.result == null) {
            registrar.addActivityResultListener(listener)
        }
        listener.result = result

        if (config.isShipment){
            IntentIntegrator(activity)
                    .setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
                    .setCaptureActivity(ShipmentCaptureActivity::class.java)
                    .setBeepEnabled(config.isBeep)
                    .addExtra(keyIsContinuous, config.isContinuous)
                    .addExtra(keyContinuousInterval, config.scanInterval)
                    .addExtra(keyRefNumber, config.refNumber)
                    .addExtra(keyOrderNumber, config.orderNumber)
                    .addExtra(keyScannedNumber, config.scannedNumber)
                    .initiateScan()
        }else {
            IntentIntegrator(activity)
                    .setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
                    .setCaptureActivity(CaptureActivity::class.java)
                    .setBeepEnabled(config.isBeep)
                    .addExtra(keyIsContinuous, config.isContinuous)
                    .addExtra(keyIsBlowhorn, config.isBlowhorn)
                    .addExtra(keyContinuousInterval, config.scanInterval)
                    .addExtra(keyRefNumber, config.refNumber)
                    .addExtra(keyOrderNumber, config.orderNumber)
                    .addExtra(keyScannedNumber, config.scannedNumber)
                    .initiateScan()
        }
    }

    private val listener = object : PluginRegistry.ActivityResultListener {
        var result: Result? = null
        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
            val array = intent?.getStringArrayListExtra("result") ?: arrayListOf()
            result?.success(array)
            return true
        }

    }

    internal data class Config(val isBeep: Boolean, val isContinuous: Boolean, val isShipment: Boolean, val scanInterval: Int, val refNumber: String, val scannedNumber: String, val orderNumber: String, , val isBlowhorn: Boolean)

}
