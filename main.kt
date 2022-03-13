import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

fun main() {
  val input = File("yalefaces/subject01.gif").inputStream()
  //val greyImage = toGrayScale(input)
  val buffImg = toBufferedImg(input)
  val raster = buffImg.raster

  val byteArr = rasterToByte(raster)

  val newImg = lbph(raster)

  Window("image", newImg)
}

fun toBufferedImg(imgFile: FileInputStream): BufferedImage {
  return ImageIO.read(imgFile)
}

fun toGrayScale(imgFile: FileInputStream): BufferedImage {
  val cs: ColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY)
  val op: ColorConvertOp = ColorConvertOp(cs, null)
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
fun getPixelNeighbourhoodClockwise(centerPos: Int, originalImage: ByteArray): UByteArray {
  val neighbourhood = UByteArray(9)
  var j = 0
  for(i in -4 until neighbourhood.size - 1) {
    neighbourhood[j] = getPixel(i + centerPos, centerPos, originalImage)
    j++
    if(j == 9) break
  }
  return neighbourhood
}

fun getPixel(position: Int, centralPixelPos: Int, image: ByteArray): UByte {
  return try { if (image[centralPixelPos].toUByte() > image[position].toUByte()) (0x01).toUByte() else (0x00).toUByte() }
  catch (exp: ArrayIndexOutOfBoundsException) { (0x00).toUByte() }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun createMeanFace(imgArr: ByteArray): ByteArray {
  val newImage = ByteArray(imgArr.size)
  for(i in imgArr.indices) {
    val currentNeighbourhood = getPixelNeighbourhoodClockwise(i, imgArr)
    val mergedPixelValue = mergeNeighbourhoodPixels(currentNeighbourhood)
    newImage[i] = mergedPixelValue.toByte()
  }
  return newImage
}

@OptIn(ExperimentalUnsignedTypes::class)
fun mergeNeighbourhoodPixels(arrToMerge: UByteArray) = arrToMerge.reduce { acc, uByte -> uByte.plus(acc).toUByte() }

fun byteArrayToBufferedImage(input: ByteArray): BufferedImage = ImageIO.read(ByteArrayInputStream(input))

fun rasterToByte(pixelsArr: Raster): ByteArray? = (pixelsArr.dataBuffer as DataBufferByte).data

fun byteArrayToBufferedImage(src: ByteArray?, width: Int, height: Int): BufferedImage {
  val result = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
  result.raster.setDataElements(0, 0, width, height, src)
  return result
}

@OptIn(ExperimentalUnsignedTypes::class)
fun lbph(raster: Raster): BufferedImage {
  val pixels = (raster.dataBuffer as DataBufferByte).data
  val newImage = createMeanFace(pixels)
  //writeImg("out.gif", newImage)
  return byteArrayToBufferedImage(newImage, raster.width, raster.height)
}

fun writeImg(fileName: String, bytes: ByteArray) {
  val outputFile = File(fileName);
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

    defaultCloseOperation = JFrame.EXIT_ON_CLOSE
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