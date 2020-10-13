import scrapy
from urllib.parse import urljoin


class BookItem(scrapy.Item):
    # define the fields for your item here like:
    title = scrapy.Field()
    author = scrapy.Field()
    price = scrapy.Field()
    year = scrapy.Field()
    count_page = scrapy.Field()
    publisher = scrapy.Field()


class ReaderTownSpider(scrapy.Spider):
    name = "readertown"
    start_urls = [
        'https://www.chitai-gorod.ru/catalog/books/nauka_i_tekhnika-9170/',
    ]
    visited_urls = []

    def parse(self, response):
        if response.url not in self.visited_urls:
            self.visited_urls.append(response.url)

            for post_link in response.xpath(
                    '//div[@class="product-card js_product js__product_card js__slider_item"]/div/a/@href').extract():
                url = urljoin(response.url, post_link)
                yield response.follow(url, callback=self.parse_post)

            next_pages = response.xpath(
                    '//div[@class="pagination"]/a[contains(@id, "navigation")]/@href').extract()
            next_page = next_pages[-1]

            next_page_url = urljoin(response.url + '/', next_page)
            yield response.follow(next_page_url, callback=self.parse)

    def parse_post(self, response):
        item = BookItem()
        title = response.xpath('//h1[contains(@class, "product__title")]/text()').extract()
        item['title'] = title[0].replace('\\n', '').replace(r'\n', '').strip() if title else None
        author = response.xpath('//a[@class="link product__author"]/text()').extract()
        item['author'] = author[0].replace('\n', '').strip() if author else None
        xpath = '//div[@class="product__props"]//div[@class="product-prop__title" and text() = "Кол-во страниц"]' + \
                '/../div[2]/text()'
        count_page = response.xpath(xpath).extract()
        item['count_page'] = int(count_page[0]) if count_page else None
        price = response.xpath('//div[@class="price-block x-label"]/div[1]/div/text()').extract()
        item['price'] = int(price[0][:len(price) - 3]) if price else None
        xpath = '//div[@class="product__props"]//div[@class="product-prop__title" and text() = "Издательство"]' + \
                '/../div[2]/a/text()'
        publisher = response.xpath(xpath).extract()
        item['publisher'] = publisher[0] if publisher else None
        xpath = '//div[@class="product__props"]//div[@class="product-prop__title" and text() = "Год издания"]' + \
                '/../div[2]/text()'
        year = response.xpath(xpath).extract()
        item['year'] = int(year[0]) if year else None
        yield item