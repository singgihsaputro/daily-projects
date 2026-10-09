package com.dailyprojects.idformat

enum class Gender { MALE, FEMALE }

/** A calendar date; kept dependency-free because `java.time` needs API 26 and this library supports 24. */
data class BirthDate(val year: Int, val month: Int, val day: Int) {
    override fun toString() = "%04d-%02d-%02d".format(year, month, day)
}

/** What a valid NIK (Nomor Induk Kependudukan) encodes. */
data class NikInfo(
    val provinceCode: String,
    val provinceName: String,
    /** Regency/city code, 4 digits including the province. */
    val regencyCode: String,
    /** District code, 6 digits including province and regency. */
    val districtCode: String,
    val birthDate: BirthDate,
    val gender: Gender,
    val sequence: String
)

/** Structural validation of the 16-digit Indonesian national ID number. */
object Nik {

    /**
     * Decodes [raw], or returns null when it is not structurally valid: 16 digits, a real province
     * code, and a real calendar date. Women's birth day is stored as day + 40.
     *
     * Two-digit birth years are resolved against [currentYear]: a year that would lie in the future
     * is read as 19xx.
     */
    fun parse(raw: String, currentYear: Int = 2026): NikInfo? {
        val nik = raw.filterNot { it == ' ' || it == '-' }
        if (nik.length != 16 || !nik.all { it in '0'..'9' }) return null

        val province = nik.take(2)
        val provinceName = PROVINCES[province] ?: return null

        val rawDay = nik.substring(6, 8).toInt()
        val gender = if (rawDay > 40) Gender.FEMALE else Gender.MALE
        val day = if (rawDay > 40) rawDay - 40 else rawDay
        val month = nik.substring(8, 10).toInt()
        val yy = nik.substring(10, 12).toInt()
        val year = if (2000 + yy <= currentYear) 2000 + yy else 1900 + yy

        if (month !in 1..12 || day !in 1..daysIn(year, month)) return null
        if (nik.takeLast(4) == "0000") return null

        return NikInfo(
            provinceCode = province,
            provinceName = provinceName,
            regencyCode = nik.take(4),
            districtCode = nik.take(6),
            birthDate = BirthDate(year, month, day),
            gender = gender,
            sequence = nik.takeLast(4)
        )
    }

    fun isValid(raw: String, currentYear: Int = 2026): Boolean = parse(raw, currentYear) != null

    /** Hides the personal middle of a NIK for logs and receipts: `3171••••••••0001`. */
    fun mask(raw: String): String {
        val nik = raw.filterNot { it == ' ' || it == '-' }
        if (nik.length != 16) return "•".repeat(nik.length)
        return nik.take(4) + "•".repeat(8) + nik.takeLast(4)
    }

    private fun daysIn(year: Int, month: Int): Int = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private val PROVINCES = mapOf(
        "11" to "Aceh", "12" to "Sumatera Utara", "13" to "Sumatera Barat", "14" to "Riau",
        "15" to "Jambi", "16" to "Sumatera Selatan", "17" to "Bengkulu", "18" to "Lampung",
        "19" to "Kepulauan Bangka Belitung", "21" to "Kepulauan Riau", "31" to "DKI Jakarta",
        "32" to "Jawa Barat", "33" to "Jawa Tengah", "34" to "DI Yogyakarta", "35" to "Jawa Timur",
        "36" to "Banten", "51" to "Bali", "52" to "Nusa Tenggara Barat", "53" to "Nusa Tenggara Timur",
        "61" to "Kalimantan Barat", "62" to "Kalimantan Tengah", "63" to "Kalimantan Selatan",
        "64" to "Kalimantan Timur", "65" to "Kalimantan Utara", "71" to "Sulawesi Utara",
        "72" to "Sulawesi Tengah", "73" to "Sulawesi Selatan", "74" to "Sulawesi Tenggara",
        "75" to "Gorontalo", "76" to "Sulawesi Barat", "81" to "Maluku", "82" to "Maluku Utara",
        "91" to "Papua", "92" to "Papua Barat", "93" to "Papua Selatan", "94" to "Papua Tengah",
        "95" to "Papua Pegunungan", "96" to "Papua Barat Daya"
    )
}
