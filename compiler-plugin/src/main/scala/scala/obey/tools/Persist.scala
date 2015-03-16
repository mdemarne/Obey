package scala.obey.tools

import java.io.{BufferedWriter, File, FileWriter, IOException}

object Persist {

  /* Renames the file as .scala.old */
  def archive(path: String): Unit = {
    val oldF = new File(path)
    if(!oldF.exists()) throw new IOException(s"Error: archive's file ${path} does not exists")
    val newF = new File(path+".old")
    oldF.renameTo(newF)
    oldF.delete()
  }

  /* Takes .scala.old and strips the .old */
  def unarchive(path: String): Unit = {
    val oldF = new File(path)
    if(!oldF.exists())
      throw new IOException(s"Error: unarchive's file ${path} does not exists")
    val newF = new File(path.stripSuffix(".old"))
    oldF.renameTo(newF)
  }

  /* Absolute name required */
  def persist(name: String, tree: String): Unit = {
    val f = new File(name)
    if(f.exists()) throw new IOException(s"Error: file to persist ${name} already exists")
    val w = new BufferedWriter(new FileWriter(name))
    w.write(tree)
    // TODO: That's where things should be changed; we want to re-print the tree while keeping other thinfs
    // TODO: such as comments, formatting, etc. This could be achieved using tokens.
    w.close()
  }
}