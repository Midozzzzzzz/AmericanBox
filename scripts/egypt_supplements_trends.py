#!/usr/bin/env python3
"""Fetch trending supplement-related searches in Egypt and plot interest over time."""

from __future__ import annotations

import argparse
from pathlib import Path

import matplotlib
import pandas as pd
from pytrends.request import TrendReq

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Find top supplement-related searches in Egypt and plot trends."
    )
    parser.add_argument(
        "--seed",
        default="مكملات غذائية",
        help="Seed keyword to discover related supplement searches (default: مكملات غذائية)",
    )
    parser.add_argument(
        "--geo",
        default="EG",
        help="Geo code for Google Trends (default: EG)",
    )
    parser.add_argument(
        "--timeframe",
        default="now 7-d",
        help="Timeframe for trends (default: now 7-d)",
    )
    parser.add_argument(
        "--top",
        type=int,
        default=5,
        help="Number of top related searches to include (default: 5)",
    )
    parser.add_argument(
        "--output-dir",
        default="output",
        help="Directory to save CSV and chart (default: output)",
    )
    return parser.parse_args()


def fetch_top_queries(
    pytrends: TrendReq, seed: str, geo: str, timeframe: str, top: int
) -> list[str]:
    pytrends.build_payload([seed], geo=geo, timeframe=timeframe)
    related = pytrends.related_queries().get(seed)
    if not related or related.get("top") is None:
        raise RuntimeError("No related queries found. Try a different seed keyword.")
    top_queries = related["top"].head(top)["query"].tolist()
    if not top_queries:
        raise RuntimeError("Related queries list is empty.")
    return top_queries


def fetch_interest_over_time(
    pytrends: TrendReq, keywords: list[str], geo: str, timeframe: str
) -> pd.DataFrame:
    pytrends.build_payload(keywords, geo=geo, timeframe=timeframe)
    data = pytrends.interest_over_time()
    if data.empty:
        raise RuntimeError("No interest over time data returned.")
    return data.drop(columns=["isPartial"], errors="ignore")


def plot_trends(data: pd.DataFrame, output_path: Path) -> None:
    plt.figure(figsize=(10, 6))
    for column in data.columns:
        plt.plot(data.index, data[column], label=column)
    plt.title("Trending supplement searches in Egypt")
    plt.xlabel("Date")
    plt.ylabel("Interest")
    plt.legend(loc="upper left")
    plt.tight_layout()
    plt.savefig(output_path)


def main() -> None:
    args = parse_args()
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    pytrends = TrendReq(hl="ar-EG", tz=120)
    top_queries = fetch_top_queries(
        pytrends, args.seed, args.geo, args.timeframe, args.top
    )
    data = fetch_interest_over_time(pytrends, top_queries, args.geo, args.timeframe)

    csv_path = output_dir / "egypt_supplements_trends.csv"
    chart_path = output_dir / "egypt_supplements_trends.png"

    data.to_csv(csv_path, encoding="utf-8")
    plot_trends(data, chart_path)

    print("Top related searches:")
    for query in top_queries:
        print(f"- {query}")
    print(f"Saved CSV: {csv_path}")
    print(f"Saved chart: {chart_path}")


if __name__ == "__main__":
    main()
