import org.junit.Test
import org.junit.Assert._

/**
 * Usage:
 * mvn test
 *
 * You should pass all the provided test cases before you make any submission.
 *
 * Feel free to add more test cases.
 */
class DataFilterTest {

    @Test
    def mapPercentDecode() {
        assertTrue(Array("en", "Carnegie_Mellon_University", "34", "0").sameElements(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))._2))
        assertTrue(Array("en", "User:K6ka", "34", "0").sameElements(
          DataFilter.mapPercentDecode((9, "en User%3AK6ka 34 0"))._2))
    }

    @Test
    def checkDataLength() {
        assertTrue(DataFilter.filterLength(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
        assertFalse(DataFilter.filterLength(
          DataFilter.mapPercentDecode((9, "en 34 0"))))
        assertFalse(DataFilter.filterLength(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 34 0"))))
        assertFalse(DataFilter.filterLength(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University%2034 34 0"))))
    }

    @Test
    def checkDomain() {
        assertTrue(DataFilter.filterDomain(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
        assertTrue(DataFilter.filterDomain(
          DataFilter.mapPercentDecode((9, "en.m Carnegie_Mellon_University 34 0"))))
        assertFalse(DataFilter.filterDomain(
          DataFilter.mapPercentDecode((9, "fr Carnegie_Mellon_University 34 0"))))
    }

    @Test
    def checkSpecialPage() {
        assertTrue(DataFilter.filterSpecialPage(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
        assertFalse(DataFilter.filterSpecialPage(
          DataFilter.mapPercentDecode((9, "en Main_Page 34 0"))))
        assertFalse(DataFilter.filterSpecialPage(
          DataFilter.mapPercentDecode((9, "en - 34 0"))))
        assertFalse(DataFilter.filterSpecialPage(
          DataFilter.mapPercentDecode((9, "en %2D 34 0"))))
    }

    @Test
    def filterPrefix() {
        assertTrue(DataFilter.filterPrefix(
          DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
        assertFalse(DataFilter.filterPrefix(
          DataFilter.mapPercentDecode((9, "en User:K6ka 34 0"))))
        assertFalse(DataFilter.filterPrefix(
          DataFilter.mapPercentDecode((9, "en User%3AK6ka 34 0"))))
        assertFalse(DataFilter.filterPrefix(
          DataFilter.mapPercentDecode((9, "en User%3aK6ka 34 0"))))
    }

    @Test
    def checkSuffix() {
      assertTrue(DataFilter.filterSuffix(
        DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
      assertTrue(DataFilter.filterSuffix(
        DataFilter.mapPercentDecode((9, "en cmu.edu 34 0"))))
      assertFalse(DataFilter.filterSuffix(
        DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University.png 34 0"))))
      assertFalse(DataFilter.filterSuffix(
        DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University.png 34 0"))))
    }

    @Test
    def checkFirstLetter() {
      assertTrue(DataFilter.filterFirstLetter(
        DataFilter.mapPercentDecode((9, "en Carnegie_Mellon_University 34 0"))))
      assertFalse(DataFilter.filterFirstLetter(
        DataFilter.mapPercentDecode((9, "en carnegie_Mellon_University 34 0"))))
    }
}
