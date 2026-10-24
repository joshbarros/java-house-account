#!/usr/bin/env python3
"""Drive a 13-service house account through shipment, compensation, and overlap."""

import base64
import json
import sys
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor

PORTS = {
    "party": 8161,
    "catalog": 8162,
    "price": 8163,
    "order": 8164,
    "credit": 8165,
    "tax": 8166,
    "stock": 8167,
    "invoice": 8168,
    "ledger": 8169,
    "fulfillment": 8170,
    "shipment": 8171,
    "notify": 8172,
    "timeline": 8173,
}
MGMT = "http://localhost:15681"
AUTH = "platform:platform"
failures = []


def check(name, ok, detail=""):
    if ok:
        print(f"PASS  {name}")
    else:
        print(f"FAIL  {name} {detail}")
        failures.append(f"{name}: {detail}")


def call(method, url, body=None, auth=None, timeout=20):
    data = None if body is None else json.dumps(body).encode()
    request = urllib.request.Request(url, data=data, method=method)
    if data is not None:
        request.add_header("content-type", "application/json")
    if auth:
        request.add_header("Authorization", "Basic " + base64.b64encode(auth.encode()).decode())
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw = response.read().decode()
            return response.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as ex:
        raw = ex.read().decode()
        try:
            parsed = json.loads(raw) if raw else None
        except json.JSONDecodeError:
            parsed = raw
        return ex.code, parsed
    except Exception as ex:
        return 0, str(ex)


def wait_ok(url, seconds=300, auth=None):
    deadline = time.time() + seconds
    while time.time() < deadline:
        if call("GET", url, auth=auth)[0] == 200:
            return True
        time.sleep(1)
    return False


def until(label, fn, seconds=70):
    deadline = time.time() + seconds
    last = None
    while time.time() < deadline:
        last = fn()
        if last is not None:
            return last
        time.sleep(0.4)
    check(label, False, f"timed out last={last!r}")
    return None


def depth(name):
    status, body = call("GET", f"{MGMT}/api/queues/%2F/{name}", auth=AUTH)
    if status != 200 or not isinstance(body, dict):
        return None
    return body.get("messages", 0)


def grew(name, before, amount=1):
    def ready():
        now = depth(name)
        if now is not None and now >= before + amount:
            return now
        return None
    return until(f"dlq {name}", ready, 30)


def base(name):
    return f"http://localhost:{PORTS[name]}"


def customer(limit, email):
    status, body = call("POST", f"{base('party')}/customers",
                        {"name": "Ada", "email": email, "creditLimitCents": limit})
    check(f"customer {email}", status == 201, body)
    if status != 201:
        return None
    cid = body["id"]

    def directory():
        code, row = call("GET", f"{base('order')}/directory/{cid}")
        return row if code == 200 else None

    def account():
        code, row = call("GET", f"{base('credit')}/accounts/{cid}")
        return row if code == 200 else None

    check(f"directory {email}", until("directory", directory, 40) is not None)
    check(f"credit account {email}", until("account", account, 40) is not None)
    return cid


def sellable(warehouse, on_hand, price=2500):
    sku = "SKU-" + uuid.uuid4().hex[:10]
    status, body = call("POST", f"{base('catalog')}/skus", {"sku": sku, "warehouseCode": warehouse})
    check(f"catalog {sku}", status == 201, body)
    status, body = call("POST", f"{base('price')}/prices", {"sku": sku, "unitPriceCents": price})
    check(f"price {sku}", status == 201, body)
    status, body = call("POST", f"{base('stock')}/items", {"sku": sku, "onHand": on_hand})
    check(f"stock {sku}", status == 201, body)
    return sku


def place(cid, sku, qty, region, token, request_id=None):
    return call("POST", f"{base('order')}/orders", {
        "requestId": request_id or str(uuid.uuid4()),
        "customerId": cid,
        "sku": sku,
        "quantity": qty,
        "region": region,
        "shipToken": token,
    })


def order(oid):
    code, body = call("GET", f"{base('order')}/orders/{oid}")
    return body if code == 200 and isinstance(body, dict) else None


