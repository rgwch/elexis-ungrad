# Tardoc Billing Assistant

## What is it for?

The Tardoc tariff brings its own challenges: New tariff positions, minute-precise billing for time-based services, requirements for action-based services.

This plugin facilitates correct billing.

## Who is it for?

Currently, the plugin only works for holders of dignity 3010 (Specialists in General Internal Medicine)

## Usage:

When you start a new consultation, click the "Start" button. This automatically bills position CA.00.0010 and the stopwatch starts running. After 5 minutes, position CA.00.0030 is additionally billed and counts up one minute at a time, up to a maximum of 15 minutes.

When you bill an action position, the timer stops. In the consultation text, the examinations prescribed for this action service are predefined and must then be completed manually.

## Limitations

While the plugin can be configured using the file rsc/config.json, it has only been tested for basic general practitioner positions so far.
Also, config.json is very syntax-sensitive. After making changes, be sure to check with a JSON validator that it is correct.

## Vibe Coding

This plugin was created with significant help from AI (Claude Sonnet 4.5)
