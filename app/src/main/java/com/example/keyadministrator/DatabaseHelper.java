package com.example.keyadministrator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    // 用户表创建表头开始
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_USER_PART = "user_part";
    private static final String COLUMN_USER_PHONE = "user_phone";
    private static final String COLUMN_FACE_ID = "face_id";
    private static final String COLUMN_CARD_ID = "card_id";
    private static final String COLUMN_FACE_PATH = "image_path";
    private static final String COLUMN_CK0 = "ck0";
    private static final String COLUMN_CK1 = "ck1";
    private static final String COLUMN_CK2 = "ck2";
    private static final String COLUMN_CK3 = "ck3";
    private static final String COLUMN_CK4 = "ck4";
    private static final String COLUMN_CK5 = "ck5";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_ENABLED = "enabled";
    // 用户表创建表头结束

    private static final String TABLE_ITEMS = "items";

    private static final String TABLE_CABINETS = "cabinets";

    // 类别管理创建表头开始
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_INDEX = "category_index";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TYPE_NAME = "type_name";
    public static final String COLUMN_TYPE_UNIT = "type_unit";
    public static final String COLUMN_TYPE_IMAGE = "type_image";
    // 类别管理创建表头结束

    // 物品管理创建表头开始
    public static final String TABLE_GOODS = "goods"; // 修改表名为 goods
    public static final String COLUMN_RFID = "rfid";
    public static final String COLUMN_MANUFACTURER = "manufacturer";
    public static final String COLUMN_LIFETIME = "lifetime";
    public static final String COLUMN_BORROW_START_TIME = "borrow_start_time";
    public static final String COLUMN_RETURN_DUE_TIME = "return_due_time";
    public static final String COLUMN_BORROW_DURATION = "borrow_duration";
    public static final String COLUMN_CABINET_NUMBER_CHECKED = "cabinet_number_checked";
    public static final String COLUMN_BELONGING_CABINET_NUMBER = "belonging_cabinet_number";
    public static final String COLUMN_ACTUAL_BELONGING_CABINET_NUMBER = "actual_belonging_cabinet_number";
    public static final String COLUMN_BORROW_OR_NOT = "borrow_or_not";
    // 物品管理创建表头结束

    // 台账信息创建表头开始
    private static final String TABLE_BORROW_RETURN= "borrowreturn";
    private static final String COLUMN_BORROW_RETURN_ID = "id";
    private static final String COLUMN_ITEM_TYPE = "item_type";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_OPERATION_TYPE = "operation_type";
    private static final String COLUMN_CABINET_NUMBER = "cabinet_number";
    private static final String COLUMN_OPERATION_DATE = "operation_date";
    // 台账信息创建表头结束

    // 参数配置表头开始
    public static final String TABLE_CONFIG = "config";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SERIAL_PORT = "serial_port";
    public static final String COLUMN_BAUD_RATE = "baud_rate";
    // 参数配置表头结束

    // 创建用户数据表
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_USER_PART + " TEXT,"
            + COLUMN_USER_PHONE + " TEXT,"
            + COLUMN_FACE_ID + " TEXT,"
            + COLUMN_CARD_ID + " TEXT,"
            + COLUMN_CK0 + " INTEGER,"
            + COLUMN_CK1 + " INTEGER,"
            + COLUMN_CK2 + " INTEGER,"
            + COLUMN_CK3 + " INTEGER,"
            + COLUMN_CK4 + " INTEGER,"
            + COLUMN_CK5 + " INTEGER,"
            + COLUMN_FACE_PATH + " TEXT,"
            + COLUMN_START_TIME + " TEXT,"
            + COLUMN_END_TIME + " TEXT,"
            + COLUMN_ENABLED + " INTEGER" + ")";

    // 创建首页三个数据表
    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "total_count INTEGER," +
            "stock_count INTEGER," +
            "lend_count INTEGER)";

    // 创建箱子的数据表
    private static final String CREATE_TABLE_CABINETS = "CREATE TABLE " + TABLE_CABINETS + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT)";
    // 插入箱子的默认值
    private static final String INSERT_DEFAULT_CABINETS = "INSERT INTO " + TABLE_CABINETS + " (name) VALUES " +
            "('总计'), ('1号柜'), ('2号柜'), ('3号柜'), ('4号柜')";

    // 创建类别管理的数据表语句
    private static final String CREATE_TABLE_CATEGORIES = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT,%s INTEGER,%s TEXT,%s TEXT,%s TEXT)", TABLE_CATEGORIES, COLUMN_ID, COLUMN_INDEX, COLUMN_TYPE_NAME, COLUMN_TYPE_UNIT, COLUMN_TYPE_IMAGE);

    // 创建物品管理数据表
    private static final String CREATE_TABLE_GOODS = "CREATE TABLE " + TABLE_GOODS + "(" // 修改表名为 goods
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_RFID + " TEXT,"
            + COLUMN_TYPE_NAME + " TEXT,"
            + COLUMN_MANUFACTURER + " TEXT,"
            + COLUMN_LIFETIME + " INTEGER,"
            + COLUMN_BORROW_START_TIME + " TEXT,"
            + COLUMN_RETURN_DUE_TIME + " TEXT,"
            + COLUMN_BORROW_DURATION + " INTEGER,"
            + COLUMN_CABINET_NUMBER_CHECKED + " INTEGER,"
            + COLUMN_BELONGING_CABINET_NUMBER + " TEXT,"
            + COLUMN_ACTUAL_BELONGING_CABINET_NUMBER + " TEXT,"
            + COLUMN_BORROW_OR_NOT + " INTEGER DEFAULT 0"
            + ")";

    // 创建借出归还信息表
    private static final String CREATE_BORROW_TABLE = "CREATE TABLE " + TABLE_BORROW_RETURN + "("
            + COLUMN_BORROW_RETURN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ITEM_TYPE + " TEXT,"
            + COLUMN_RFID + " TEXT,"
            + COLUMN_USER_ID + " INTEGER,"
            + COLUMN_USER + " TEXT,"
            + COLUMN_OPERATION_TYPE + " TEXT," // 借出或归还
            + COLUMN_CABINET_NUMBER + " TEXT,"
            + COLUMN_OPERATION_DATE + " TEXT" // 日期时间格式，例如 "yyyy-MM-dd HH:mm:ss"
            + ")";

    // 创建参数配置表的 SQL 语句
    private static final String CREATE_TABLE_CONFIG = "CREATE TABLE " + TABLE_CONFIG + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_SERIAL_PORT + " TEXT, "
            + COLUMN_BAUD_RATE + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行首页概括数据
        db.execSQL(CREATE_TABLE_ITEMS);
        // 执行首页箱子数据
        db.execSQL(CREATE_TABLE_CABINETS);
        // 执行插入箱子默认值
        db.execSQL(INSERT_DEFAULT_CABINETS);
        // 创建用户列表
        db.execSQL(CREATE_USERS_TABLE);
        // 创建类别管理表格
        db.execSQL(CREATE_TABLE_CATEGORIES);
        // 创建物品管理表格
        db.execSQL(CREATE_TABLE_GOODS);
        // 创建借出归还信息表格
        db.execSQL(CREATE_BORROW_TABLE);
        // 创建参数配置表
        db.execSQL(CREATE_TABLE_CONFIG);

        // 插入默认数据
        insertConfig(db, "智能锁", "/dev/ttyS1", 9600);
        insertConfig(db, "智能标签", "/dev/ttyS2", 115200);
        insertConfig(db, "电量显示", "/dev/ttyS3", 9600);
        insertConfig(db, "ip", "10.1.177.49", 9000);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CABINETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROW_RETURN);
        onCreate(db);
    }
    // 添加用户方法
    public void addUser(String username, String password, String user_part, String user_phone, String face_id, String card_id, int ck0, int ck1, int ck2, int ck3, int ck4, int ck5, String image_path, String startTime, String endTime, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_PART, user_part);
        values.put(COLUMN_USER_PHONE, user_phone);
        values.put(COLUMN_FACE_ID, face_id);
        values.put(COLUMN_CARD_ID, card_id);
        values.put(COLUMN_CK0, ck0);
        values.put(COLUMN_CK1, ck1);
        values.put(COLUMN_CK2, ck2);
        values.put(COLUMN_CK3, ck3);
        values.put(COLUMN_CK4, ck4);
        values.put(COLUMN_CK5, ck5);
        values.put(COLUMN_FACE_PATH, image_path);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_ENABLED, enabled);
        db.insert(TABLE_USERS, null, values);
    }
    // 批量上传用户结果
    public boolean addUsers(List<User> users) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean allSuccess = true; // 初始化所有操作都成功的标志

        for (User user : users) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_PASSWORD, user.getPassword());
            values.put(COLUMN_USER_PART, user.getUserPart());
            values.put(COLUMN_USER_PHONE, user.getUserPhone());
            values.put(COLUMN_CARD_ID, user.getCardId());
            values.put(COLUMN_CK0, user.isCk0());
            values.put(COLUMN_CK1, user.isCk1());
            values.put(COLUMN_CK2, user.isCk2());
            values.put(COLUMN_CK3, user.isCk3());
            values.put(COLUMN_CK4, user.isCk4());
            values.put(COLUMN_CK5, user.isCk5());
            values.put(COLUMN_ENABLED, user.getEnabled());
            long newRowId = db.insert(TABLE_USERS, null, values);
            if (newRowId == -1) {
                allSuccess = false; // 如果任何一个插入操作失败，将标志设置为 false
            }
        }

        db.close();
        return allSuccess; // 返回所有操作是否都成功
    }
    // 查询用户方法
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
        int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
        int userPartIndex = cursor.getColumnIndex(COLUMN_USER_PART);
        int userPhoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
        int faceIdIndex = cursor.getColumnIndex(COLUMN_FACE_ID);
        int facePathIndex = cursor.getColumnIndex(COLUMN_FACE_PATH);
        int cardIdIndex = cursor.getColumnIndex(COLUMN_CARD_ID);
        int column0Index = cursor.getColumnIndex(COLUMN_CK0);
        int column1Index = cursor.getColumnIndex(COLUMN_CK1);
        int column2Index = cursor.getColumnIndex(COLUMN_CK2);
        int column3Index = cursor.getColumnIndex(COLUMN_CK3);
        int column4Index = cursor.getColumnIndex(COLUMN_CK4);
        int column5Index = cursor.getColumnIndex(COLUMN_CK5);
        int startTimeIndex = cursor.getColumnIndex(COLUMN_START_TIME);
        int endTimeIndex = cursor.getColumnIndex(COLUMN_END_TIME);
        int columnEnabled = cursor.getColumnIndex(COLUMN_ENABLED);

        if (idIndex == -1 || usernameIndex == -1 || passwordIndex == -1 || userPartIndex == -1 || userPhoneIndex == -1
                || faceIdIndex == -1 || facePathIndex == -1 || cardIdIndex == -1 || column0Index == -1 || column1Index == -1 ||
        column2Index == -1 || column3Index == -1 || column4Index == -1 || column5Index == -1 || startTimeIndex == -1 || endTimeIndex == -1 || columnEnabled == -1) {
            // Handle error or throw an exception
            throw new IllegalArgumentException("Database column not found");
        }else{
            if (cursor.moveToFirst()){
                do {
                    User user = new User(
                            cursor.getInt(idIndex),
                            cursor.getString(usernameIndex),
                            cursor.getString(passwordIndex),
                            cursor.getString(userPartIndex),
                            cursor.getString(userPhoneIndex),
                            cursor.getString(faceIdIndex),
                            cursor.getString(facePathIndex),
                            cursor.getString(cardIdIndex),
                            cursor.getInt(column0Index),
                            cursor.getInt(column1Index),
                            cursor.getInt(column2Index),
                            cursor.getInt(column3Index),
                            cursor.getInt(column4Index),
                            cursor.getInt(column5Index),
                            cursor.getString(startTimeIndex),
                            cursor.getString(endTimeIndex),
                            cursor.getInt(columnEnabled)
                    );
                    userList.add(user);
                } while (cursor.moveToNext());
            }
            cursor.close();
            // 注意：这里不需要关闭数据库连接，因为 getReadableDatabase() 返回的是一个共享的连接
            return userList;
        }
    }
    // 通过id查询用户数据
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int userPartIndex = cursor.getColumnIndex(COLUMN_USER_PART);
            int userPhoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
            int faceIdIndex = cursor.getColumnIndex(COLUMN_FACE_ID);
            int facePathIndex = cursor.getColumnIndex(COLUMN_FACE_PATH);
            int cardIdIndex = cursor.getColumnIndex(COLUMN_CARD_ID);
            int column0Index = cursor.getColumnIndex(COLUMN_CK0);
            int column1Index = cursor.getColumnIndex(COLUMN_CK1);
            int column2Index = cursor.getColumnIndex(COLUMN_CK2);
            int column3Index = cursor.getColumnIndex(COLUMN_CK3);
            int column4Index = cursor.getColumnIndex(COLUMN_CK4);
            int column5Index = cursor.getColumnIndex(COLUMN_CK5);
            int startTimeIndex = cursor.getColumnIndex(COLUMN_START_TIME);
            int endTimeIndex = cursor.getColumnIndex(COLUMN_END_TIME);
            int columnEnabled = cursor.getColumnIndex(COLUMN_ENABLED);

            if (idIndex >= 0 && usernameIndex >= 0 && passwordIndex >= 0 && userPartIndex >= 0 && userPhoneIndex >= 0
                    && faceIdIndex >= 0 && facePathIndex >= 0 && column0Index >= 0 && column1Index >= 0 &&
                    column2Index >= 0 && column3Index >= 0 && column4Index >= 0 && column5Index >= 0 || startTimeIndex >= 0 || endTimeIndex >= 0 || columnEnabled >= 0) {
                user = new User(
                        cursor.getInt(idIndex),
                        cursor.getString(usernameIndex),
                        cursor.getString(passwordIndex),
                        cursor.getString(userPartIndex),
                        cursor.getString(userPhoneIndex),
                        cursor.getString(faceIdIndex),
                        cursor.getString(facePathIndex),
                        cursor.getString(cardIdIndex),
                        cursor.getInt(column0Index),
                        cursor.getInt(column1Index),
                        cursor.getInt(column2Index),
                        cursor.getInt(column3Index),
                        cursor.getInt(column4Index),
                        cursor.getInt(column5Index),
                        cursor.getString(startTimeIndex),
                        cursor.getString(endTimeIndex),
                        cursor.getInt(columnEnabled)
                );
            } else {
                // 处理列不存在的情况
                Log.e("DatabaseHelper", "One or more columns not found");
            }
            cursor.close();
        }
        return user;
    }

    // 通过人脸id或者人员id进行查询

    public User getUserByIdOrFaceId(Object identifier) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        String selection;
        String[] selectionArgs;

        if (identifier instanceof Integer) {
            //通过 id 查询
            selection = COLUMN_ID + "=?";
            selectionArgs = new String[]{String.valueOf(identifier)};
        } else if (identifier instanceof String) {
            // 通过 faceId 查询
            selection = COLUMN_FACE_ID + "=?";
            selectionArgs = new String[]{(String) identifier};
        } else {
            // 处理无效参数类型
            return null;
        }

        cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor !=null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int userPartIndex = cursor.getColumnIndex(COLUMN_USER_PART);
            int userPhoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
            int faceIdIndex = cursor.getColumnIndex(COLUMN_FACE_ID);
            int facePathIndex = cursor.getColumnIndex(COLUMN_FACE_PATH);
            int cardIdIndex = cursor.getColumnIndex(COLUMN_CARD_ID);
            int column0Index = cursor.getColumnIndex(COLUMN_CK0);
            int column1Index = cursor.getColumnIndex(COLUMN_CK1);
            int column2Index = cursor.getColumnIndex(COLUMN_CK2);
            int column3Index = cursor.getColumnIndex(COLUMN_CK3);
            int column4Index = cursor.getColumnIndex(COLUMN_CK4);
            int column5Index = cursor.getColumnIndex(COLUMN_CK5);
            int startTimeIndex = cursor.getColumnIndex(COLUMN_START_TIME);
            int endTimeIndex = cursor.getColumnIndex(COLUMN_END_TIME);
            int columnEnabled = cursor.getColumnIndex(COLUMN_ENABLED);

            if (idIndex >= 0 && usernameIndex >= 0 && passwordIndex >= 0 && userPartIndex >= 0 && userPhoneIndex >= 0
                    && faceIdIndex >= 0 && facePathIndex >= 0 && column0Index >= 0 && column1Index >= 0 &&
                    column2Index >= 0 && column3Index >= 0 && column4Index >= 0 && column5Index >= 0 || startTimeIndex >= 0 ||
                    endTimeIndex >= 0 || columnEnabled >= 0) {
                user = new User(
                        cursor.getInt(idIndex),
                        cursor.getString(usernameIndex),
                        cursor.getString(passwordIndex),
                        cursor.getString(userPartIndex),
                        cursor.getString(userPhoneIndex),
                        cursor.getString(faceIdIndex),
                        cursor.getString(facePathIndex),
                        cursor.getString(cardIdIndex),
                        cursor.getInt(column0Index),
                        cursor.getInt(column1Index),
                        cursor.getInt(column2Index),
                        cursor.getInt(column3Index),
                        cursor.getInt(column4Index),
                        cursor.getInt(column5Index),
                        cursor.getString(startTimeIndex),
                        cursor.getString(endTimeIndex),
                        cursor.getInt(columnEnabled)
                );
            } else {
                // 处理列不存在的情况
                Log.e("DatabaseHelper", "One or more columns not found");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    // 修改对应用户 ID 的数据
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_USER_PART, user.getUserPart());
        values.put(COLUMN_USER_PHONE, user.getUserPhone());
        values.put(COLUMN_FACE_ID, user.getFaceId());
        values.put(COLUMN_CARD_ID, user.getCardId());
        values.put(COLUMN_CK0, user.isCk0());
        values.put(COLUMN_CK1, user.isCk1());
        values.put(COLUMN_CK2, user.isCk2());
        values.put(COLUMN_CK3, user.isCk3());
        values.put(COLUMN_CK4,user.isCk4());
        values.put(COLUMN_CK5, user.isCk5());
        values.put(COLUMN_FACE_PATH, user.getImagePath());
        values.put(COLUMN_START_TIME, user.getStartTime());
        values.put(COLUMN_END_TIME, user.getEndTime());
        values.put(COLUMN_ENABLED, user.getEnabled());
        db.update(TABLE_USERS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
    }

    // 删除用户
    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }

    // 用户名和密码一致后返回用户信息
    public User getUserByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username,password}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int userPartIndex = cursor.getColumnIndex(COLUMN_USER_PART);
            int userPhoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
            int faceIdIndex = cursor.getColumnIndex(COLUMN_FACE_ID);
            int facePathIndex =cursor.getColumnIndex(COLUMN_FACE_PATH);
            int cardIdIndex = cursor.getColumnIndex(COLUMN_CARD_ID);
            int column0Index = cursor.getColumnIndex(COLUMN_CK0);
            int column1Index = cursor.getColumnIndex(COLUMN_CK1);
            int column2Index = cursor.getColumnIndex(COLUMN_CK2);
            int column3Index = cursor.getColumnIndex(COLUMN_CK3);
            int column4Index = cursor.getColumnIndex(COLUMN_CK4);
            int column5Index = cursor.getColumnIndex(COLUMN_CK5);
            int startTimeIndex = cursor.getColumnIndex(COLUMN_START_TIME);
            int endTimeIndex = cursor.getColumnIndex(COLUMN_END_TIME);
            int columnEnabled = cursor.getColumnIndex(COLUMN_ENABLED);

            if (idIndex >= 0 && usernameIndex >= 0 && passwordIndex >= 0 && userPartIndex >= 0 && userPhoneIndex >= 0
                    && faceIdIndex >= 0 && facePathIndex >= 0 && column0Index >= 0 && column1Index >= 0 &&
                    column2Index >= 0 && column3Index >= 0 && column4Index >= 0 && column5Index >= 0 || startTimeIndex >= 0 || endTimeIndex >= 0 || columnEnabled >= 0) {
                user = new User(
                        cursor.getInt(idIndex),
                        cursor.getString(usernameIndex),
                        cursor.getString(passwordIndex),
                        cursor.getString(userPartIndex),
                        cursor.getString(userPhoneIndex),
                        cursor.getString(faceIdIndex),
                        cursor.getString(facePathIndex),
                        cursor.getString(cardIdIndex),
                        cursor.getInt(column0Index),
                        cursor.getInt(column1Index),
                        cursor.getInt(column2Index),
                        cursor.getInt(column3Index),
                        cursor.getInt(column4Index),
                        cursor.getInt(column5Index),
                        cursor.getString(startTimeIndex),
                        cursor.getString(endTimeIndex),
                        cursor.getInt(columnEnabled)
                );
            } else {
                // 处理列不存在的情况
                Log.e("DatabaseHelper", "One or more columns not found");
            }
            cursor.close();
        }
        return user;
    }
    // 使用 cardId 查询用户
    public User getUserByCardId(String cardId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,COLUMN_CARD_ID + "=?",
                new String[]{cardId}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int userPartIndex = cursor.getColumnIndex(COLUMN_USER_PART);
            int userPhoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
            int faceIdIndex = cursor.getColumnIndex(COLUMN_FACE_ID);
            int facePathIndex =cursor.getColumnIndex(COLUMN_FACE_PATH);
            int cardIdIndex = cursor.getColumnIndex(COLUMN_CARD_ID);
            int column0Index = cursor.getColumnIndex(COLUMN_CK0);
            int column1Index = cursor.getColumnIndex(COLUMN_CK1);
            int column2Index = cursor.getColumnIndex(COLUMN_CK2);
            int column3Index = cursor.getColumnIndex(COLUMN_CK3);
            int column4Index = cursor.getColumnIndex(COLUMN_CK4);
            int column5Index = cursor.getColumnIndex(COLUMN_CK5);
            int startTimeIndex = cursor.getColumnIndex(COLUMN_START_TIME);
            int endTimeIndex = cursor.getColumnIndex(COLUMN_END_TIME);
            int columnEnabled = cursor.getColumnIndex(COLUMN_ENABLED);

            if (idIndex >= 0 && usernameIndex >= 0 && passwordIndex >= 0 && userPartIndex >= 0 && userPhoneIndex >= 0
                    && faceIdIndex >= 0 && facePathIndex >= 0 && cardIdIndex >= 0 && column0Index >= 0 && column1Index >= 0 &&
                    column2Index >= 0 && column3Index >= 0 && column4Index >= 0 && column5Index >= 0 || startTimeIndex >= 0 || endTimeIndex >= 0
                    || columnEnabled >= 0) {
                user = new User(
                    cursor.getInt(idIndex),
                    cursor.getString(usernameIndex),
                    cursor.getString(passwordIndex),
                    cursor.getString(userPartIndex),
                    cursor.getString(userPhoneIndex),
                    cursor.getString(faceIdIndex),
                    cursor.getString(facePathIndex),
                    cursor.getString(cardIdIndex),
                    cursor.getInt(column0Index),
                    cursor.getInt(column1Index),
                    cursor.getInt(column2Index),
                    cursor.getInt(column3Index),
                    cursor.getInt(column4Index),
                    cursor.getInt(column5Index),
                    cursor.getString(startTimeIndex),
                    cursor.getString(endTimeIndex),
                    cursor.getInt(columnEnabled)
            );
            } else {
                // 处理列不存在的情况
                Log.e("DatabaseHelper", "One or more columns not found");
            }
            cursor.close();
        }
        return user;
    }
    // 检验用户有效性
    public ValidationResult validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[] { COLUMN_ENABLED},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[] { username, password }, null, null, null);

        ValidationResult result;
        if (cursor.moveToFirst()) {
            int enabledIndex = cursor.getColumnIndex(COLUMN_ENABLED);
            if (enabledIndex >= 0) {
                int enabledValue = cursor.getInt(enabledIndex);
                result = new ValidationResult(true, enabledValue);
            } else {
                result = new ValidationResult(false, -1); // 表示错误
            }
        } else {
            result = new ValidationResult(false, -1); // 用户名或密码不匹配
        }
        cursor.close();
        return result;
    }

    // 获取所有箱子方法
    public List<String> getAllCabinets() {
        List<String> cabinets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM cabinets", null);
        if (cursor.moveToFirst()) {
            do {
                cabinets.add(cursor.getString(0));  // 假设 name 是在第一列
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cabinets;
    }

    // 添加分类管理内容条目
    public long addCategory(int index, String typeName, String typeUnit, String typeImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INDEX, index);
        values.put(COLUMN_TYPE_NAME, typeName);
        values.put(COLUMN_TYPE_UNIT, typeUnit);
        values.put(COLUMN_TYPE_IMAGE, typeImage);
        long newRowId = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return newRowId;
    }

    // 批量添加分类管理内容条目
    public boolean addCategories(List<Category> categories) {
        SQLiteDatabase db = this.getWritableDatabase();ContentValues values = new ContentValues();
        for (Category category : categories) {
            values.clear(); // 清空 values
            values.put(COLUMN_INDEX, category.getIndex());
            values.put(COLUMN_TYPE_NAME, category.getTypeName());
            values.put(COLUMN_TYPE_UNIT, category.getTypeUnit());
            values.put(COLUMN_TYPE_IMAGE, category.getTypeImage());
            long newRowId = db.insert(TABLE_CATEGORIES, null, values);
            if (newRowId == -1) {
                db.close();
                return false; // 插入失败
            }
        }
        db.close();
        return true; // 所有数据添加成功
    }
    // 查询所有类别
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, null);
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int indexIndex = cursor.getColumnIndex(COLUMN_INDEX);
        int typeIndex = cursor.getColumnIndex(COLUMN_TYPE_NAME);
        int typeUnitIndex = cursor.getColumnIndex(COLUMN_TYPE_UNIT);
        int typeImageIndex = cursor.getColumnIndex(COLUMN_TYPE_IMAGE);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(idIndex);
                int index = cursor.getInt(indexIndex);
                String typeName = cursor.getString(typeIndex);
                String typeUnit = cursor.getString(typeUnitIndex);
                String typeImage = cursor.getString(typeImageIndex);
                Category category = new Category(id, index, typeName, typeUnit, typeImage);
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryList;
    }
    // 根据 ID 查询类别
    public Category getCategoryById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(categoryId)}, null, null, null);
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int indexIndex = cursor.getColumnIndex(COLUMN_INDEX);
        int typeIndex = cursor.getColumnIndex(COLUMN_TYPE_NAME);
        int typeUnitIndex = cursor.getColumnIndex(COLUMN_TYPE_UNIT);
        int typeImageIndex = cursor.getColumnIndex(COLUMN_TYPE_IMAGE);
        Category category = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(idIndex);
            int index = cursor.getInt(indexIndex);
            String typeName = cursor.getString(typeIndex);
            String typeUnit = cursor.getString(typeUnitIndex);
            String typeImage = cursor.getString(typeImageIndex);
            category = new Category(id, index, typeName, typeUnit, typeImage);
        }

        cursor.close();
        return category;
    }
    // 根据名称搜索
    public List<Category> searchCategories(String query) {
        List<Category> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_TYPE_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%"}; // 模糊查询

        Cursor cursor = db.query(
                TABLE_CATEGORIES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int index = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INDEX));
                String typeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
                String typeUnit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_UNIT));
                String typeImage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_IMAGE));
                results.add(new Category(id, index, typeName, typeUnit, typeImage));
            } while (cursor.moveToNext());
        }cursor.close();
        db.close();
        return results;
    }
    //删除类别管理
    public void deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, COLUMN_ID + "=?", new String[]{String.valueOf(categoryId)});
    }
    // 更新类别管理
    public int updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INDEX, category.getIndex());
        values.put(COLUMN_TYPE_NAME, category.getTypeName());
        values.put(COLUMN_TYPE_UNIT, category.getTypeUnit());
        values.put(COLUMN_TYPE_IMAGE, category.getTypeImage());

        // 更新类别，WHERE 子句匹配 ID
        return db.update(TABLE_CATEGORIES, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(category.getId())});
    }
    /**
     * 添加物品管理条目到数据库
     *
     * @param rfid                      RFID
     * @param typeName                  类型名称
     * @param manufacturer              生产厂家
     * @param lifetime                  使用寿命
     * @param borrowStartTime           借出起始时间
     * @param returnDueTime             归还截至时间
     * @param borrowDuration            借出时长
     * @param cabinetNumberChecked      入柜编号是否检查
     * @param belongingCabinetNumber    归属柜子号码
     * @param actualBelongingCabinetNumber 实际归属柜子号码
     * @return 插入成功的行 ID，如果失败则返回 -1
     */
    public long addGood(String rfid, String typeName, String manufacturer, int lifetime,
                        String borrowStartTime, String returnDueTime, int borrowDuration,
                        int cabinetNumberChecked, String belongingCabinetNumber, String actualBelongingCabinetNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RFID, rfid);
        values.put(COLUMN_TYPE_NAME, typeName);
        values.put(COLUMN_MANUFACTURER, manufacturer);
        values.put(COLUMN_LIFETIME, lifetime);
        values.put(COLUMN_BORROW_START_TIME, borrowStartTime);
        values.put(COLUMN_RETURN_DUE_TIME, returnDueTime);
        values.put(COLUMN_BORROW_DURATION, borrowDuration);
        values.put(COLUMN_CABINET_NUMBER_CHECKED, cabinetNumberChecked);
        values.put(COLUMN_BELONGING_CABINET_NUMBER, belongingCabinetNumber);
        values.put(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER, actualBelongingCabinetNumber);

        long id = db.insert(TABLE_GOODS, null, values);
        db.close();
        return id;
    }

    // 批量添加物品管理条目
    public boolean addGoods(List<Good> goods) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean allSuccess = true; // 初始化所有操作都成功的标志
        for (Good good : goods) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_RFID, good.getRfid());
            values.put(COLUMN_TYPE_NAME, good.getTypeName());
            values.put(COLUMN_MANUFACTURER, good.getManufacturer());
            values.put(COLUMN_LIFETIME, good.getLifetime());
            values.put(COLUMN_BORROW_START_TIME, good.getBorrowStartTime());
            values.put(COLUMN_RETURN_DUE_TIME, good.getReturnDueTime());
            values.put(COLUMN_BORROW_DURATION, good.getBorrowDuration());
            values.put(COLUMN_CABINET_NUMBER_CHECKED, good.getCabinetNumberChecked());
            values.put(COLUMN_BELONGING_CABINET_NUMBER, good.getBelongingCabinetNumber());
            values.put(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER, good.getActualBelongingCabinetNumber());
            long newRowId = db.insert(TABLE_GOODS, null, values);
            if (newRowId == -1) {
                allSuccess = false; // 如果任何一个插入操作失败，将标志设置为 false
            }
        }
        db.close();
        return allSuccess; // 返回所有操作是否都成功
    }

    // 根据查询条件进行搜索物品管理条目
    public List<Good> searchGoods(String itemName, String rfid, String actualCabinetNumber, boolean isOverdue, String startTime, String endTime) {
        List<Good> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectionBuilder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        if (!itemName.isEmpty()) {
            selectionBuilder.append(COLUMN_TYPE_NAME).append(" LIKE ?");
            selectionArgsList.add("%" + itemName + "%");
        }

        if (!rfid.isEmpty()) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(COLUMN_RFID).append(" LIKE ?");
            selectionArgsList.add("%" + rfid + "%");
        }

        if (!actualCabinetNumber.isEmpty() && !actualCabinetNumber.equals("总计")) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER).append(" = ?");
            selectionArgsList.add(actualCabinetNumber);
        }

        if (isOverdue) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append("(")
                    .append(COLUMN_BORROW_START_TIME).append(" IS NOT NULL AND ")
                    .append(COLUMN_BORROW_DURATION).append(" IS NOT NULL AND ")
                    .append("strftime('%s', datetime(").append(COLUMN_BORROW_START_TIME).append(", '+'").append(" || ")
                    .append(COLUMN_BORROW_DURATION).append(" || ").append(" ' days')) < strftime('%s', 'now', 'localtime')")
                    .append(")");
        }

        if (!startTime.isEmpty()) {
            if (selectionBuilder.length() >0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(COLUMN_BORROW_START_TIME).append(" >= ?");
            selectionArgsList.add(startTime);
        }

        if (!endTime.isEmpty()) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(COLUMN_BORROW_START_TIME).append(" <= ?");
            selectionArgsList.add(endTime);
        }

        String selection = selectionBuilder.length() > 0 ? selectionBuilder.toString() : null;
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        Cursor cursor = db.query(
                TABLE_GOODS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            do {
                // 从 Cursor 中获取物品信息
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String rfidValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                String typeNameValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
                String manufacturerValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
                int lifetimeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIFETIME));
                String borrowStartTimeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_START_TIME));
                String returnDueTimeValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RETURN_DUE_TIME));
                int borrowDurationValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_DURATION));
                int cabinetNumberCheckedValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER_CHECKED));
                String belongingCabinetNumberValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BELONGING_CABINET_NUMBER));
                String actualBelongingCabinetNumberValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER));
                int borrowOrNotValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT));
                // 创建 Good 对象并添加到列表中
                Good good = new Good(id, rfidValue, typeNameValue, manufacturerValue, lifetimeValue, borrowStartTimeValue,
                        returnDueTimeValue, borrowDurationValue, cabinetNumberCheckedValue, belongingCabinetNumberValue,
                        actualBelongingCabinetNumberValue, borrowOrNotValue);
                goodsList.add(good);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return goodsList;
    }
    /**
     * 查询所有物品管理条目
     *
     * @return 物品管理条目列表
     */
    public List<Good> getAllGoods() {
        List<Good> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_GOODS;
        Cursor cursor = db.rawQuery(selectQuery, null);if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                String typeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
                String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
                int lifetime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIFETIME));
                String borrowStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_START_TIME));
                String returnDueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RETURN_DUE_TIME));
                int borrowDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_DURATION));
                int cabinetNumberChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER_CHECKED));
                String belongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BELONGING_CABINET_NUMBER));
                String actualBelongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER));
                int borrowOrNot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT));

                Good good = new Good(id, rfid, typeName, manufacturer, lifetime, borrowStartTime,
                        returnDueTime, borrowDuration, cabinetNumberChecked, belongingCabinetNumber, actualBelongingCabinetNumber, borrowOrNot);
                goodsList.add(good);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return goodsList;
    }
    /**
     * 通过 RFID 查询物品管理条目
     *
     * @param rfid 要查询的 RFID
     * @return 对应的物品管理条目，如果未找到则返回 null
     */
    public Good getGoodByRfid(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();String selectQuery = "SELECT * FROM " + TABLE_GOODS + " WHERE " + COLUMN_RFID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{rfid});

        Good good = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String typeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
            String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
            int lifetime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIFETIME));
            String borrowStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_START_TIME));
            String returnDueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RETURN_DUE_TIME));
            int borrowDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_DURATION));
            int cabinetNumberChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER_CHECKED));
            String belongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BELONGING_CABINET_NUMBER));
            String actualBelongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER));
            int borrowOrNot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT));

            good = new Good(id, rfid, typeName, manufacturer, lifetime, borrowStartTime,
                    returnDueTime, borrowDuration, cabinetNumberChecked, belongingCabinetNumber, actualBelongingCabinetNumber, borrowOrNot);
        }

        cursor.close();
        db.close();
        return good;
    }
    /**
     * 通过 ID 查询物品管理条目
     *
     * @param id 物品管理条目的 ID
     * @return 物品管理条目，如果未找到则返回 null
     */
    public Good getGoodById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_GOODS + " WHERE " + COLUMN_ID + " = " + id;
        Cursor cursor =db.rawQuery(selectQuery, null);

        Good good = null;
        if (cursor.moveToFirst()) {
            String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
            String typeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
            String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
            int lifetime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIFETIME));
            String borrowStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_START_TIME));
            String returnDueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RETURN_DUE_TIME));
            int borrowDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_DURATION));
            int cabinetNumberChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER_CHECKED));
            String belongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BELONGING_CABINET_NUMBER));
            String actualBelongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER));
            int borrowOrNot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT));

            good = new Good(id, rfid, typeName, manufacturer, lifetime, borrowStartTime,
                    returnDueTime, borrowDuration, cabinetNumberChecked, belongingCabinetNumber, actualBelongingCabinetNumber, borrowOrNot);
        }

        cursor.close();
        db.close();
        return good;
    }

    /**
     * 删除物品管理条目
     *
     * @param id 要删除的物品管理条目的 ID
     */
    public void deleteGood(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GOODS, COLUMN_ID + " = ?",new String[]{String.valueOf(id)});
    }
    /**
     * 更新物品管理条目
     *
     * @param good 要更新的物品管理条目
     * @return 如果更新成功则返回 true，否则返回 false
     */
    public boolean updateGood(Good good) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RFID, good.getRfid());
        values.put(COLUMN_TYPE_NAME, good.getTypeName());
        values.put(COLUMN_MANUFACTURER, good.getManufacturer());
        values.put(COLUMN_LIFETIME, good.getLifetime());
        values.put(COLUMN_BORROW_START_TIME, good.getBorrowStartTime());
        values.put(COLUMN_RETURN_DUE_TIME, good.getReturnDueTime());
        values.put(COLUMN_BORROW_DURATION, good.getBorrowDuration());
        values.put(COLUMN_CABINET_NUMBER_CHECKED, good.getCabinetNumberChecked());
        values.put(COLUMN_BELONGING_CABINET_NUMBER, good.getBelongingCabinetNumber());
        values.put(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER, good.getActualBelongingCabinetNumber());
        values.put(COLUMN_BORROW_OR_NOT, good.getBorrowOrNOt());

        int rowsAffected = db.update(TABLE_GOODS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(good.getId())});
        db.close();
        return rowsAffected > 0;
    }
    /**
     * 根据借用状态查询物品管理条目
     *
     * @param borrowStatus 借用状态（0：未借出，1：已借出）
     * @return 符合条件的物品管理条目列表
     */
    public List<Good> getGoodsByBorrowStatus(int borrowStatus) {
        List<Good> goodsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_GOODS + " WHERE " + COLUMN_BORROW_OR_NOT + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(borrowStatus)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                String typeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE_NAME));
                String manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER));
                int lifetime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIFETIME));
                String borrowStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_START_TIME));
                String returnDueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RETURN_DUE_TIME));
                int borrowDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_DURATION));
                int cabinetNumberChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER_CHECKED));
                String belongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BELONGING_CABINET_NUMBER));
                String actualBelongingCabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_BELONGING_CABINET_NUMBER));
                int borrowOrNot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT));

                Good good = new Good(id, rfid, typeName, manufacturer, lifetime, borrowStartTime,
                        returnDueTime, borrowDuration, cabinetNumberChecked, belongingCabinetNumber, actualBelongingCabinetNumber, borrowOrNot);
                goodsList.add(good);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return goodsList;
    }
    /**
     * 根据物品 ID 更新物品状态
     *
     * @param ids     要更新状态的物品 ID 列表
     * @param status 要设置的新状态值
     * @return 受影响的行数
     */
    public int updateGoodsStatusByIds(List<Integer> ids, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BORROW_OR_NOT, status);

        StringBuilder whereClause = new StringBuilder();
        whereClause.append(COLUMN_ID).append(" IN (");
        for (int i = 0; i < ids.size(); i++) {
            whereClause.append("?");if (i < ids.size() - 1) {
                whereClause.append(", ");
            }
        }
        whereClause.append(")");

        String[] whereArgs = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            whereArgs[i] = String.valueOf(ids.get(i));
        }

        int rowsAffected = db.update(TABLE_GOODS, values, whereClause.toString(), whereArgs);
        db.close();
        return rowsAffected;
    }
    /**
     * 统计所有柜子中每种类型名称的物品数量和借出数量
     *
     * @return 一个 HashMap，键为类型名称，值为一个长度为 2 的数组，
     *         数组的第一个元素表示该类型名称的物品总数，
     *         第二个元素表示该类型名称中借出的物品数量。
     */
    public HashMap<String, int[]> countGoodsByTypeAndBorrowStatusForAllCabinets() {
        HashMap<String, int[]> countMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT + ", COUNT(*) FROM " + TABLE_GOODS +
                " GROUP BY " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT; // 去除柜子号码筛选条件
        Cursor cursor = db.rawQuery(query, null); // 不需要传递参数
        if (cursor.moveToFirst()) {
            do {
                String typeName = cursor.getString(0);
                int borrowOrNot = cursor.getInt(1);
                int count = cursor.getInt(2);
                if (!countMap.containsKey(typeName)) {
                    countMap.put(typeName, new int[]{0, 0});
                }
                int[] counts = countMap.get(typeName);
                assert counts != null;
                counts[0] += count; // 总数
                if (borrowOrNot == 1) {
                    counts[1] += count; // 借出数量
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return countMap;
    }

    /**
     * 获取所有物品的总数和借出总数
     *
     * @return 一个长度为 2 的数组，第一个元素表示所有物品的总数，第二个元素表示借出的物品总数。
     */
    public int[] countTotalGoodsAndBorrowed() {
        int[] counts = new int[]{0, 0};
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*), SUM(CASE WHEN " + COLUMN_BORROW_OR_NOT + " = 1 THEN 1 ELSE 0 END) FROM " + TABLE_GOODS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            counts[0] = cursor.getInt(0); // 总数
            counts[1] = cursor.getInt(1); // 借出总数
        }
        cursor.close();
        db.close();
        return counts;
    }

    /**
     * 统计指定柜子中每种类型名称的物品数量和借出数量
     *
     * @param cabinetNumber 实际归属柜子号码
     * @return 一个 HashMap，键为类型名称，值为一个长度为 2 的数组，
     *         数组的第一个元素表示该类型名称的物品总数，
     *         第二个元素表示该类型名称中借出的物品数量。
     */
    public HashMap<String, int[]> countGoodsByTypeAndBorrowStatus(String cabinetNumber) {
        HashMap<String, int[]> countMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT + ", COUNT(*) FROM " + TABLE_GOODS +
                " WHERE " + COLUMN_BELONGING_CABINET_NUMBER + " = ?" + // 添加柜子号码筛选条件
                " GROUP BY " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT;
        Cursor cursor = db.rawQuery(query, new String[]{cabinetNumber}); // 传递柜子号码作为参数

        if (cursor.moveToFirst()) {
            do {
                String typeName = cursor.getString(0);
                int borrowOrNot = cursor.getInt(1);
                int count = cursor.getInt(2);
                if (!countMap.containsKey(typeName)) {
                    countMap.put(typeName, new int[]{0, 0});
                }
                int[] counts = countMap.get(typeName);
                assert counts != null;
                counts[0] += count; // 总数
                if (borrowOrNot == 1) {
                    counts[1] += count; // 借出数量
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return countMap;
    }

    /**
     * 统计指定柜子中每种类型名称的物品数量和借出数量
     *
     * @param cabinetNumber 实际归属柜子号码
     * @return 一个 HashMap，键为类型名称，值为一个长度为 2 的数组，
     *         数组的第一个元素表示该类型名称的物品总数，
     *         第二个元素表示该类型名称中借出的物品数量。
     */
    public HashMap<String, int[]> countGoodsByActualTypeAndBorrowStatus(String cabinetNumber) {
        HashMap<String, int[]> countMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT + ", COUNT(*) FROM " + TABLE_GOODS +
                " WHERE " + COLUMN_ACTUAL_BELONGING_CABINET_NUMBER + " = ?" + // 添加柜子号码筛选条件
                " GROUP BY " + COLUMN_TYPE_NAME + ", " + COLUMN_BORROW_OR_NOT;
        Cursor cursor = db.rawQuery(query, new String[]{cabinetNumber}); // 传递柜子号码作为参数

        if (cursor.moveToFirst()) {
            do {
                String typeName = cursor.getString(0);
                int borrowOrNot = cursor.getInt(1);
                int count = cursor.getInt(2);
                if (!countMap.containsKey(typeName)) {
                    countMap.put(typeName, new int[]{0, 0});
                }
                int[] counts = countMap.get(typeName);
                assert counts != null;
                counts[0] += count; // 总数
                if (borrowOrNot == 1) {
                    counts[1] += count; // 借出数量
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return countMap;
    }
    /**
     * 统计每个类型名称在每个实际归属柜子中的数量和借出数量
     *
     * @param typeName 类型名称
     * @return 一个 HashMap，键为实际归属柜子号码，值为一个长度为 2 的数组，
     *         数组的第一个元素表示该类型名称在该柜子中的总数，
     *         第二个元素表示该类型名称在该柜子中借出的数量。
     */
    public HashMap<String, int[]> countGoodsByTypeAndCabinetWithBorrow(String typeName) {
        HashMap<String, int[]> countMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ACTUAL_BELONGING_CABINET_NUMBER +
                ", SUM(CASE WHEN " + COLUMN_BORROW_OR_NOT + " = 0 THEN 1 ELSE 0 END)" +
                ", SUM(CASE WHEN " + COLUMN_BORROW_OR_NOT + " = 1 THEN 1 ELSE 0 END) " +
                "FROM " + TABLE_GOODS +
                " WHERE " + COLUMN_TYPE_NAME + " = ?" +
                " GROUP BY " + COLUMN_ACTUAL_BELONGING_CABINET_NUMBER;
        Cursor cursor = db.rawQuery(query, new String[]{typeName});
        if (cursor.moveToFirst()) {
            do {
                String cabinetNumber = cursor.getString(0);
                int totalCount = cursor.getInt(1);
                int borrowedCount = cursor.getInt(2);
                countMap.put(cabinetNumber, new int[]{totalCount, borrowedCount});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return countMap;
    }
    /**
     * 添加借出归还信息条目到数据库
     *
     * @param itemType        物品类型
     * @param userId          操作用户ID
     * @param user            操作用户
     * @param operationType   操作类型（"借出" 或 "归还"）
     * @param cabinetNumber   所属柜号
     * @param operationDate   操作日期时间（格式例如 "yyyy-MM-dd HH:mm:ss"）
     * @return 插入成功的行 ID，如果失败则返回 -1
     */
    public long addBorrowReturnRecord(String itemType, String rfid, int userId, String user, String operationType, String cabinetNumber, String operationDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_TYPE, itemType);
        values.put(COLUMN_RFID, rfid);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_USER, user);
        values.put(COLUMN_OPERATION_TYPE, operationType);
        values.put(COLUMN_CABINET_NUMBER, cabinetNumber);
        values.put(COLUMN_OPERATION_DATE, operationDate);

        long id = db.insert(TABLE_BORROW_RETURN, null, values);
        db.close();
        return id;
    }
    /**
     * 批量添加借出归还信息条目到数据库
     *
     * @param records 借出归还信息条目列表
     * @return 插入成功的行 ID 列表，如果失败则返回空列表
     */
    public List<Long> addBorrowReturnRecords(List<BorrowReturnRecord> records) {
        List<Long> insertedIds = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        for (BorrowReturnRecord record : records) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_TYPE, record.getItemType());
            values.put(COLUMN_RFID, record.getRfid());
            values.put(COLUMN_USER_ID, record.getUserId());
            values.put(COLUMN_USER, record.getUser());
            values.put(COLUMN_OPERATION_TYPE, record.getOperationType());
            values.put(COLUMN_CABINET_NUMBER, record.getCabinetNumber());
            values.put(COLUMN_OPERATION_DATE, record.getOperationDate());

            long id = db.insert(TABLE_BORROW_RETURN, null, values);
            if (id != -1) {
                insertedIds.add(id);
            }
        }

        db.close();
        return insertedIds;
    }
    /**
     * 查询所有借出归还记录
     *
     * @return 借出归还记录的列表
     */
    public List<BorrowReturnRecord> getAllBorrowReturnRecords(int limit, int offset) {
        List<BorrowReturnRecord>records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_BORROW_RETURN +
                " ORDER BY " + "DATETIME(" + COLUMN_OPERATION_DATE + ")" + " DESC " +
                " LIMIT " + limit + " OFFSET " + offset;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_RETURN_ID));
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_TYPE));
                String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER));
                String operationType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_TYPE));
                String cabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER));
                String operationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_DATE));

                BorrowReturnRecord record = new BorrowReturnRecord(id, itemType, rfid, userId, user, operationType, cabinetNumber, operationDate);
                records.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return records;
    }
    /**
     * 根据用户 ID 查询借出归还记录
     *
     * @param userId 用户 ID
     * @return 该用户的借出归还记录列表
     */
    public List<BorrowReturnRecord> getBorrowReturnRecordsByUserId(int userId) {
        List<BorrowReturnRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_BORROW_RETURN +
                " WHERE " + COLUMN_USER_ID + " = ? " +
                "ORDER BY " + COLUMN_OPERATION_DATE + " DESC";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_RETURN_ID));
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_TYPE));
                String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER));
                String operationType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_TYPE));
                String cabinetNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER));
                String operationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_DATE));

                BorrowReturnRecord record = new BorrowReturnRecord(id, itemType, rfid, userId, user, operationType, cabinetNumber, operationDate);
                records.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return records;
    }
    /**
     * 根据条件查询借出归还记录
     *
     * @param userId           用户 ID（可选，如果为 null 则不限制用户）
     * @param operationType   操作类型（可选，如果为 null 则不限制操作类型）
     * @param cabinetNumber   柜号（可选，如果为 null 则不限制柜号）
     * @param startTime       开始时间（可选，如果为 null 则不限制开始时间）
     * @param endTime         结束时间（可选，如果为 null 则不限制结束时间）
     * @return 符合条件的借出归还记录列表
     */
    public List<BorrowReturnRecord> queryBorrowReturnRecords(Integer userId, String operationType, String cabinetNumber, String startTime, String endTime) {
        List<BorrowReturnRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectQuery = new StringBuilder("SELECT * FROM " + TABLE_BORROW_RETURN);
        List<String> selectionArgs = new ArrayList<>();

        // 添加查询条件
        if (userId != null) {
            selectQuery.append(" WHERE ").append(COLUMN_USER_ID).append(" = ?");
            selectionArgs.add(String.valueOf(userId));
        }
        if (operationType != null && !operationType.equals("所有")) {
            selectQuery.append(selectionArgs.isEmpty() ? " WHERE " : " AND ").append(COLUMN_OPERATION_TYPE).append(" = ?");
            selectionArgs.add(operationType);
        }
        if (cabinetNumber != null && !cabinetNumber.equals("所有")) {
            selectQuery.append(selectionArgs.isEmpty() ? " WHERE " : " AND ").append(COLUMN_CABINET_NUMBER).append(" = ?");
            selectionArgs.add(cabinetNumber);
        }
        if (startTime != null) {
            selectQuery.append(selectionArgs.isEmpty() ? " WHERE " : " AND ").append(COLUMN_OPERATION_DATE).append(" >= ?");
            selectionArgs.add(startTime);
        }
        if (endTime != null) {
            selectQuery.append(selectionArgs.isEmpty() ? " WHERE " : " AND ").append(COLUMN_OPERATION_DATE).append(" <= ?");
            selectionArgs.add(endTime);
        }

        selectQuery.append(" ORDER BY ").append(COLUMN_OPERATION_DATE).append(" DESC");

        Cursor cursor = db.rawQuery(selectQuery.toString(), selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BORROW_RETURN_ID));
                String itemType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_TYPE));
                String rfid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RFID));
                String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER));
                String operation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_TYPE));
                String cabinet = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CABINET_NUMBER));
                String operationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OPERATION_DATE));
                int userIdValue = userId != null ? userId : -1;
                BorrowReturnRecord record = new BorrowReturnRecord(id, itemType, rfid, userIdValue, user, operation, cabinet, operationDate);
                records.add(record);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return records;
    }
    /**
     * 获取用户当前借入但尚未归还的记录
     *
     * @param userId 用户 ID
     * @return 用户当前借入但尚未归还的记录列表
     */
    public List<BorrowReturnRecord> getUnreturnedRecordsByUserId(int userId) {
        List<BorrowReturnRecord> unreturnedRecords = new ArrayList<>();
        List<BorrowReturnRecord>allUserRecords = getBorrowReturnRecordsByUserId(userId);
        for (BorrowReturnRecord record : allUserRecords) {
            Good good = getGoodByRfid(record.getRfid());
            if (good != null){
                if (record.getOperationType().equals("借出") && record.getOperationDate().equals(good.getBorrowStartTime())) {
                    // 获取物品的当前状态
                    int itemStatus = getItemStatusByItemType(record.getRfid()); // 您需要实现 getItemStatusByItemType 方法
                    if (itemStatus==1) { // 检查物品是否不在箱子中
                        unreturnedRecords.add(record);
                    }
                }
            }
        }

        return unreturnedRecords;
    }

    /**
     * 根据物品类型获取物品的当前状态
     *
     * @param rfid 物品编号
     * @return 物品的状态（0 表示在箱子中，1 表示不在箱子中）
     */
    private int getItemStatusByItemType(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        int status = 0;

        // 查询物品表以获取物品的状态
        String selectQuery = "SELECT borrow_or_not FROM goods WHERE rfid = ?"; // 替换 "goods" 为您的物品表名，"item_type" 为物品编号字段名
        Cursor cursor = db.rawQuery(selectQuery, new String[]{rfid});

        if (cursor.moveToFirst()) {
            status = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BORROW_OR_NOT)));
        }
        cursor.close();
        db.close();
        return status;
    }

    // 查询参数配置表并返回数据
    public List<Config> getAllConfigs() {
        List<Config> configList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONFIG;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String serialPort = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERIAL_PORT));
                int baudRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BAUD_RATE));

                Config config = new Config(id, name, serialPort, baudRate);
                configList.add(config);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return configList;
    }

    // 添加数据到参数配置表
    private long insertConfig(SQLiteDatabase db, String name, String serialPort, int baudRate) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SERIAL_PORT, serialPort);
        values.put(COLUMN_BAUD_RATE, baudRate);
        return db.insert(TABLE_CONFIG, null, values);
    }

    // 修改参数配置表中的数据
    public int updateConfig(int id, String name, String serialPort, int baudRate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SERIAL_PORT, serialPort);
        values.put(COLUMN_BAUD_RATE, baudRate);
        int rowsAffected = db.update(TABLE_CONFIG, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    // 根据名称查询参数配置表
    public Config getConfigByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CONFIG + " WHERE " + COLUMN_NAME + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{name});

        Config config = null;
        if (cursor.moveToFirst()) {
            int id= cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String serialPort = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERIAL_PORT));
            int baudRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BAUD_RATE));

            config = new Config(id, name, serialPort, baudRate);
        }

        cursor.close();
        db.close();
        return config;
    }
}