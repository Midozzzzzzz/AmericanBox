# American Box

A simple Arabic storefront for imported supplements and vitamins. The repository contains a single `index.html` page styled with basic CSS. You can open the file directly in any modern browser.

Product images currently use placeholder links hosted on GitHub. Replace these URLs with your own images or place your files in an `img/` directory and update the paths accordingly.

To view the page locally:

```bash
# clone the repository
# then open index.html
open index.html
```

The archive of `nanorc` syntax files has been removed since it was unrelated to the project.

## كود بايثون لسحب بيانات صفحة ويب

يمكنك نسخ الكود التالي في بايثون لسحب عنوان الصفحة وروابط الصور من أي موقع (مع الالتزام بسياسة الاستخدام والـ robots.txt للموقع):

```python
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin


def fetch_page_data(url):
    if not url.startswith(("http://", "https://")):
        url = f"https://{url}"

    response = requests.get(
        url,
        headers={"User-Agent": "Mozilla/5.0 (compatible; DataFetcher/1.0)"},
        timeout=15,
    )
    response.raise_for_status()

    soup = BeautifulSoup(response.text, "html.parser")
    title = soup.title.text.strip() if soup.title else "بدون عنوان"

    image_links = []
    for img in soup.select("img[src]"):
        image_links.append(urljoin(url, img["src"]))

    return title, image_links


if __name__ == "__main__":
    page_url = input("ادخل الرابط: ").strip()
    page_title, images = fetch_page_data(page_url)

    print(f"\nعنوان الصفحة: {page_title}")
    print("روابط الصور:")
    for link in images:
        print(f"- {link}")
```

> ملاحظة: قد تحتاج لتثبيت المتطلبات عبر `pip install requests beautifulsoup4`.
