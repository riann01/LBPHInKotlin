import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

val gridX = 10
val gridY = 10

fun main() {
  val input = File("yalefaces/subject01.normal").inputStream()
  //val greyImage = toGrayScale(input)
  val buffImg = toBufferedImg(input)
  val raster = buffImg.raster
  //val byteArr = rasterToByte(raster)
  val newImg = lbph(raster, 16)
//  Window("image", newImg)
}

fun toBufferedImg(imgFile: FileInputStream): BufferedImage {
  return ImageIO.read(imgFile)
}

fun toGrayScale(imgFile: FileInputStream): BufferedImage {
  val cs: ColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY)
  val op = ColorConvertOp(cs, null)
  val bufferedImage: BufferedImage = ImageIO.read(imgFile)
  return op.filter(bufferedImage, null)
}

fun toByteArr(image: BufferedImage): ByteArray {
  val outputStream = ByteArrayOutputStream()
  ImageIO.write(image, "gif", outputStream)
  return outputStream.toByteArray()
}

fun printArr(image: ByteArray) {
  val itr = image.iterator()
  while(itr.hasNext()) {
    println(itr.next().toUByte())
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun printBiDimArr(arr: Array<UByteArray>) {
  for (i in arr.indices) {
    println(arr[i])
  }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun getPixelNeighbourhoodClockwise(centerPos: Int, originalImage: ByteArray): Array<String?> {
  val neighbourhood = arrayOfNulls<String>(8)
  var j = 0
  for(i in -4 until neighbourhood.size - 1) {
    if(centerPos != i) {
      neighbourhood[j] = getPixel(i + centerPos, centerPos, originalImage)
      j++
    }
    if(j == 8) break
  }
  return neighbourhood
}

fun getPixel(position: Int, centralPixelPos: Int, image: ByteArray): String {
  return try { if (image[centralPixelPos].toUByte() > image[position].toUByte()) "1" else "0" }
  catch (exp: ArrayIndexOutOfBoundsException) { "0" }
}

fun generateHistogram(lbpImage: ByteArray, rangesDivider: Int): HashMap<String, Int> {
  val ranges = defineRanges(rangesDivider)
  val histograms: HashMap<String, Int> = initializeHistogram(ranges)
  for(pixelPos in lbpImage.indices) {
    for((start, end) in ranges.entries) {
      when(lbpImage[pixelPos]) {
        in start..end -> {
          histograms["$start -> $end"] = histograms["$start -> $end"]!!.plus(1)
        }
      }
    }
  }
  return histograms
}

fun defineRanges(rangesDivider: Int): HashMap<Int, Int> {
  val ranges = HashMap<Int, Int>()
  for(i in rangesDivider until 257 step rangesDivider) {
    val lastStep = i - rangesDivider + if(i == rangesDivider) 0 else 1
    ranges[lastStep] = i
  }
  return ranges
}

fun initializeHistogram(ranges: HashMap<Int, Int>): HashMap<String, Int> {
  val histograms = HashMap<String, Int>()
  ranges.forEach { (key, value) -> histograms["$key -> $value"] = 0 }
  return histograms
}

fun parseStringInt(value: String): Int = Integer.parseInt(value)

@OptIn(ExperimentalUnsignedTypes::class)
fun generateNewImage(imgArr: ByteArray): ByteArray {
  val newImage = ByteArray(imgArr.size)
  for(i in imgArr.indices) {
    val currentNeighbourhood = getPixelNeighbourhoodClockwise(i, imgArr)
    val mergedPixelValue = mergeNeighbourhoodPixels(currentNeighbourhood)
    newImage[i] = Integer.parseInt(mergedPixelValue,2).toByte()
  }
  return newImage
}

@OptIn(ExperimentalUnsignedTypes::class)
fun mergeNeighbourhoodPixels(arrToMerge: Array<String?>): String {
  return arrToMerge[3] + arrToMerge[5] + arrToMerge[6] + arrToMerge[7] + arrToMerge[4] + arrToMerge[2] + arrToMerge[1] + arrToMerge[0]
}

fun rasterToByte(pixelsArr: Raster): ByteArray = (pixelsArr.dataBuffer as DataBufferByte).data

fun byteArrayToBufferedImage(src: ByteArray, width: Int, height: Int): BufferedImage {
  val result = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
  result.raster.setDataElements(0, 0, width, height, src)
  return result
}

@OptIn(ExperimentalUnsignedTypes::class)
fun lbph(raster: Raster, range: Int): BufferedImage {
  val pixels = (raster.dataBuffer as DataBufferByte).data
  val newImage = generateNewImage(pixels)
  val histogram = generateHistogram(newImage, range)
  for((key, value) in histogram.entries)
    println("$key = $value")
  return byteArrayToBufferedImage(newImage, raster.width, raster.height)
}

fun writeImg(fileName: String, bytes: ByteArray) {
  val outputFile = File(fileName)
  //val bis = ByteArrayInputStream(bytes)
  //println(bis.readAllBytes()[0])
  //val bImage2 = ImageIO.read(bis)
  //ImageIO.write(bImage2, "jpg", outputFile)
  Files.write(outputFile.toPath(), bytes)
}

class Window(title: String, image: BufferedImage): JFrame() {
  init {
    createUI(title, image.width, image.height)
    drawImg(image)
    this.isVisible = true
  }

  private fun createUI(title: String, width: Int, height: Int) {
    setTitle(title)
    defaultCloseOperation = EXIT_ON_CLOSE
    setSize(width, height)
    setLocationRelativeTo(null)
  }

  private fun drawImg(image: BufferedImage) {
    val icon = ImageIcon(image)
    val lbl = JLabel()
    lbl.icon = icon
    this.add(lbl)
  }

}