def await_status(oid, status, reason=None, seconds=70):
    def ready():
        body = order(oid)
        if body and body.get("status") == status and (reason is None or body.get("reason") == reason):
            return body
        return None
    return until(status, ready, seconds)


def on_hand(sku):
    code, body = call("GET", f"{base('stock')}/items/{sku}")
    return body.get("onHand") if code == 200 and isinstance(body, dict) else None


def exposed(cid):
    code, body = call("GET", f"{base('credit')}/accounts/{cid}")
    return body.get("exposedCents") if code == 200 and isinstance(body, dict) else None


def invoice(oid):
    return call("GET", f"{base('invoice')}/invoices/{oid}")


def receivable(cid):
    return call("GET", f"{base('ledger')}/receivables/{cid}")


def timeline_has(oid, status):
    code, body = call("GET", f"{base('timeline')}/timelines/{oid}")
    if code != 200 or not isinstance(body, list):
        return False
    return any(item.get("status") == status for item in body)


def main():
    for name in PORTS:
        check(f"{name} health", wait_ok(f"{base(name)}/actuator/health"))
    check("rabbit health", wait_ok(f"{MGMT}/api/overview", auth=AUTH))

    status, _ = call("POST", f"{base('party')}/customers", {"name": "Ada", "email": "ada@shop.test", "creditLimitCents": -1})
    check("negative limit is 400", status == 400, status)
    status, _ = call("POST", f"{base('order')}/orders", {
        "requestId": "x", "customerId": "x", "sku": " ", "quantity": 0, "region": "SP", "shipToken": "DOCK",
    })
    check("blank order is 400", status == 400, status)
    missing = str(uuid.uuid4())
    status, _ = place(missing, "SKU", 1, "SP", "DOCK")
    check("unknown customer is 404", status == 404, status)

    dup = "DUP-" + uuid.uuid4().hex[:8]
    status, _ = call("POST", f"{base('catalog')}/skus", {"sku": dup, "warehouseCode": "CENTRAL"})
    status, _ = call("POST", f"{base('catalog')}/skus", {"sku": dup, "warehouseCode": "CENTRAL"})
    check("duplicate sku is 409", status == 409, status)

    buyer = customer(100000, "ada@shop.test")
    mug = sellable("CENTRAL", 10)
    request_id = str(uuid.uuid4())
    status, placed = place(buyer, mug, 2, "SP", "DOCK1", request_id)
    check("order accepted", status == 201 and isinstance(placed, dict) and placed.get("status") == "DRAFT", placed)
    if not isinstance(placed, dict) or "id" not in placed:
        print("\n== summary ==\norder create failed before the saga")
        sys.exit(1)
    oid = placed["id"]

    def shipped_ready():
        body = order(oid)
        if body and body.get("status") == "SHIPPED" and body.get("totalCents") == 5400 and body.get("noticeStatus") == "DELIVERED":
            return body
        return None

    shipped = until("happy ship", shipped_ready, 90)
    check("happy path ships", shipped is not None, shipped)
    check("shelf dropped by two", on_hand(mug) == 8, on_hand(mug))
    check("credit stayed exposed", exposed(buyer) == 5400, exposed(buyer))
    code, bill = invoice(oid)
    check("invoice issued", code == 200 and bill.get("status") == "ISSUED" and bill.get("totalCents") == 5400, bill)
    code, book = receivable(buyer)
    check("receivable posted", code == 200 and book.get("balanceCents") == 5400, book)
    code, booking = call("GET", f"{base('shipment')}/shipments/{oid}")
    check("shipment booked", code == 200 and booking.get("status") == "BOOKED", booking)
    code, note = call("GET", f"{base('notify')}/notices/{oid}")
    check("notice delivered", code == 200 and note.get("status") == "DELIVERED", note)

    def timeline_ready():
        return True if timeline_has(oid, "SHIPPED") else None

    check("timeline shows shipped", until("timeline", timeline_ready, 40) is not None)
    status, replay = place(buyer, mug, 2, "SP", "DOCK1", request_id)
    check("same request returns the same order", status == 200 and replay.get("id") == oid, replay)
    time.sleep(1)
    check("replay does not take more stock", on_hand(mug) == 8, on_hand(mug))

    pick_before = depth("house.pick.dlq") or 0
    ghost = "MISSING-" + uuid.uuid4().hex[:8]
    status, missing_order = place(buyer, ghost, 1, "SP", "DOCK1")
    check("unknown sku cancels", await_status(missing_order["id"], "CANCELLED", "UNKNOWN_SKU") is not None)
    time.sleep(1)
    check("unknown sku does not dead-letter the pick", depth("house.pick.dlq") == pick_before, depth("house.pick.dlq"))

    priced = sellable("CENTRAL", 4)
    call("DELETE", "http://localhost:9/nope")
    # Price is required. Register a catalog sku with stock and no price by using a second sku name.
    bare = "NOPRICE-" + uuid.uuid4().hex[:8]
    status, _ = call("POST", f"{base('catalog')}/skus", {"sku": bare, "warehouseCode": "CENTRAL"})
    check("sku without a price", status == 201, status)
    status, bare_order = place(buyer, bare, 1, "SP", "DOCK1")
    check("missing price cancels", await_status(bare_order["id"], "CANCELLED", "PRICE") is not None)
    check("missing price did not expose credit", exposed(buyer) == 5400, exposed(buyer))

    blocked_sku = sellable("CENTRAL", 4)
    status, blocked = place(buyer, blocked_sku, 1, "blocked-market", "DOCK1")
    check("blocked region cancels", await_status(blocked["id"], "CANCELLED", "TAX") is not None)
    check("blocked region leaves the shelf", on_hand(blocked_sku) == 4, on_hand(blocked_sku))

    exempt_sku = sellable("CENTRAL", 4)
    status, exempt = place(buyer, exempt_sku, 1, "EXEMPT-ZONE", "DOCK1")
    exempt_body = await_status(exempt["id"], "SHIPPED")
    check("exempt region ships with zero tax", exempt_body is not None and exempt_body.get("taxCents") == 0 and exempt_body.get("totalCents") == 2500, exempt_body)

    short_buyer = customer(100000, "bea@shop.test")
    short_sku = sellable("CENTRAL", 1)
    status, short = place(short_buyer, short_sku, 2, "SP", "DOCK1")
    check("short shelf cancels", await_status(short["id"], "CANCELLED", "STOCK") is not None)

    def short_settled():
        if on_hand(short_sku) != 1 or exposed(short_buyer) != 0:
            return None
        code, _bill = invoice(short["id"])
        if code != 404:
            return None
        return True

    check("short shelf releases credit and skips the invoice", until("short settle", short_settled) is not None)

    closed_buyer = customer(100000, "cam@shop.test")
    closed_sku = sellable("WH-CLOSED", 3)
    pick_dlq = depth("house.pick.dlq") or 0
    status, closed = place(closed_buyer, closed_sku, 1, "SP", "DOCK1")

    def closed_settled():
        body = order(closed["id"])
        if not body or body.get("status") != "CANCELLED" or body.get("reason") != "PICK_FAILED":
            return None
        if on_hand(closed_sku) != 3 or exposed(closed_buyer) != 0:
            return None
        code, bill = invoice(closed["id"])
        led_code, led = receivable(closed_buyer)
        pick_code, _pick = call("GET", f"{base('fulfillment')}/picks/{closed['id']}")
        if code != 200 or bill.get("status") != "VOID" or led_code != 200 or led.get("balanceCents") != 0 or pick_code != 404:
            return None
        return body

    check("closed warehouse compensates", until("closed", closed_settled, 80) is not None)
    check("closed warehouse dead-letters the pick", grew("house.pick.dlq", pick_dlq) == pick_dlq + 1)

    ship_buyer = customer(100000, "dee@shop.test")
    ship_sku = sellable("CENTRAL", 3)
    ship_dlq = depth("house.ship.dlq") or 0
    status, declined = place(ship_buyer, ship_sku, 1, "SP", "DOCK0000")

    def ship_settled():
        body = order(declined["id"])
        if not body or body.get("reason") != "SHIPMENT_FAILED":
            return None
        if on_hand(ship_sku) != 3 or exposed(ship_buyer) != 0:
            return None
        code, bill = invoice(declined["id"])
        led_code, led = receivable(ship_buyer)
        pick_code, pick = call("GET", f"{base('fulfillment')}/picks/{declined['id']}")
        ship_code, _row = call("GET", f"{base('shipment')}/shipments/{declined['id']}")
        if code != 200 or bill.get("status") != "VOID" or led_code != 200 or led.get("balanceCents") != 0:
            return None
        if pick_code != 200 or pick.get("status") != "CANCELLED" or ship_code != 404:
            return None
        return body

    check("declined carrier compensates", until("carrier", ship_settled, 80) is not None)
    check("declined carrier dead-letters the ship", grew("house.ship.dlq", ship_dlq) == ship_dlq + 1)

    bounce_buyer = customer(100000, "A@Bounce.Test")
    bounce_sku = sellable("CENTRAL", 3)
    notice_dlq = depth("house.notify.dlq") or 0
    status, bounced = place(bounce_buyer, bounce_sku, 1, "SP", "DOCK1")
    bounce_body = await_status(bounced["id"], "SHIPPED")

    def bounce_settled():
        body = order(bounced["id"])
        code, _note = call("GET", f"{base('notify')}/notices/{bounced['id']}")
        if body and body.get("status") == "SHIPPED" and body.get("noticeStatus") == "BOUNCED" and code == 404 and on_hand(bounce_sku) == 2:
            return body
        return None

    check("bounced address stays shipped", until("bounce", bounce_settled, 80) is not None)
    check("bounced address dead-letters the notice", grew("house.notify.dlq", notice_dlq) == notice_dlq + 1)
    check("bounce body was shipped first", bounce_body is not None, bounce_body)

    tight = customer(8100, "eri@shop.test")
    tight_sku = sellable("CENTRAL", 20)

    def one_credit(_):
        return place(tight, tight_sku, 1, "SP", "DOCK1")

    with ThreadPoolExecutor(max_workers=4) as pool:
        raced = list(pool.map(one_credit, range(4)))
    ids = [body["id"] for code, body in raced if code == 201 and isinstance(body, dict)]
    check("four credit orders accepted", len(ids) == 4, [code for code, _body in raced])

    def credit_settled():
        states = []
        for oid in ids:
            body = order(oid)
            if not body or body.get("status") not in ("SHIPPED", "CANCELLED"):
                return None
            states.append(body)
        shipped = [row for row in states if row.get("status") == "SHIPPED"]
        cancelled = [row for row in states if row.get("status") == "CANCELLED"]
        if len(shipped) != 3 or len(cancelled) != 1 or cancelled[0].get("reason") != "CREDIT":
            return None
        if exposed(tight) != 8100 or on_hand(tight_sku) != 17:
            return None
        return states

    check("three of four orders fit the house limit", until("credit race", credit_settled, 90) is not None)

    shelf = customer(1000000, "fay@shop.test")
    shelf_sku = sellable("CENTRAL", 5)

    def one_stock(_):
        return place(shelf, shelf_sku, 1, "SP", "DOCK1")

    with ThreadPoolExecutor(max_workers=8) as pool:
        raced = list(pool.map(one_stock, range(8)))
    ids = [body["id"] for code, body in raced if code == 201 and isinstance(body, dict)]
    check("eight stock orders accepted", len(ids) == 8, [code for code, _body in raced])

    def stock_settled():
        states = []
        for oid in ids:
            body = order(oid)
            if not body or body.get("status") not in ("SHIPPED", "CANCELLED"):
                return None
            states.append(body)
        shipped = [row for row in states if row.get("status") == "SHIPPED"]
        cancelled = [row for row in states if row.get("reason") == "STOCK"]
        if len(shipped) != 5 or len(cancelled) != 3:
            return None
        if on_hand(shelf_sku) != 0 or exposed(shelf) != 13500:
            return None
        return states

    check("five of eight orders fit the shelf", until("stock race", stock_settled, 100) is not None)

    print("\n== summary ==")
    if failures:
        print(f"{len(failures)} failed")
        for item in failures:
            print(f" - {item}")
        sys.exit(1)
    print("house account smoke passed")


if __name__ == "__main__":
    main()
