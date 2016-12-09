Assets are recognised as strings in HTML ending with extensions:
	.jpg
	.png
	.css
	.js
	.ico
	.gif
	.flv
	
The list could be easily adjusted by adding additional extensions to the WebCrawler.ASSET_REGEX constant.

Given a URL visits every reachable page under the domain. For each page, it returns the URLs of every
static asset (images, javascript, stylesheets) on that page.

The crawler ouputs to STDOUT in JSON format listing the URLs of every static asset, grouped by page.
