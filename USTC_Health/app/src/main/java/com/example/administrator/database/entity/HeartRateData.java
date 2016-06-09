package com.example.administrator.database.entity;

import com.example.administrator.database.DBConstants;
import com.example.administrator.database.annotation.ColumnName;
import com.example.administrator.database.annotation.PrimaryKey;
import com.example.administrator.database.annotation.TableName;

/**
 * Created by CaoRuijuan on 3/19/16.
 */
@TableName("heartRateData")
public class HeartRateData {
    @PrimaryKey(autoincrement=true)
    @ColumnName(DBConstants.TABLE_BLE_KEY)
    public int id;

    @ColumnName(DBConstants.BLE_DATATIME)
    public String datatime;

    @ColumnName(DBConstants.BLE_HEARTRATE)
    public String heartrate;
}
