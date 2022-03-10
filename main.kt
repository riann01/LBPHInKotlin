import java.io.File
import java.io.FileInputStream
import java.io.ByteArrayOutputStream
import java.awt.color.ColorSpace
import java.awt.image.ColorConvertOp
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun main() {
  val input = File("yalefaces/subject01.gif").inputStream()
  val greyImage = toGrayScale(input)
  val byteArr = toByteArr(greyImage)
  printArr(byteArr)
}

fun toGrayScale(imgFile: FileInputStream): BufferedImage {
  //val cs: ColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY)  
  //val op: ColorConvertOp = ColorConvertOp(cs, null)
  val bufferedImage: BufferedImage = ImageIO.read(imgFile)
  //return op.filter(bufferedImage, null)
  return bufferedImage
}

fun toByteArr(image: BufferedImage): ByteArray {
  val outputStream = ByteArrayOutputStream();
  ImageIO.write(image, "jpeg", outputStream);
  return outputStream.toByteArray();
}

fun printArr(image: ByteArray) {
  val itr = image.iterator()
  while(itr.hasNext()) {
    println(itr.next().toUByte())
  }
}

fun getWidthAndHeight(image: BufferedImage) {

}

fun getPixelNeighbourhoodClockwise(centerPos: Int, originalImage: ByteArray): ByteArray {

  var neighbourhood: ByteArray

  return neighbourhood

}

fun lbph(image: BufferedImage) {

}

fun writeImg(fileName: String, image: BufferedImage) {
  val outputfile = File(fileName);
  ImageIO.write(image, "gif", outputfile);
}