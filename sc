"""
Generic Lead Scraper
====================

This module provides a simple example for collecting customer lead
information from a product page and exporting it into an Excel file.

**Disclaimer:**

- Scraping or collecting personally identifiable information from
  websites without explicit permission may violate the terms of
  service of those sites and local data‑protection laws. You should
  only run this script against pages you own or have permission to
  scrape, and you must comply with all applicable privacy regulations.

- The HTML structure of a page varies from site to site. Before
  running this script you should inspect the page you want to
  collect leads from and adjust the CSS selectors accordingly.

The example uses requests and BeautifulSoup for static pages. For
dynamic sites that load content via JavaScript (e.g. Facebook or
other social media platforms) you may need to use Selenium or an
official API instead. See the ``scrape_dynamic`` function for a
starting point.
"""

from __future__ import annotations

import csv
import os
from typing import Dict, List, Optional

import pandas as pd
import requests
from bs4 import BeautifulSoup


def scrape_static(url: str, selectors: Optional[Dict[str, str]] = None) -> List[Dict[str, str]]:
    """Scrape leads from a static HTML page.

    Parameters
    ----------
    url: str
        The URL of the page containing customer lead information. The page
        should be static; if it relies on client‑side JavaScript to
        populate data, consider using a different function.

    selectors: dict[str, str], optional
        A mapping of field names to CSS selectors used to extract
        information for each lead. The default selectors assume each lead
        resides in an element with the class ``lead-card`` and that
        ``.name``, ``.email`` and ``.phone`` classes hold the
        corresponding values. Adjust these selectors to match your
        specific page.

    Returns
    -------
    list of dict
        A list of dictionaries containing extracted lead data. Keys are
        derived from the ``selectors`` mapping.
    """

    # Default CSS selectors for demonstration purposes.
    default_selectors = {
        "container": ".lead-card",
        "name": ".name",
        "email": ".email",
        "phone": ".phone",
    }
    selectors = selectors or default_selectors

    response = requests.get(url, timeout=30)
    response.raise_for_status()  # Will raise if the request fails
    soup = BeautifulSoup(response.content, "html.parser")

    lead_data: List[Dict[str, str]] = []
    # Find all lead containers on the page.
    for container in soup.select(selectors["container"]):
        record: Dict[str, str] = {}
        for field, selector in selectors.items():
            if field == "container":
                # Skip container key; not a data field
                continue
            element = container.select_one(selector)
            record[field] = element.get_text(strip=True) if element else ""
        lead_data.append(record)

    return lead_data


def save_to_excel(records: List[Dict[str, str]], filename: str) -> None:
    """Save a list of lead dictionaries into an Excel file.

    Parameters
    ----------
    records: list of dict
        The data to save; each dict should have the same keys so the
        resulting spreadsheet columns are consistent.

    filename: str
        Target path for the Excel file. If no directory is given, the
        file will be created in the current working directory.
    """
    if not records:
        raise ValueError("No lead data to save")
    df = pd.DataFrame(records)
    # Make sure the parent directory exists
    os.makedirs(os.path.dirname(filename) or ".", exist_ok=True)
    df.to_excel(filename, index=False)


def scrape_dynamic(url: str, output_file: str, wait_for_element: str) -> None:
    """Example function demonstrating how to scrape leads from a dynamic site.

    This uses Selenium WebDriver to load a page that requires
    client‑side JavaScript and then extracts lead information. You
    must have the appropriate WebDriver executable installed (e.g.
    chromedriver) and the ``selenium`` Python package. Because
    dynamic scraping is highly dependent on the page structure,
    this function is left as a placeholder with explanatory comments.

    Parameters
    ----------
    url: str
        The URL of the site to scrape.

    output_file: str
        Path where the Excel file should be saved.

    wait_for_element: str
        A CSS selector used to wait until a particular element is
        present, indicating that the page has loaded the data you need.
    """
    # NOTE: To use this function, install selenium via pip and
    # download a WebDriver (e.g. for Chrome: https://sites.google.com/a/chromium.org/chromedriver/).
    # Example usage (uncomment after installing selenium and chromedriver):
    #
    # from selenium import webdriver
    # from selenium.webdriver.common.by import By
    # from selenium.webdriver.support.ui import WebDriverWait
    # from selenium.webdriver.support import expected_conditions as EC
    # driver = webdriver.Chrome()
    # driver.get(url)
    # # Wait until a specific element loads
    # WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.CSS_SELECTOR, wait_for_element)))
    # page_source = driver.page_source
    # driver.quit()
    # soup = BeautifulSoup(page_source, "html.parser")
    # # Then parse the leads similar to scrape_static, adjusting selectors.
    # lead_data = []
    # ...
    # save_to_excel(lead_data, output_file)
    raise NotImplementedError(
        "scrape_dynamic is a placeholder. Install selenium and implement the scraping logic as appropriate."
    )


if __name__ == "__main__":
    # Example usage of the static scraper. This block is purely for
    # demonstration and should be adjusted or removed when integrating
    # into production code.
    example_url = "https://example.com/leads.html"
    try:
        leads = scrape_static(example_url)
        print(f"Collected {len(leads)} leads from {example_url}")
        # Save results into an Excel file in the current directory
        output_path = "leads.xlsx"
        save_to_excel(leads, output_path)
        print(f"Lead information exported to {output_path}")
    except Exception as err:
        print(f"Error occurred: {err}")