import os
import mysql.connector
from mysql.connector import Error

path_pdptw = r"D:\Logistic\data\pdp_100"
path_vrptw = r"D:\Logistic\data\In"

def read_file(file_path, start_line=0):
    try:
        with open(file_path, 'r') as f:
            lines = f.readlines()[start_line:]  # Start reading from specific line
        return lines
    except Exception as e:
        print(f"Error reading file '{file_path}': {e}")
        return []

def insert_algorithm_data(cursor, count, algorithm, k, q):
    cursor.execute(
        "INSERT INTO algorithm(id, k, q, algorithm) VALUES(%s, %s, %s, %s)",
        (count, k, q, algorithm)
    )

def insert_location_data(cursor, count, data, table_name):
    insert_query = ""
    if table_name == 'pdptw':
        insert_query = """
        INSERT INTO location_pdptw(id, task, x, y, demand, earliest_time, lastest_time, service_time, pickup, delivery)
        VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
    elif table_name == 'vrptw':
        insert_query = """
        INSERT INTO location_vrptw(id, cust, xcord, ycord, demand, ready_time, due_time, service_time)
        VALUES(%s, %s, %s, %s, %s, %s, %s, %s)
        """
    
    cursor.executemany(insert_query, data)

def read_pdptw(path, connection, algorithm):
    if not os.path.exists(path):
        print(f"Path '{path}' does not exist.")
        return

    files = os.listdir(path)
    cursor = connection.cursor()

    cursor.execute("SELECT COUNT(*) FROM algorithm")
    result = cursor.fetchone()
    count = int(result[0]) if result[0] else 0

    for file in files:
        if file.endswith('.txt'):
            lines = read_file(os.path.join(path, file))
            if lines:
                info = lines[0].split()  # First line: `k` and `q`
                try:
                    count += 1
                    insert_algorithm_data(cursor, count, algorithm, info[0], info[1])

                    data = []
                    for line in lines[1:]:
                        line = line.split()
                        if len(line) >= 9:
                            data.append((count, *line[:9]))
                    insert_location_data(cursor, count, data, 'pdptw')

                except mysql.connector.Error as err:
                    print(f"Failed to insert into database for file '{file}': {err}")
                    
    connection.commit()

def read_vrptw(path, connection, algorithm):
    if not os.path.exists(path):
        print(f"Path '{path}' does not exist.")
        return

    files = os.listdir(path)
    cursor = connection.cursor()

    cursor.execute("SELECT COUNT(*) FROM algorithm")
    result = cursor.fetchone()
    count = int(result[0]) if result[0] else 0

    for file in files:
        if file.endswith('.txt'):
            lines = read_file(os.path.join(path, file), start_line=9)
            if lines:
                info = lines[0].split()  # First line: `k` and `q`
                try:
                    count += 1
                    insert_algorithm_data(cursor, count, algorithm, info[0], info[1])

                    data = []
                    for line in lines:
                        line = line.split()
                        if len(line) >= 7:
                            data.append((count, *line[:7]))
                    insert_location_data(cursor, count, data, 'vrptw')

                except mysql.connector.Error as err:
                    print(f"Failed to insert into database for file '{file}': {err}")

    connection.commit()

def connect_to_mysql():
    config = {
        'user': 'root',
        'password': '',
        'host': '127.0.0.1',
        'database': 'logistic',
        'raise_on_warnings': True
    }
    try:
        connection = mysql.connector.connect(**config)
        if connection.is_connected():
            print("Kết nối thành công!")
            return connection
    except Error as e:
        print(f"Lỗi kết nối: {e}")
        return None

# Open the connection once and reuse it
connection = connect_to_mysql()

if connection:
    try:
        read_pdptw(path_pdptw, connection, 'pdptw')  # insert data pdptw to database
        read_vrptw(path_vrptw, connection, 'vrptw')  # insert data vrptw to database
    finally:
        if connection.is_connected():
            connection.close()
