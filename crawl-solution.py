import os
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin

def crawl():
    url = "https://www.sintef.no/projectweb/top/pdptw/100-customers/" # Thay đổi URL ở đây
    folder = r"D:\Logistic\data\In\solution" # Thay đổi đường dẫn thư mục ở đây

    os.makedirs(folder, exist_ok=True)  # Tạo thư mục nếu chưa có

    response = requests.get(url)
    response.raise_for_status()

    soup = BeautifulSoup(response.text, 'html.parser')

    for link in soup.find_all('a'):
        href = link.get('href')
        if href and "contentassets" in href and href.endswith(".txt"):
            full_url = urljoin(url, href)  # Tự động ghép nối đúng URL

            file_name = os.path.basename(href.rstrip('/'))  # Xóa dấu '/' cuối nếu có
            file_path = os.path.join(folder, file_name)

            file_response = requests.get(full_url)
            file_response.raise_for_status()

            with open(file_path, "wb") as file:
                file.write(file_response.content)

            print(f"Đã tải: {file_name}")

if __name__ == "__main__":
    crawl()
