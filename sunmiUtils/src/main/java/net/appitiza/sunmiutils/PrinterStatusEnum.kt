package net.appitiza.sunmiutils

enum class PrinterStatusEnum(val code :Int) {
    RUNNING(1),
    STILL_INITIALIZING(2),
    HARDWARE_ABNORMAL(3),
    OUT_OF_PAPER(4),
    OVER_HEATING(5),
    COVER_NOT_CLOSED(6),
    CUTTER_ABNORMAL(7),
    CUTTER_NORMAL(8),
    BLACK_MARK_PAPER_NOT_FOUND(9),
    NOT_AVAILABLE(505),
    UNKNOWN(0),

}