#!/usr/bin/env python


from scrapy.http.response.html import HtmlResponse
from scrapy.linkextractors.lxmlhtml import LxmlLinkExtractor
from scrapy.spiders import CrawlSpider, Rule

from crawler.items import LittleItem


class LittleSpider(CrawlSpider):
    name = 'little'
    allowed_domains = ['www.vim.org']
    start_urls = [
            'http://www.vim.org/',
            ]

    rules = (
            Rule(LxmlLinkExtractor(restrict_xpaths=('//a',)),
                callback="parse_items",
                follow=True),
            )

    def parse_items(self, response):
        if not isinstance(response, HtmlResponse):
            return
        item = LittleItem()
        item['link'] = response.url
        return [item]
        # item_list = []
        # for href in response.xpath('//a/@href'):
        #     url = response.urljoin(href.extract())
        #     # print(url)
        #     item = LittleItem()
        #     item['link'] = url
        #     item_list.append(item)
        # return item_list

# class LittleSpider(scrapy.Spider):
#     name = "little"
#     allowed_domains = ["www.vim.org"]
#     start_urls = [
#             "http://www.vim.org/",
#             ]
#     def parse(self, response):
#         # item = LittleItem()
#         # item['link'] = response.url
#         # yield item
#         if not isinstance(response, HtmlResponse):
#             return
#         for href in response.xpath('//a/@href'):
#             url = response.urljoin(href.extract())
#             item = LittleItem()
#             item['link'] = url
#             yield item
#             yield scrapy.Request(url, callback=self.parse)
# 


