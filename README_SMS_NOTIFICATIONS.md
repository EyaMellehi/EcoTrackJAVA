# SMS Notifications (Module Annonces)

This project now supports sending SMS notifications to citizens of the same region as an announcement, without database schema changes.

## What was added

- Region-targeted citizens query in `UserService`
- Twilio sender service in `SmsNotificationService`
- Local anti-duplicate log store in `AnnonceSmsLocalStore`
- UI action button in admin annonces list: `Informer les citoyens`
- New create flow in add annonces screen: `Créer + informer citoyens`
- Auto-load Twilio config support in `run.bat`

## Twilio configuration

Use environment variables OR a local file `twilio_sms.local.txt` at project root.

Example file format:

```
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_FROM_NUMBER=+12183192973
TWILIO_DEFAULT_COUNTRY_CODE=+216
```

A template exists: `twilio_sms.local.example.txt`.

## Phone number format

- Twilio requires E.164 numbers (`+` followed by country code and number)
- Local 8-digit numbers are auto-normalized with `TWILIO_DEFAULT_COUNTRY_CODE`
- Example: `26106036` becomes `+21626106036`

## Duplicate prevention without DB

A local CSV log file is used:

- `var/sms/annonce_sms_dispatch.csv`

If a row with status `SENT` already exists for the same `(annonce_id, user_id, phone)`, SMS is skipped.

## Runtime behavior

From `Gestion des Annonces`, each announcement includes:

- `Informer les citoyens`

From `Créer une Nouvelle Annonce`, you now have:

- `Créer l'annonce`
- `Créer + informer citoyens`

Flow for `Créer + informer citoyens`:

1. Publish announcement first
2. Confirm SMS sending
3. Fetch active citizens with phone in same region
4. Send in background task
5. Show summary: sent / skipped / failed

## Build verification

Validated with:

```powershell
mvn -DskipTests compile
```
