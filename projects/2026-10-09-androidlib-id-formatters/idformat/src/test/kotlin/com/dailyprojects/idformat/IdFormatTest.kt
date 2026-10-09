package com.dailyprojects.idformat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdFormatTest {

    @Test fun rupiahGroupsThousands() {
        assertEquals("Rp 0", Rupiah.format(0))
        assertEquals("Rp 999", Rupiah.format(999))
        assertEquals("Rp 1.250.000", Rupiah.format(1_250_000))
        assertEquals("-Rp 15.000", Rupiah.format(-15_000))
        assertEquals("1.000", Rupiah.format(1_000, withSymbol = false))
    }

    @Test fun rupiahCompact() {
        assertEquals("Rp 500", Rupiah.formatCompact(500))
        assertEquals("Rp 12 rb", Rupiah.formatCompact(12_000))
        assertEquals("Rp 1,5 jt", Rupiah.formatCompact(1_500_000))
        assertEquals("Rp 2,3 M", Rupiah.formatCompact(2_350_000_000))
        assertEquals("-Rp 3 T", Rupiah.formatCompact(-3_000_000_000_000))
    }

    @Test fun rupiahParseRoundTrips() {
        for (v in listOf(0L, 7L, 1_000L, 1_250_000L, -45_000L)) assertEquals(v, Rupiah.parse(Rupiah.format(v)))
        assertEquals(1_250_000L, Rupiah.parse("1.250.000,00"))
        assertEquals(-1_250_000L, Rupiah.parse("-Rp1.250.000"))
        assertEquals(50_000L, Rupiah.parse("50000"))
    }

    @Test fun rupiahParseRejectsMalformed() {
        assertNull(Rupiah.parse(""))
        assertNull(Rupiah.parse("Rp 1.25.000"))
        assertNull(Rupiah.parse("1.250.000,50"))
        assertNull(Rupiah.parse("abc"))
    }

    @Test fun phoneNormalisesEveryWriting() {
        val expected = "+6281234567890"
        for (raw in listOf("081234567890", "6281234567890", "+62 812-3456-7890", "81234567890", "(0812) 3456 7890")) {
            assertEquals(raw, expected, PhoneNumber.parse(raw)?.e164)
        }
    }

    @Test fun phoneDisplayAndOperator() {
        val p = PhoneNumber.parse("+6281234567890")!!
        assertEquals("0812-3456-7890", p.display)
        assertEquals("Telkomsel", p.operator)
        assertEquals("Tri", PhoneNumber.parse("0896 1234 5678")?.operator)
        assertNull(PhoneNumber.parse("0890 1234 5678")?.operator)
    }

    @Test fun phoneRejectsNonMobile() {
        assertNull(PhoneNumber.parse("0215551234"))
        assertNull(PhoneNumber.parse("+14155550123"))
        assertNull(PhoneNumber.parse("0812"))
        assertNull(PhoneNumber.parse("08123abc789"))
    }

    @Test fun nikDecodesMaleAndFemale() {
        val m = Nik.parse("3171011505900001")!!
        assertEquals("DKI Jakarta", m.provinceName)
        assertEquals("317101", m.districtCode)
        assertEquals(BirthDate(1990, 5, 15), m.birthDate)
        assertEquals(Gender.MALE, m.gender)

        val f = Nik.parse("3578015506020002")!!
        assertEquals(BirthDate(2002, 6, 15), f.birthDate)
        assertEquals(Gender.FEMALE, f.gender)
        assertEquals("Jawa Timur", f.provinceName)
    }

    @Test fun nikRejectsInvalid() {
        assertFalse(Nik.isValid("317101150590000"))
        assertFalse(Nik.isValid("9971011505900001"))
        assertFalse(Nik.isValid("3171013002900001"))
        assertFalse(Nik.isValid("3171011513900001"))
        assertFalse(Nik.isValid("3171011505900000"))
        assertFalse(Nik.isValid("31710115059000AB"))
    }

    @Test fun nikLeapDayAndMask() {
        assertTrue(Nik.isValid("3171012902960003"))
        assertFalse(Nik.isValid("3171012902970003"))
        assertEquals("3171••••••••0001", Nik.mask("3171 0115 0590 0001"))
    }
}
