package com.example.administrator.database.entity;

import com.example.administrator.database.DBConstants;
import com.example.administrator.database.Utils;
import com.example.administrator.database.annotation.ColumnName;
import com.example.administrator.database.annotation.PrimaryKey;
import com.example.administrator.database.annotation.TableName;

/**
 * Created by CaoRuijuan on 3/15/16.
 */
@TableName("bleData")
public class BleData {
    @PrimaryKey(autoincrement=true)
    @ColumnName(DBConstants.TABLE_BLE_KEY)
    public int id;

    @ColumnName(DBConstants.BLE_DATATIME)
    public String datatime;

    @ColumnName(DBConstants.BLE_HEARTRATE)
    public String heartrate;

    @ColumnName(DBConstants.BLE_STEPNUM)
    public String stepnum;

    @ColumnName(DBConstants.BLE_CALORIE)
    public String calorie;

    @ColumnName(DBConstants.BLE_AMUTOFERCE)
    public String amutoferce;

    public BleData(){
        datatime = Utils.getDataAndTime();
    }
}
