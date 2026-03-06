package com.quality.platform.util;

import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HBaseClientUtil {
    private final Connection connection;

    public void putData(String tableName, String rowKey, String family, Map<String, String> data) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        for (Map.Entry<String, String> entry : data.entrySet()) {
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(entry.getKey()), Bytes.toBytes(entry.getValue()));
        }
        table.put(put);
        table.close();
    }

    public Result getRow(String tableName, String rowKey) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(rowKey));
        Result result = table.get(get);
        table.close();
        return result;
    }

    // 新增扫描方法
    public List<Map<String, String>> scan(String tableName, String startRow, String stopRow) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        if (startRow != null) {
            scan.withStartRow(Bytes.toBytes(startRow));
        }
        if (stopRow != null) {
            scan.withStopRow(Bytes.toBytes(stopRow));
        }
        ResultScanner scanner = table.getScanner(scan);
        List<Map<String, String>> results = new ArrayList<>();
        for (Result result : scanner) {
            Map<String, String> rowMap = new HashMap<>();
            rowMap.put("rowKey", Bytes.toString(result.getRow()));
            for (Cell cell : result.listCells()) {
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                rowMap.put(qualifier, value);
            }
            results.add(rowMap);
        }
        scanner.close();
        table.close();
        return results;
    }
}
