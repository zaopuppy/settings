
-record (db_column, {id, type, name}).
-record (db_row, {id, values}).
-record (db_table, {id, name, columns, rows}).
-record (db_database, {id, name, tables}).

