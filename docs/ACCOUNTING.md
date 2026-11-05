# Accounting rules (Phase 3)

## Inventory costing: weighted average cost
On every purchase, the product's cost (shown as purchase price) becomes
`(existing stock x current cost + landed cost of new goods) / (existing stock + new quantity)`.
Landed cost = line amount + its share of extra expenses - its share of invoice discount
(shares are proportional to line amounts; the last line takes the rounding remainder).
Zero or negative existing stock counts as no value.

## Sales
- Sale total = sum of (quantity x price - item discount) - invoice discount.
- Cost of goods sold (COGS) = quantity x average cost at the moment of sale, stored on the sale.
- Gross profit = sale total - COGS. A later change of purchase price never changes old sales.

## Dashboard definitions
- Sales / Profit: selected date range. Receivables, payables, stock: current totals.
- Receivables = sum(total - paid) over sales. Payables = sum(total - paid) over purchases.
- Cash balance = money received from sales - money paid for purchases - expenses (opening cash comes later).
- Revenue, cash received, credit given and credit taken are kept separate in the data.

## Returns (Phase 3, part 2)
- Sales return value = quantity share of the line after its share of the invoice discount.
  Returning the last remaining quantity takes the exact remaining value, so rounding never adds up to more than the line.
- Stock goes back up; COGS of the returned goods is reversed at the ORIGINAL cost snapshot, so profit reverses correctly.
- Settlement: the return first cancels unpaid balance. Only money paid beyond what is still owed is refunded in cash
  (`refund = max(0, (paid - refunded so far) - (total - returned so far - this return))`).
- Purchase return value = share of the line's landed cost. Stock goes down (not allowed if already sold).
  The product's average cost is left unchanged.
- Dashboard: sales and profit are net of returns; receivable = sum(total - paid) - returns + refunds; same for payables.

## Customers, suppliers and ledger (Phase 4)
- Balance (customer) = opening + sum(sale total - paid at sale) - sum(return value - cash refund) - payments received.
  Supplier balance is the same with purchases and payments made. Positive = we receive / we pay; negative = advance.
- Ledger lines: BILL raises the balance, PAID WITH BILL lowers it, RETURN lowers it, REFUND (cash given back) raises it,
  PAYMENT lowers it. The running balance is computed in order of time.
- Payments on account are not tied to one invoice; invoice screens keep showing the remaining of that invoice only.
- Dashboard: receivables/payables include opening balances and are reduced by payments on account;
  cash balance also adds payments received and subtracts payments made.

## Not yet included
Opening cash balance, payment allocation to individual invoices.

## Expenses (Phase 5)
- An expense is a running cost of the shop (rent, salary, bills ...). It is NOT part of the cost of goods sold.
- Net profit (dashboard) = gross profit (sales - COGS, net of returns) - expenses dated inside the selected range.
- The Expenses card shows the expenses of the selected range. Cash balance subtracts ALL expenses, whatever their date.
- Expenses are grouped by a fixed category list so Reports can total them reliably.

## Reports (Phase 5)
- Same definitions as the dashboard: sales = invoices after discounts minus sales returns of the period;
  gross profit = sales - cost of goods (returned goods give their cost back); net profit = gross profit - expenses.
- A sale or return belongs to the day it was made (device time zone), an expense to the date chosen on it.
- Ranges up to 31 days are shown per day, longer ones per month.
- Best selling products: ranked by item sales amount (before the invoice discount and returns); the quantity is reduced by what was returned in the same period.
