# Barcodes

## Scanning
- The camera (CameraX) and Google ML Kit read the barcode on the phone itself, so no internet is needed.
- Formats read: EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39 and QR.
- Sale invoice: every new code adds one unit of the matching product. A code that stays in view is added only once;
  look away for about 1.5 seconds (or show another code) to add another unit.
- The match is on the product's Barcode field (active products only). A 12-digit UPC-A and the same code with a
  leading 0 (EAN-13) are treated as the same barcode.

## Printing
- Product details -> Print barcodes -> number of barcodes -> Share PDF or Print.
- Sheet: A4, 5 x 13 = 65 labels of 38.1 x 21.2 mm (the common "65 per sheet" sticker layout; also cuts neatly from plain paper).
- Bars are drawn as vector rectangles, so they stay sharp at any printer resolution. Nominal bar width 0.33 mm;
  longer codes are squeezed to fit the label.
- Retail numbers (valid EAN-13, EAN-8, UPC-A) keep their own symbology. Any other text is printed as Code 128.
- A product with no barcode gets a shop EAN-13: `2` + 11-digit serial (product id) + check digit. GS1 reserves prefixes
  20-29 for in-store use, so it cannot clash with a manufacturer's barcode. It is saved on the product.
- Print at 100% / "actual size" (not "fit to page"), otherwise the labels will not line up with the stickers.
