# Tardoc Billing Assistant

## What is it for?

The Tardoc tariff brings its own challenges: New billing positions, minute-accurate billing for time-based services, and requirements for procedural services.

This plugin facilitates correct billing.

## Who is it for?

Currently, the plugin only works for holders of dignity 3010 (Specialists in General Internal Medicine)

## Usage:

When you start a new consultation, click the "Start" button. This will automatically bill position CA.00.0010 and the stopwatch will start running. After 5 minutes, position CA.00.0030 is additionally billed and incremented every minute, up to a maximum of 15 minutes.

When you bill a procedural position, the timer is stopped. In the consultation text, the examinations required for this procedural service are pre-filled and must then be completed manually.

## Limitations

Although the plugin can be configured via the rsc/config.json file, it has so far only been tested for general practitioner basic positions.
Also, config.json is very syntax-sensitive. After making a change, be sure to check with a JSON validator that it is correct.
