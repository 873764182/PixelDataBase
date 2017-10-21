

SQLiteDatabase db = mHelper.getWritableDatabase();
try
{
//      ByteArrayOutputStream bos = new ByteArrayOutputStream();
//      ObjectOutputStream oos = new ObjectOutputStream(bos);
//      oos.write(command);
//      byte[] buff = bos.toByteArray();
    // ByteArrayInputStream bis = new
    // ByteArrayInputStream(buff);
    String sql = "INSERT INTO " + DBHelper.TABLE_NAME
            + " VALUES(?,?);";
    SQLiteStatement ss = db.compileStatement(sql);
    ss.bindBlob(1, command);
    ss.bindString(2, "N");
    ss.execute();
}
finally
{
    db.close();
}


public void insertSeedItem(long ToyID, byte[]ToySeed) {
    String sqlstr = "insert into " + TABLE_SEED + " (ToyID, ToySeed,ToyMemo) values (?,?,?);";
    Object[] args = new Object[]{ToyID,ToySeed,null};
        try{
        mTestDatabase.execSQL(sqlstr,args);
        } catch (SQLException ex) {
        }
    Log.i("testSeedDB", "insertSeedItem");

    }

    public byte[] GetSeedItem(long ToyID) {
    Cursor cur;
        byte[] strSeed = null;

        String col[] = {"ToyID", "ToySeed" ,"ToyMemo"};
        String strToy = "ToyID=" +  new Integer((int) ToyID).toString();
        try{
        cur = mTestDatabase.query(TABLE_SEED, col, strToy, null, null, null, null);
            cur.moveToFirst();
            strSeed = cur.getBlob(1);
        } catch (SQLException ex) {
        }
        if (cur !=null) cur.close;
        Log.i("testSeedDB", strToy);
        return strSeed;
    }