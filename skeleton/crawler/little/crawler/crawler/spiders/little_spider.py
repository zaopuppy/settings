#!/usr/bin/env python

import scrapy
from scrapy.http.response.html import HtmlResponse
from scrapy.spiders import CrawlSpider

class LittleSpider(scrapy.Spider):
    name = "little"
    allowed_domains = ["www.vim.org"]
    start_urls = [
            "http://www.vim.org/",
            ]
    def parse(self, response):
        print('type: {}'.format(type(response)))
        print(response.url)
        if not isinstance(response, HtmlResponse):
            return
        for href in response.xpath('//a/@href'):
            url = response.urljoin(href.extract())
            print('url: {}'.format(url))
            yield scrapy.Request(url, callback=self.parse)


