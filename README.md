# Cloud Serverless Function with Google Cloud Functions v2 and Pub/Sub

This Cloud Function is designed to be triggered by messages published to a Pub/Sub topic. Upon receiving a message, it processes the message payload, extracts necessary information, sends an email using Mailgun API, and updates the expiration time for a given token in a MySQL database.

## Overview

This Cloud Function performs the following tasks:

1. Receives messages from a Pub/Sub topic.
2. Parses the message payload.
3. Extracts email, activation link, and token ID from the payload.
4. Sends an email with an activation link to the provided email address.
5. Updates the expiration time for the token in the database.

## Setup

### Prerequisites

Before deploying this function, make sure you have the following:

- Google Cloud Platform account.
- Google Cloud Functions v2 environment set up.
- Mailgun account with API key and domain set up.
- MySQL database accessible from Google Cloud Platform.

### Environment Variables

Make sure to set the following environment variables:

- `MAIL_GUN_DOMAIN_NAME`: Your Mailgun domain name.
- `MAIL_GUN_API_KEY`: Your Mailgun API key.
- `SQL_HOST`: Hostname of your MySQL database.
- `SQL_DATABASE`: Name of your MySQL database.
- `SQL_USERNAME`: Username to access the MySQL database.
- `SQL_PASSWORD`: Password to access the MySQL database.


## Usage

Once deployed, this function will be triggered automatically when messages are published to the specified Pub/Sub topic. The message payload should be a JSON object containing `email`, `activationLink`, and `tokenId` fields.

