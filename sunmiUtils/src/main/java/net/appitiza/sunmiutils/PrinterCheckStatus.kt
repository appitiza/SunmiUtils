package net.appitiza.sunmiutils

enum class PrinterCheckStatus(val value: Int) {
    NoSunmiPrinterCheck(0x00000000), CheckSunmiPrinterCheck(0x00000001), FoundSunmiPrinterCheck(0x00000002), LostSunmiPrinterCheck(0x00000003)
}