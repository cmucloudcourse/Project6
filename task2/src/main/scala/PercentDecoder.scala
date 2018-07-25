//import java.nio.charset.StandardCharsets
//
//import java.util.Arrays
//
//object PercentDecoder {
//
//  /**
//    * Hex value benchmark.
//    */
//  private val HEX_0: Char = 0x30
//
//  private val HEX_UPPER_A: Char = 0x37
//
//  private val HEX_LOWER_A: Char = 0x57
//
//  /**
//    * The offset of the first hex value of
//    * percent encoded UTF-8 characters.
//    */
//  private val UTF_8_OFFSET: Int = 4
//
//  /**
//    * Return hex value given a byte.
//    *
//    * @param b to convert
//    * @return hex value, -1 if if invalid
//    */
//  private def getHexValue(b: Byte): Int = {
//    if ('0' <= b && b <= '9') {
//      b - HEX_0
//    } else if ('A' <= b && b <= 'F') {
//      b - HEX_UPPER_A
//    } else if ('a' <= b && b <= 'f') {
//      b - HEX_LOWER_A
//    } else {
//      -1
//    }
//  }
//
//  /**
//    * Decodes percent encoded strings.
//    *
//    * @param encoded the percent encoded string
//    * @return the decoded string
//    */
//  def decode(encoded: String): String = {
//    if (encoded == null) {
//      return null
//    }
//    val encodedChars: Array[Byte] = encoded.getBytes(StandardCharsets.UTF_8)
//    val encodedLength: Int = encodedChars.length
//    val decodedChars: Array[Byte] = Array.ofDim[Byte](encodedLength)
//
//    var decodedIdx: Int = 0
//    var encodedIdx: Int = 0
//
//    while (encodedIdx < encodedLength) {
//      decodedChars(decodedIdx) = encodedChars(encodedIdx)
//      if (decodedChars(decodedIdx) == '%') {
//        if (encodedIdx + 2 < encodedLength) {
//          val value1: Int = getHexValue(encodedChars(encodedIdx + 1))
//          val value2: Int = getHexValue(encodedChars(encodedIdx + 2))
//          if (value1 >= 0 && value2 >= 0) {
//            decodedChars(decodedIdx) =
//              ((value1 << UTF_8_OFFSET) + value2).toByte
//            encodedIdx += 2
//          }
//        }
//      }
//      encodedIdx += 1
//      decodedIdx += 1
//    }
//    new String(Arrays.copyOf(decodedChars, decodedIdx), StandardCharsets.UTF_8)
//  }
//
//}