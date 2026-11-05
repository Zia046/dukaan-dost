# Changelog

## 0.8.0 - Secret Code (price decoder)
- The "Secret Code" menu now holds the Burhan Nexus price decoder: Code -> Price and Price -> Code
- The letter for each digit can be changed (one letter per digit, no repeats); it is saved on the phone. Default code: AEIOUSBLWP
- Fixed footer "Developed by Burhan Nexus" with the phone number, which cannot be edited
- Shop name, address and logo are not copied from the old app: Dukaan Dost already has them in Settings
- Unit tests for the code logic

## 0.7.0 - Barcode scanning and printing
- Sale invoice: new "Scan barcode" button next to "Search products". The phone camera scans and the product is added to the invoice straight away; the camera stays open for the next item, with a beep and a message for each scan
- A product that is not found, or has no stock, is reported on the scan screen instead of being added
- Product form: "Scan barcode with camera" fills the barcode field
- Product details: "Print barcodes" asks how many barcodes are needed and makes an A4 PDF with 65 small labels per page (5 x 13), each with the product name, the barcode and its number. Share it or print it directly. Cut lines can be switched off
- Retail codes (EAN-13, EAN-8, UPC-A) are printed in their own symbology and other codes (such as a phone IMEI) as Code 128. A product without a barcode gets a shop EAN-13 (prefix 2, reserved for in-store use) that is saved on the product
- Scanning also finds a product saved as UPC-A when the camera reads it as EAN-13, and the other way round
- Needs the camera permission (asked on first use); everything else works without it. No database change (still version 5)
- Unit tests for barcode rules and the label sheet layout

## 0.6.0 - Phase 5: Reports
- Reports menu is now a real screen (it was empty)
- Period filter: Today, Yesterday, This week, This month, This year or a custom range
- Summary: invoices, sales (after discounts and returns), returns, gross profit, expenses, net profit - same definitions as the dashboard
- Bar chart of sales and expenses: one bar pair per day (up to 31 days) or per month, with a table of the active days/months
- Expenses by category with share of the total
- Best selling products (top 5 by item sales, quantity net of returns)
- Expenses and Reports share one period filter
- No database change (still version 5). Unit tests for the report calculations

## 0.5.0 - Phase 5: Expenses
- Expenses menu is now a real screen: add, edit and delete shop expenses
- Fields: what it was for, amount, category (rent, salary, electricity and bills, transport, food and tea, repair, marketing, other), paid by cash/bank/wallet, date, note
- Filter by Today, Yesterday, This week, This month, This year or a custom range; total for the period
- Dashboard: Expenses card now shows real figures; the profit card is NET profit (gross profit minus expenses of the period)
- Dashboard cash balance now also subtracts expenses
- Database v5 with migration 4 -> 5 (expenses table). Existing data is untouched
- Unit tests: expense validation

## 0.4.0 - Phase 4: Customers, Suppliers, Ledger
- New menu entries: Return items, Customers, Suppliers, Ledger
- Return items menu: all sales and purchase returns in one list, start a new return against any invoice
- Customers and Suppliers: list with balances, search, add, edit (name, phone, address, opening balance)
- Ledger: every bill, payment-with-bill, return, refund and payment on account with a running balance
- Receive payment from a customer / make payment to a supplier on account
- Statement PDF (logo, shop details) with share and print
- Opening balance is locked once the party has transactions (history is never rewritten silently)
- Database v4 with migration 3 -> 4 (customer_payments, supplier_payments)
- Dashboard receivables, payables and cash balance include opening balances and payments on account

## 0.3.2 - Shop profile and PDF receipts
- First run asks for shop name, mobile number, address, owner, receipt message and logo (DataStore + private logo file)
- Shop details editable later in Settings
- PDF receipt (A5) with logo, shop name, address and phone on top; multi-page; Share PDF and Print
- Text share also starts with the shop details
- Shop name shown in the toolbar business menu

## 0.3.1 - Phase 3 (part 2): Returns
- Database v3 with migration 2 -> 3 (sale_returns, sale_return_items, purchase_returns, purchase_return_items)
- Sales return: stock goes back up, receivable falls first, only the overpayment is refunded in cash
- Purchase return: stock goes down (blocked if the goods were already sold), payable falls first, excess refunded by the supplier
- Return value includes the invoice-discount share; repeated partial returns never exceed the line value
- Invoice screens show returns, net remaining balance and a "Return items" button
- Dashboard sales, profit, receivables, payables and cash balance are net of returns and refunds
- Stock history shows SR-/PR- return numbers

## 0.3.0 - Phase 3 (part 1): Purchases and Sales / POS
- Database v2 with migration 1 -> 2 (suppliers, customers, purchases, sales and their items)
- Purchases: multi-item invoice, item and invoice discount, extra expenses, cash/partial/credit, supplier quick-add
- Sales / POS: cart, quantity +/-, item and invoice discount, cash/partial/credit, customer quick-add
- Stock rises on purchase and falls on sale in one database transaction; stock history records both
- Weighted average cost (see docs/ACCOUNTING.md); cost of goods stored per sale
- Duplicate-sale protection (unique checkout token), no selling above available stock
- Sequential invoice numbers (S-000001, P-000001), history lists, invoice view, text share (WhatsApp etc.)
- Dashboard: sales, profit, receivables, payables, cash balance and recent invoices from real records
- Unit tests: calculators, weighted average, invoice text

## 0.2.0 - Phase 2: Database and Products
- Room database (SQLite) with foreign keys, exported schema, migration scaffold
- Entities: categories, products, stock movements
- Money stored as paisa (Long), quantities as thousandths (Long); BigDecimal in the app
- Product CRUD: add, edit, detail, delete (archive; history kept)
- Categories: add, rename, delete (products kept)
- Search (name/SKU/barcode), category filter, sorting
- Stock value and expected profit; stock history (opening stock, adjustments)
- Dashboard product figures now come from the database
- Unit tests: validator, minor-unit conversions

## 0.1.0 - Phase 1: Project Foundation
- Android Studio project, Gradle Kotlin DSL, version catalog
- Material 3 theme (light/dark), adaptive app icon, splash screen
- Navigation drawer, top toolbar, Navigation Compose graph
- Dashboard UI with date filters
- Fixed developer information (Burhan Nexus) in footer and About
