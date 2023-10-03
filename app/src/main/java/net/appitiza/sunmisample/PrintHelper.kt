package net.appitiza.sunmisample

import android.content.Context
import android.os.RemoteException
import android.util.Log
import net.appitiza.lib.sunmiutils.AlignmentType
import net.appitiza.lib.sunmiutils.SunmiPrintHelper

class PrintHelper : SunmiPrintHelper {
    var isPrinterServiceActivated = false
    constructor(context: Context) {
        initPrinterService(context) {
            isPrinterServiceActivated = it
        }
    }


    fun printTest() {
        if (!isPrinterServiceAvailable()) {
            return
        }
        try {
            setFontStyle(
                isBold = false,
                isUnderLine = false,
                fontSize = 30f,
                alignment = AlignmentType.CENTRE_ALIGNED
            )

            printText("Text 1")
            addNewLine()
            printText("Text 2")
            addNewLine()
           printDivider(false)

              setFontStyle(
                 isBold = true,
                 isUnderLine = false,
                 fontSize = 25f,
                 alignment = AlignmentType.LEFT_ALIGNED
             )
             val dataValue = arrayOf("", "")
             val dataValueWidth = intArrayOf(1, 1)
             val dataValueAlign = intArrayOf(0, 2)
             dataValue[0] = "Left"
             dataValue[1] = "Right"
             printColumnsString(
                 dataValue,
                 dataValueWidth,
                 dataValueAlign
             )
            outPaper()
            return

        } catch (e: RemoteException) {
            Log.e("PrintHelper", "printTest: ", e)
            return
        }
    }
    fun getPrinterStatus(): Boolean {
        return isPrinterAvailable()
    }
    fun deInitialize(context: Context) {
        deInitPrinterService(context)
    }
}