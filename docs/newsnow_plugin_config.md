# get_news_from_newsnow Plugin News Source Configuration Guide

## Overview

The `get_news_from_newsnow` plugin now supports dynamic configuration of news sources via the web management interface, and no longer requires code changes. Users can configure different news sources for each agent in the control console.

## Configuration methods

### 1. Configure via the web management interface (recommended)

1. Log in to the control console.
2. Go to the "Role Configuration" page.
3. Select the agent to configure.
4. Click the "Edit Features" button.
5. In the parameter configuration area on the right, find the "newsnow news aggregation" plugin.
6. In the "News source configuration" field, enter semicolon-separated Chinese names.

### 2. Configuration file method

Configure in `config.yaml`:

```yaml
plugins:
  get_news_from_newsnow:
    url: "https://newsnow.busiyi.world/api/s?id="
    news_sources: "澎湃新闻;百度热搜;财联社;微博;抖音"
```

## News source configuration format

The news source configuration uses semicolon-separated Chinese names, in the format:

```
Chinese name 1;Chinese name 2;Chinese name 3
```

### Configuration example

```
澎湃新闻;百度热搜;财联社;微博;抖音;知乎;36氪
```

## Supported news sources

The plugin supports the following Chinese-named news sources:

- 澎湃新闻 (The Paper)
- 百度热搜 (Baidu Hot Search)
- 财联社 (Cailian Press)
- 微博 (Weibo)
- 抖音 (Douyin)
- 知乎 (Zhihu)
- 36氪 (36Kr)
- 华尔街见闻 (Wall Street CN)
- IT之家 (IThome)
- 今日头条 (Toutiao)
- 虎扑 (Hupu)
- 哔哩哔哩 (Bilibili)
- 快手 (Kuaishou)
- 雪球 (Xueqiu)
- 格隆汇 (Gelonghui)
- 法布财经 (Fastbull)
- 金十数据 (Jin10)
- 牛客 (Nowcoder)
- 少数派 (Sspai)
- 稀土掘金 (Juejin)
- 凤凰网 (IFeng)
- 虫部落 (Chongbuluo)
- 联合早报 (Zaobao)
- 酷安 (Coolapk)
- 远景论坛 (PCBeta)
- 参考消息 (Cankaoxiaoxi)
- 卫星通讯社 (Sputnik News)
- 百度贴吧 (Baidu Tieba)
- 靠谱新闻 (Kaopu News)
- And more...

## Default configuration

If no news source is configured, the plugin will use the following default:

```
澎湃新闻;百度热搜;财联社
```

## Usage

1. **Configure news sources**: Set the Chinese names of news sources in the web interface or configuration file, separated by semicolons.
2. **Invoke the plugin**: The user can say "play news" or "get the news".
3. **Specify a news source**: The user can say "play news from The Paper" or "get Baidu Hot Search".
4. **Get details**: The user can say "give me more details on this news".

## How it works

1. The plugin accepts a Chinese name as a parameter (e.g., "澎湃新闻").
2. Based on the configured news source list, it converts the Chinese name into the corresponding English ID (e.g., "thepaper").
3. It calls the API with the English ID to fetch news data.
4. It returns the news content to the user.

## Notes

1. Configured Chinese names must exactly match the names defined in `CHANNEL_MAP`.
2. After changing the configuration, restart the service or reload the configuration.
3. If a configured news source is invalid, the plugin will fall back to the default news sources.
4. Use the ASCII semicolon (`;`) between multiple news sources, not the full-width Chinese semicolon (`；`).
