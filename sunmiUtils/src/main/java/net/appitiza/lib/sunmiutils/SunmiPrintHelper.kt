package net.appitiza.lib.sunmiutils
import android.content.Context
import android.graphics.BitmapFactory
import android.os.RemoteException
import android.util.Log
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import com.sunmi.peripheral.printer.WoyouConsts

open class SunmiPrintHelper {

    var mPrinter = PrinterCheckStatus.CheckSunmiPrinterCheck.value
    var mPrinterService: SunmiPrinterService? = null

    fun initPrinter() {
        if (mPrinterService == null) {
            return
        }
        try {
            mPrinterService?.printerInit(null)
        } catch (e: RemoteException) {
            Log.e("SunmiPrintHelper", "showPrinterStatus", e)
        }
    }

    fun initPrinterService(context: Context?,callback:(Boolean)->Unit) {
        try {
            val ret = InnerPrinterManager.getInstance().bindService(
                context,
                object : InnerPrinterCallback(){
                    override fun onConnected(service: SunmiPrinterService) {
                        mPrinterService = service
                        checkPrinterService(service)
                        callback(true)
                    }

                    override fun onDisconnected() {
                        mPrinterService = null
                        mPrinter = PrinterCheckStatus.LostSunmiPrinterCheck.value
                        callback(false)
                    }
                }
            )
            if (!ret) {
                mPrinter = PrinterCheckStatus.NoSunmiPrinterCheck.value
            }
        } catch (e: InnerPrinterException) {
            Log.e("SunmiPrintHelper", "InnerPrinterException", e)
        }
    }

    fun deInitPrinterService(context: Context?) {
        try {
            if (mPrinterService != null) {
                InnerPrinterManager.getInstance().unBindService(context, object : InnerPrinterCallback(){
                    override fun onConnected(service: SunmiPrinterService) {
                        mPrinterService = service
                        checkPrinterService(service)
                    }

                    override fun onDisconnected() {
                        mPrinterService = null
                        mPrinter = PrinterCheckStatus.LostSunmiPrinterCheck.value
                    }
                })
                mPrinterService = null
                mPrinter = PrinterCheckStatus.LostSunmiPrinterCheck.value
            }
        } catch (e: InnerPrinterException) {
            Log.e("SunmiPrintHelper", "showPrinterStatus", e)
        }
    }

