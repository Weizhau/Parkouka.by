package weizhau.parkoukaby

class TimeUtility(val time: String) {
    fun sumTime(plusHours: Int): String {
        return (time
            .substringBefore(':')
            .padStart(2, '0')
            .toInt() + plusHours)
            .toString() + ':' + time.substringAfter(':')
    }

    fun findTimeBetweenInMillis(till: String): Long {
        val timeInMillis = stringTimeToMillis(time)
        val tillInMillis = stringTimeToMillis(till)

        return tillInMillis - timeInMillis
    }

    private fun stringTimeToMillis(time: String): Long {
        var millis = 0L
        //hours to millis
        millis += time.substringBefore(':')
            .padStart(2, '0')
            .toInt() * 3_600_000
        //minutes to millis
        millis += time.substringAfter(':')
            .substringBefore(':')
            .padStart(2, '0')
            .toInt() * 60_000
        //seconds to millis
        millis += time
            .substringAfterLast(':')
            .padStart(2, '0')
            .toInt() * 1000

        return millis
    }

    fun clipToIfBigger(clipTo: String): String {
        if (stringTimeToMillis(time) > stringTimeToMillis(clipTo)) {
           return clipTo
        } else {
            return time
        }
    }
}