package com.fs.pxe.daohib.hobj;

/**
 * Used to store large data sets into a single table. When an HObject
 * instance needs to store as part of its state large binary or text
 * data, a reference to an instance of this class must be created.
 * @hibernate.class table="LARGE_DATA"
 */
public class HLargeData extends HObject {

  private byte[] binary = null;

  public HLargeData() {
    super();
  }

  public HLargeData(byte[] binary) {
    super();
    this.binary = binary;
  }

  public HLargeData(String text) {
    super();
    this.binary = text.getBytes();
  }

  /**
   * @hibernate.property 
   *    type="binary"
   *     length="2G"
   * 
   * @hibernate.column 
   *    name="BIN_DATA"
   *    sql-type="blob(2G)"
   */
  public byte[] getBinary() {
    return binary;
  }

  public void setBinary(byte[] binary) {
    this.binary = binary;
  }

  public String getText() {
    return new String(binary);
  }
}
