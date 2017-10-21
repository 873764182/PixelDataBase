package pixel.database.app;

import pixel.database.library.OnDbIdCallback;
import pixel.database.library.TableColumn;

/**
 * Created by pixel on 2017/10/21.
 * <p>
 * 测试数据库存储类型
 */

public class BaseTable implements OnDbIdCallback {

    public long dbId;

    @TableColumn
    public boolean aBool;

//    @TableColumn
//    public boolean aBoolean;  // 测试字段修改 需要增加数据库版本号

    @TableColumn
    public char aChar;

    @TableColumn
    public byte aByte;

    @TableColumn
    public short aShort;

    @TableColumn
    public int aInt;

    @TableColumn
    public long aLong;

    @TableColumn
    public float aFloat;

    @TableColumn
    public double aDouble;

    @TableColumn
    public String aString;

    public BaseTable() {
    }

    public BaseTable(boolean aBoolean, char aChar, byte aByte, short aShort, int aInt, long aLong, float aFloat, double aDouble, String aString) {
        this.aBool = aBoolean;
//        this.aBoolean = aBoolean;
        this.aChar = aChar;
        this.aByte = aByte;
        this.aShort = aShort;
        this.aInt = aInt;
        this.aLong = aLong;
        this.aFloat = aFloat;
        this.aDouble = aDouble;
        this.aString = aString;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public boolean isaBoolean() {
        return aBool;
//        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBool = aBoolean;
//        this.aBoolean = aBoolean;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    public int getaInt() {
        return aInt;
    }

    public void setaInt(int aInt) {
        this.aInt = aInt;
    }

    public long getaLong() {
        return aLong;
    }

    public void setaLong(long aLong) {
        this.aLong = aLong;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }

    @Override
    public void setId(Long _id) {
        this.dbId = _id;
    }

    @Override
    public String toString() {
        return "BaseTable{" +
                "dbId=" + dbId +
                ", aBool=" + aBool +
//                ", aBoolean=" + aBoolean +
                ", aChar=" + aChar +
                ", aByte=" + aByte +
                ", aShort=" + aShort +
                ", aInt=" + aInt +
                ", aLong=" + aLong +
                ", aFloat=" + aFloat +
                ", aDouble=" + aDouble +
                ", aString='" + aString + '\'' +
                '}';
    }

}
