import java.io.File
import java.awt.{Graphics, Toolkit}
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.Transferable
import java.awt.image.{BufferedImage, ImageObserver}

import javax.imageio.ImageIO
import java.util.UUID

import sun.awt.image.MultiResolutionCachedImage

object Clip {

  def genFileName(): String = {
    UUID.randomUUID.toString // + ".png"
  }

  def getClipboard(): Clipboard = {
    Toolkit.getDefaultToolkit().getSystemClipboard()
  }

  def showClipboardMimeTypes(cpl: Clipboard) = {
    cpl.getAvailableDataFlavors().map(_.getPrimaryType()).toSet
  }

  def getContent(cpl: Clipboard): Option[Transferable] = {
    Option(cpl.getContents(null))
  }

  /// Try to get a image from clipboard
  def getImage(cpl: Clipboard): Option[BufferedImage] = {

    val r = getContent(cpl)
      .filter(_.isDataFlavorSupported(DataFlavor.imageFlavor))
      .map(_.getTransferData(DataFlavor.imageFlavor).asInstanceOf[MultiResolutionCachedImage])
      .map(image => {
        val r: BufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB)
        val bg: Graphics = r.createGraphics()
        bg.drawImage(image, 0, 0, null)
        bg.dispose()
        r
      })
    r
  }

  /// Save image from clipboard to a file
  def saveClipboardImage(directory: String, imageName: String) {
    val imgOpt = getImage(getClipboard())

    imgOpt match {
      case None => println("Error: No image available in clipboard.")
      case Some(img) => val file = new File(directory, imageName + ".png")
        ImageIO.write(img, "png", file)
        println(file.toString)
    }
  }

  /// Save image from clipboard to a file with extension png
  def saveClipboardImageUUID(directory: String) {
    // val imageName = (new File(directory,)).toString
    //println("File name = " + filename)      
    saveClipboardImage(directory, genFileName())
  }

  def printHelp() {
    println(
      """Usage:

  Save clipboard image with a given Name

    $ javar -jar Clip.jar --name imageName
      -> save image to ./imageName.png and print ./imageName.png

    $ java -jar Clip.jar --name imageName  /tmp
      -> save image to /tmp/imageName.png and print /tmp/imageName.png

  Save clipboard image with an automatic generated name
    - UUID - Universal Unique Identifier

    $ java -jar Clip.jar --uuid /tmp
      -> save image to /tmp/415dafcf-5fd0-4d10-97f2-0da82bf1bf3f.png and print
         - 415dafcf-5fd0-4d10-97f2-0da82bf1bf3f.png
""")

  }


  def main(args: Array[String]): Unit = {
    args.toList match {
      case List("--uuid", directory) => saveClipboardImageUUID(directory)
      case List("--name", name) => saveClipboardImage(".", name)
      case List("--name", name, directory) => saveClipboardImage(directory, name)
      case List() => printHelp()
      case _ => println("Failed")

    }
  } // End of main ()

}
