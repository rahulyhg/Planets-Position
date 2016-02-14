package planets.position.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SolarEclipseTable {

    public static final String TABLE_SOLAR_ECLIPSE = "solarEclipse";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LOCAL_TYPE = "localType";
    public static final String COLUMN_GLOBAL_TYPE = "globalType";
    public static final String COLUMN_LOCAL = "local";
    public static final String COLUMN_LOCAL_MAX = "localMax";
    public static final String COLUMN_LOCAL_FIRST = "localFirst";
    public static final String COLUMN_LOCAL_SECOND = "localSecond";
    public static final String COLUMN_LOCAL_THIRD = "localThird";
    public static final String COLUMN_LOCAL_FOURTH = "localFourth";
    public static final String COLUMN_SUNRISE = "sunRise";
    public static final String COLUMN_SUNSET = "sunSet";
    public static final String COLUMN_RATIO = "ratio";
    public static final String COLUMN_FRACTION_COVERED = "fractionCovered";
    public static final String COLUMN_SUN_AZ = "sunAz";
    public static final String COLUMN_SUN_ALT = "sunAlt";
    public static final String COLUMN_LOCAL_MAG = "localMag";
    public static final String COLUMN_SAROS_NUM = "sarosNum";
    public static final String COLUMN_SAROS_MEMBER_NUM = "sarosMemNum";
    public static final String COLUMN_MOON_AZ = "moonAz";
    public static final String COLUMN_MOON_ALT = "moonAlt";
    public static final String COLUMN_GLOBAL_MAX = "globalMax";
    public static final String COLUMN_GLOBAL_BEGIN = "globalBegin";
    public static final String COLUMN_GLOBAL_END = "globalEnd";
    public static final String COLUMN_GLOBAL_TOTAL_BEGIN = "globalTotalBegin";
    public static final String COLUMN_GLOBAL_TOTAL_END = "globalTotalEnd";
    public static final String COLUMN_GLOBAL_CENTER_BEGIN = "globalCenterBegin";
    public static final String COLUMN_GLOBAL_CENTER_END = "globalCenterEnd";
    public static final String COLUMN_ECLIPSE_DATE = "eclipseDate";
    public static final String COLUMN_ECLIPSE_TYPE = "eclipseType";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SOLAR_ECLIPSE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LOCAL_TYPE
            + " integer, " + COLUMN_GLOBAL_TYPE + " integer," + COLUMN_LOCAL
            + " integer, " + COLUMN_LOCAL_MAX + " real, " + COLUMN_LOCAL_FIRST
            + " real, " + COLUMN_LOCAL_SECOND + " real, " + COLUMN_LOCAL_THIRD
            + " real, " + COLUMN_LOCAL_FOURTH + " real, " + COLUMN_SUNRISE
            + " real, " + COLUMN_SUNSET + " real, " + COLUMN_RATIO
            + " real, " + COLUMN_FRACTION_COVERED + " real, " + COLUMN_SUN_AZ
            + " real, " + COLUMN_SUN_ALT + " real, " + COLUMN_LOCAL_MAG
            + " real, " + COLUMN_SAROS_NUM + " integer, "
            + COLUMN_SAROS_MEMBER_NUM + " integer, " + COLUMN_MOON_AZ
            + " real, " + COLUMN_MOON_ALT + " real, " + COLUMN_GLOBAL_MAX
            + " real, " + COLUMN_GLOBAL_BEGIN + " real, " + COLUMN_GLOBAL_END
            + " real, " + COLUMN_GLOBAL_TOTAL_BEGIN + " real, "
            + COLUMN_GLOBAL_TOTAL_END + " real, " + COLUMN_GLOBAL_CENTER_BEGIN
            + " real, " + COLUMN_GLOBAL_CENTER_END + " real, "
            + COLUMN_ECLIPSE_DATE + " real, " + COLUMN_ECLIPSE_TYPE + " text);";

    public static void onCreate(SQLiteDatabase database) {
        String ip1, ip2;
        database.execSQL(DATABASE_CREATE);
        ip1 = "insert into " + TABLE_SOLAR_ECLIPSE + "(" + COLUMN_ID + ","
                + COLUMN_LOCAL_TYPE + "," + COLUMN_GLOBAL_TYPE + ","
                + COLUMN_LOCAL + "," + COLUMN_LOCAL_MAX + ","
                + COLUMN_LOCAL_FIRST + "," + COLUMN_LOCAL_SECOND + ","
                + COLUMN_LOCAL_THIRD + "," + COLUMN_LOCAL_FOURTH + ","
                + COLUMN_SUNRISE + "," + COLUMN_SUNSET + ","
                + COLUMN_RATIO + "," + COLUMN_FRACTION_COVERED + ","
                + COLUMN_SUN_AZ + "," + COLUMN_SUN_ALT + "," + COLUMN_LOCAL_MAG
                + "," + COLUMN_SAROS_NUM + "," + COLUMN_SAROS_MEMBER_NUM + ","
                + COLUMN_MOON_AZ + "," + COLUMN_MOON_ALT + ","
                + COLUMN_GLOBAL_MAX + "," + COLUMN_GLOBAL_BEGIN + ","
                + COLUMN_GLOBAL_END + "," + COLUMN_GLOBAL_TOTAL_BEGIN + ","
                + COLUMN_GLOBAL_TOTAL_END + "," + COLUMN_GLOBAL_CENTER_BEGIN
                + "," + COLUMN_GLOBAL_CENTER_END + "," + COLUMN_ECLIPSE_DATE
                + "," + COLUMN_ECLIPSE_TYPE + ") VALUES (";
        ip2 = ",0,0,-1,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0.0,"
                + "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,'T');";
        for (int i = 0; i < 10; i++) {
            database.execSQL(ip1 + i + ip2);
        }
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(SolarEclipseTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SOLAR_ECLIPSE);
        onCreate(database);
    }
}