     fun checkPrinterService(service: SunmiPrinterService) {
        var ret = false
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service)
        } catch (e: InnerPrinterException) {
            Log.e("SunmiPrintHelper", "showPrinterStatus", e)
        }
        mPrinter = if (ret) PrinterCheckStatus.FoundSunmiPrinterCheck.value else PrinterCheckStatus.NoSunmiPrinterCheck.value
    }

     fun setFontStyle(
        isBold: Boolean,
        isUnderLine: Boolean,
        fontSize: Float,
        alignment: AlignmentType
    ) {
        mPrinterService?.setFontSize(fontSize, null)
        when (isUnderLine) {
            true -> {
                mPrinterService?.setPrinterStyle(
                    WoyouConsts.ENABLE_UNDERLINE,
                    WoyouConsts.ENABLE
                )
            }
            false -> {
                mPrinterService?.setPrinterStyle(
                    WoyouConsts.ENABLE_UNDERLINE,
                    WoyouConsts.DISABLE
                )
            }
        }
        try {
            when (isBold) {
                true -> {
                    mPrinterService?.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.ENABLE)
                }
                false -> {
                    mPrinterService?.setPrinterStyle(WoyouConsts.ENABLE_BOLD, WoyouConsts.DISABLE)
                }
            }
        }
        catch (e: RemoteException) {
            mPrinterService?.sendRAWData(ESCUtil.boldOff(), null)
        }
        mPrinterService?.setAlignment(alignment.value, null)
    }

     fun printDivider(isDark: Boolean) {
        val fontSize = if (isDark) {
            36f
        } else {
            20f
        }
        val type = mPrinterService?.printerModal
        val text = if (isDark) {
            when (type) {
                DeviceTypeEnum.EIGHTY.code -> {
                    "_______________________________\n"
                }
                else -> {
                    "_____________________\n"
                }
            }

        } else {
            when (type) {
                DeviceTypeEnum.EIGHTY.code -> {
                    "*******************************************************\n"
                }
                else -> {
                    "**************************************\n"
                }
            }

        }
        setFontStyle(
            isBold = isDark,
            isUnderLine = false,
            fontSize = fontSize,
            alignment = AlignmentType.CENTRE_ALIGNED
        )

        mPrinterService?.printText(text, null)
    }

     fun addNewLine(number: Int = 1) {
        mPrinterService?.lineWrap(number, null)
    }

     fun moveToNextLine() {
        mPrinterService?.printText(
            "\n",
            null
        )
    }
     fun printLogo(context: Context,logo:Int) {
        val bitmap = BitmapFactory.decodeResource(context.resources,logo)
        mPrinterService?.printBitmap(bitmap, null)
    }
    fun showPrinterStatus() {
        if (mPrinterService == null) {
            return
        }
        var result = "Interface is too low to implement interface"
        try {
            result = when (mPrinterService?.updatePrinterState()) {
                1 -> "printer is running"
                2 -> "printer found but still initializing"
                3 -> "printer hardware interface is abnormal and needs to be reprinted"
                4 -> "printer is out of paper"
                5 -> "printer is overheating"
                6 -> "printer's cover is not closed"
                7 -> "printer's cutter is abnormal"
                8 -> "printer's cutter is normal"
                9 -> "not found black mark paper"
                505 -> "printer does not exist"
                else -> {
                    "printer Else"
                }
            }
        } catch (e: RemoteException) {
            Log.e("SunmiPrintHelper", "showPrinterStatus", e)
        }
    }

    fun printerStatus(): PrinterStatusEnum {
        if (mPrinterService == null) {
            return PrinterStatusEnum.NOT_AVAILABLE
        }
        return when (mPrinterService?.updatePrinterState()) {
            1 -> PrinterStatusEnum.RUNNING
            2 -> PrinterStatusEnum.STILL_INITIALIZING
            3 -> PrinterStatusEnum.HARDWARE_ABNORMAL
            4 -> PrinterStatusEnum.OUT_OF_PAPER
            5 -> PrinterStatusEnum.OVER_HEATING
            6 -> PrinterStatusEnum.COVER_NOT_CLOSED
            7 -> PrinterStatusEnum.CUTTER_ABNORMAL
            8 -> PrinterStatusEnum.CUTTER_NORMAL
            9 -> PrinterStatusEnum.BLACK_MARK_PAPER_NOT_FOUND
            505 -> PrinterStatusEnum.NOT_AVAILABLE
            else -> {
                PrinterStatusEnum.UNKNOWN
            }
        }
    }
    fun outPaper(){
        mPrinterService?.autoOutPaper(null)
    }
    fun setAlignment(alignment: Int){
        mPrinterService?.setAlignment(alignment, null)
    }
    fun printColumnsString(title:Array<String>,width:IntArray,align:IntArray){
        mPrinterService?.printColumnsString(
            title,
            width,
            align,
            null
        )
    }
    fun printText(text:String){
        mPrinterService?.printText(text, null)
    }
    fun printQRCode(text:String, modulesize:Int,  errorlevel:Int){
        mPrinterService?.printQRCode(text, modulesize, errorlevel, null)
    }
    fun isPrinterServiceAvailable():Boolean{
        return mPrinterService != null
    }
    fun isPrinterAvailable():Boolean{
        return when (mPrinter) {
            PrinterCheckStatus.NoSunmiPrinterCheck.value -> {
                false
            }

            PrinterCheckStatus.CheckSunmiPrinterCheck.value -> {
                false
            }

            PrinterCheckStatus.FoundSunmiPrinterCheck.value -> {
                true
            }

            else -> {
                true
            }
        }
    }
}