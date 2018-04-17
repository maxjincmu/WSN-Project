#!/usr/bin/python
import bluetooth
import time

print("Finding devices...")

while True:
    time.sleep(60)
    nearby_devices = bluetooth.discover_devices(lookup_names = True, flush_cache = True, duration = 20)

    print("found %d devices" % len(nearby_devices))
 
    for addr, name in nearby_devices:
        print("  %s - %s" % (addr, name))
