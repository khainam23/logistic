import os
import mysql.connector
from mysql.connector import Error

DB_CONFIG = {
    'user': 'root',
    'password': '',
    'host': '127.0.0.1',
    'database': 'logistic',
    'raise_on_warnings': True
}

DATA_PATHS = {
    'pdptw': r"D:\Logistic\data\pdp_100",
    'vrptw': r"D:\Logistic\data\In",
    'vprspdtw': "No data" 
}

TABLE_SCHEMAS = {
    'pdptw': {
        'columns': 9,
        'query': """
            INSERT INTO location_pdptw(id, task, x, y, demand, earliest_time, lastest_time, service_time, pickup, delivery)
            VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
    },
    'vrptw': {
        'columns': 7,
        'query': """
            INSERT INTO location_vrptw(id, cust, xcord, ycord, demand, ready_time, due_time, service_time)
            VALUES(%s, %s, %s, %s, %s, %s, %s, %s)
        """
    },
    'vrpspdtw': {
        'columns': 7,
        'query': """
            INSERT INTO location_vrptw(id, cust, xcord, ycord, demand, ready_time, due_time, service_time)
            VALUES(%s, %s, %s, %s, %s, %s, %s, %s)
        """
    }
}

def connect_to_mysql():
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print("Kết nối thành công!")
            return connection
    except Error as e:
        print(f"Lỗi kết nối: {e}")
    return None

def read_file(file_path, start_line=0):
    try:
        with open(file_path, 'r') as f:
            return f.readlines()[start_line:]
    except Exception as e:
        print(f"Lỗi đọc file '{file_path}': {e}")
    return []

def insert_algorithm_data(cursor, count, algorithm, k, q):
    cursor.execute("INSERT INTO algorithm(id, k, q, algorithm) VALUES(%s, %s, %s, %s)", (count, k, q, algorithm))

def insert_location_data(cursor, count, data, table_name):
    if table_name in TABLE_SCHEMAS:
        cursor.executemany(TABLE_SCHEMAS[table_name]['query'], data)

def get_record_count(cursor):
    cursor.execute("SELECT COUNT(*) FROM algorithm")
    return cursor.fetchone()[0] or 0

def read_data(path, connection, algorithm):
    if not os.path.exists(path):
        print(f"Đường dẫn '{path}' không tồn tại.")
        return

    cursor = connection.cursor()
    count = get_record_count(cursor)
    files = [f for f in os.listdir(path) if f.endswith('.txt')]

    for file in files:
        lines = read_file(os.path.join(path, file), start_line=9 if algorithm == 'vrptw' else 0)
        if not lines:
            continue

        info = lines[0].split()
        if len(info) < 2:
            continue

        try:
            count += 1
            insert_algorithm_data(cursor, count, algorithm, info[0], info[1])
            
            data = [
                (count, *line.split()[:TABLE_SCHEMAS[algorithm]['columns']])
                for line in lines[1:] if len(line.split()) >= TABLE_SCHEMAS[algorithm]['columns']
            ]
            insert_location_data(cursor, count, data, algorithm)
        except mysql.connector.Error as err:
            print(f"Lỗi khi chèn dữ liệu từ '{file}': {err}")

    connection.commit()
    cursor.close()

if __name__ == "__main__":
    connection = connect_to_mysql()
    if connection:
        try:
            for algo, path in DATA_PATHS.items():
                read_data(path, connection, algo)
        finally:
            connection.close()
